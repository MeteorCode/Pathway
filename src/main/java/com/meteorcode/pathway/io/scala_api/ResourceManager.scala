package com.meteorcode.pathway.io.scala_api

import java.io.{File, IOException}
import java.util
import java.util.jar.JarFile
import java.util.zip.ZipFile

import com.meteorcode.common.ForkTable
import com.meteorcode.pathway.io._
import com.meteorcode.pathway.io.java_api.LoadOrderProvider
import com.meteorcode.pathway.logging.Logging

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
 * A ResourceManager "fuses" a directory or directories into a virtual filesystem, abstracting Zip and Jar archives
 * as though they were directories.
 *
 * Root directories and archives archives are attached at `/` in the virtual filesystem, and directories within archives
 * are "fused" into one directory in the virtual filesystem. For example, if we have a file `foo.zip` containing the
 * path `foo/images/spam.png` and a directory `bar` containing `bar/images/eggs.jpeg`, the virtual directory `images/`
 * contains `spam.png` and `eggs.jpeg`.
 *
 * For security reasons, paths within the virtual filesystem are non-writable by default, unless they are within an
 * optional specified write directory. The write directory may exist at any writable physical path, but it will always
 * be attached at `/write/` in the virtual filesystem. Note that if the write directory doesn't exist when this
 * ResourceManager is initialized, it will be created, along with any directories containing it, if necessary.
 *
 * @param directories A list of [[FileHandle]] into the directories to be fused into the top level roots of the virtual
 *                    filesystem.
 * @param writeDir An optional [[FileHandle]] into the specified write directory. The write directory's virtual path will be
 *                 set to `/write/`.
 * @param order A [[LoadOrderPolicy]] representing the game's load-order
 *               policy.
 */

class ResourceManager private[this] (
  private[this] val directories: util.List[FileHandle],
  private[this] val writeDir: Option[FileHandle],
  private[this] val order: LoadOrderPolicy
) extends Logging { // TODO: refactor constructor
  /**
   * Constructor for a ResourceManager with a single managed directory.
   *
   * @param directory a FileHandle into the directory to manage.
   * @param policy a [[LoadOrderProvider]] for resolving load collisions
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directory: FileHandle, policy: LoadOrderProvider) = this(List(directory), None, policy)

  /**
   * Constructor for a ResourceManager with a list of managed directories.
   *
   * @param directories a list of FileHandles into the directories to manage.
   * @param policy a [[LoadOrderProvider]] for resolving load collisions
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directories: util.List[FileHandle], policy: LoadOrderProvider) = this(directories, None, policy)

  /**
   * Constructor for a ResourceManager with a single managed directory and a specified directory for writing.
   *
   * @param directory a [[FileHandle]] into the directory to manage.
   * @param policy a [[LoadOrderProvider]] for resolving load collisions
   * @param writeDir a [[FileHandle]] into the write directory
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directory: FileHandle,
           writeDir: FileHandle,
           policy: LoadOrderProvider) = this(List(directory), Some(writeDir), policy)

  /**
   * Constructor for a ResourceManager with a single managed directory and a specified directory for writing.
   * The write directory's virtual path will be automatically determined.
   *
   * @param path a String representing the path to the directory to manage.
   * @param policy a [[LoadOrderProvider]] for resolving load collisions
   * @param writePath a String representing the path into the write directory
   * @return a new [[ResourceManager]] managing the specified directory.
   */
  def this(path: String,                    // it's okay for the Manager to be null because if it has a path,
           writePath: String,               // it will never need to get the path from the ResourceManager
           policy: LoadOrderProvider) = this(Seq[FileHandle](new FilesystemFileHandle("", path, null)),
                                             writeDir = Some(new FilesystemFileHandle(writePath.replace(path, ""), writePath, null)),
                                             order = policy)

  def this(directories: util.List[FileHandle],
           writeDir: FileHandle,
           policy: LoadOrderProvider) = this(directories, Some(writeDir), policy)

  private[this] val paths = makeFS(directories)//buildVirtualFS(collectVirtualPaths(directories))
  private[this] val cachedHandles = mutable.Map[String, FileHandle]()

  writeDir.foreach{ directory =>
      if (!directory.exists) {
        if (!(directory.file exists (_.mkdirs()) ) ) throw new IOException("Specified write directory could not be created!")
        else logger.log(this.toString, s"write directory ${directory.physicalPath} created")
      } else logger.log(this.toString, s"write directory ${directory.physicalPath} already exists")
      if (directory.manager == null) directory.manager = this
  }
  /**
   * Recursively walk the filesystem down from each FileHandle in a list
   * @param dirs a list of FileHandles to seed the recursive walk
   */

  private[this] def makeFS(dirs: Seq[FileHandle]): ForkTable[String,String] = {
    val fs = new ForkTable[String,String]
    def _walk(current: FileHandle, fs: ForkTable[String,String]): ForkTable[String,String] = current match {
      case FileHandle(virtualPath,physicalPath) if current.isDirectory =>
        val newfs = fs.fork
        newfs put (virtualPath, physicalPath)
        order(current.list.get).foldRight(newfs)((fh, tab) => _walk(fh, tab))
      case FileHandle(virtualPath, physicalPath) => fs put (virtualPath, physicalPath); fs
      case _ => throw new IOException(s"FATAL: FileHandle $current did not have a physical path")
    }
    val orderedFS = order(dirs).foldRight(fs)((fh, tab) => _walk(fh, tab))
    writeDir match { // TODO: this is where we could "freeze" the un-writedir'd map
      case Some(dir) => _walk(dir, orderedFS)
      case _ => orderedFS
    }
  }

  /**
   * Returns true if a given virtual path is writable, false if it is not.
   *
   * TODO: make this a method on Strings instead (eta-expansion) for code-prettiness reasons
   * @param virtualPath a path in the virtual filesystem
   * @return true if that path can be written to, false if it cannot
   */
  def isPathWritable(virtualPath: String): Boolean = if (writeDir.isDefined) virtualPath.contains("write/") else false

  /**
   * Request that the ResourceManager handle the file at a given path.
   *
   * ResourceManager attempts to cache all FileHandles requested, so if you request a FileHandle once and then
   * request it again later, you will receive the same FileHandle, if possible. This is because making new FileHandles
   * is a relatively expensive operation.
   *
   * @param path The virtual path to the requested object
   * @return A [[Success]] containing a [[FileHandle]] into the object that exists at the requested path
   *         in the virutal filesystem, or a [[Failure]] containing an [[IOException]] if something went
   *         wrong while handling the path.
   */
  def handle(path: String): Try[FileHandle] = if (cachedHandles.keySet contains path)
      Try(cachedHandles.getOrElseUpdate(path, makeHandle(path).get))
    else makeHandle(path) map { (f) => cachedHandles += (path -> f); f }

  private[this] def makeHandle(virtualPath: String): Try[FileHandle] = {
    logger.log(this.toString, s"making a FileHandle for $virtualPath")
    (paths.get(trailingSlash(virtualPath)) match {
      case Some(s: String) => Success(s)
      // If the path is not in the tree, handle write attempts.
      case None if isPathWritable(virtualPath) =>
        logger.log(this.toString, s"handling write attempt to empty path $virtualPath")
        assume(writeDir.isDefined)
        writeDir match {
          case Some(FileHandle(writeVirt, writePhys)) =>
            val phys = writePhys +               // The physical path is the write dir's physical path, plus
              virtualPath.replace(writeVirt, "") // the virtual path with the write dir's virtual path removed.
            paths put (virtualPath, phys) // Since we've created a new FS object, add it to the known paths.
            logger.log(this.toString, s"successfully handled write attempt to $virtualPath")
            Success(phys)
          case Some(_) => // if the write directory won't destructure, it's missing a physical path.
            // if this is the case, somebody seriously fucked up.
            Failure(new IOException("Cannot handle write attempt: write directory missing physical path."))
          case None =>
            // if there's no write directory, we cannot support write attempts.
            // This should never happen – isPathWritable() should prevent this.
            Failure(new IOException("FATAL: Cannot handle write attempt:" +
              " no write directory exists, but a path claimed to be writable.\n"))
        }
      case None =>
        Failure(new IOException(s"A filehandle to an empty path ($virtualPath) was requested," +
        " and the requested path was not writable"))
    }) flatMap { (physicalPath: String) =>
      physicalPath.split('.').drop(1).lastOption match {
        case Some("jar") => Success(new JarFileHandle(virtualPath, new File(physicalPath), this))
        case Some("zip") => Success(new ZipFileHandle(virtualPath, new File(physicalPath), this))
        case _ => inArchiveRE findFirstIn physicalPath match {
          case Some(inArchiveRE(path, ".zip", name)) =>
            val parent = new ZipFileHandle("/", new File(s"$path.zip"), this)
            parent.file match {
              case Some(file) => Success(new ZipEntryFileHandle(virtualPath, new ZipFile(file).getEntry(name), parent))
              case None => Failure(new IOException(s"FATAL: ZipFileHandle $parent was not backed by a File object"))
            }
          case Some(inArchiveRE(path, ".jar", name)) =>
            val parent = new JarFileHandle("/", new File(s"$path.jar"), this)
            parent.file match {
              case Some(file) => Success(new JarEntryFileHandle(virtualPath, new JarFile(file).getJarEntry(name), parent))
              case None => Failure(new IOException(s"FATAL: JarFileHandle $parent was not backed by a File object"))
            }
          case _ => Success(new FilesystemFileHandle(virtualPath, physicalPath, this))
        }
      }
    }
  }

  override def toString: String = "ResourceManager" + directories.map {
    case FileHandle(_, physPath) => physPath.split(File.separatorChar).lastOption.getOrElse("")
    case dir => throw new IOException(s"FATAL: FileHandle $dir did not have a physical path")
  }.mkString

}