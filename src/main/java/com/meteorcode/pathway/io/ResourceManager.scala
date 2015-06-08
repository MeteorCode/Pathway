package com.meteorcode.pathway.io

import java.io.{File, IOException}

import java.util


import com.meteorcode.pathway.io.scala_api._
import com.meteorcode.common.ForkTable
import com.meteorcode.pathway.logging.Logging

import scala.collection.JavaConversions._
import scala.collection.mutable

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
 * @param directories A list of FileHandles into the directories to be fused into the top level roots of the virtual
 *                    filesystem.
 * @param writeDir An optional FileHandle into the specified write directory. The write directory's virtual path will be
 *                 set to `/write/`.
 * @param policy A [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]] representing the game's load-order
 *               policy.
 */
class ResourceManager (val directories: Seq[FileHandle],
                       val writeDir: Option[FileHandle] = None,
                       val policy: LoadOrderProvider    = new AlphabeticLoadPolicy
                        ) extends Logging {

  /**
   * Constructor for a ResourceManager with a single managed directory and a specified directory for writing.
   * The write directory's virtual path will be automatically determined.
   *
   * @param path a String representing the path to the directory to manage.
   * @param policy a [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]] for resolving load collisions
   * @param writePath a String representing the path into the write directory
   * @return a new ResourceManager managing the specified directory.
   */
  def this(path: String,                    // it's okay for the Manager to be null because if it has a path,
           writePath: String,               // it will never need to get the path from the ResourceManager
           policy: LoadOrderProvider) = this(Seq[FileHandle](new FilesystemFileHandle("", path, null)),
                                             writeDir = Some(new FilesystemFileHandle(writePath.replace(path, ""), writePath, null)),
                                             policy = policy)

  def this(directories: util.List[FileHandle],
           writeDir: FileHandle,
           policy: LoadOrderProvider) = this(directories, Some(writeDir), policy)

  private val paths = makeFS(directories)//buildVirtualFS(collectVirtualPaths(directories))
  private val cachedHandles = mutable.Map[String, FileHandle]()

  writeDir.foreach{ directory =>
      if (!directory.exists) {
        if (!directory.file.mkdirs()) throw new IOException("Specified write directory could not be created!")
        else logger.log(this.toString, s"write directory ${directory.physicalPath} created")
      } else logger.log(this.toString, s"write directory ${directory.physicalPath} already exists")
      if (directory.manager == null) directory.manager = this
  }
  /**
   * Recursively walk the filesystem down from each FileHandle in a list
   * @param dirs a list of FileHandles to seed the recursive walk
   */
  private def makeFS(dirs: util.List[FileHandle]): ForkTable[String,String] = {
    val fs = new ForkTable[String,String]
    def _walk(current: FileHandle, fs: ForkTable[String,String]): ForkTable[String,String] = current match {
      case a: FileHandle if a.isDirectory =>
        val newfs = fs.fork
        newfs put (current.path, current.physicalPath)
        policy.orderPaths(a.list).foldRight(newfs)((fh, tab) => _walk(fh, tab))
      case _: FileHandle => fs put (current.path, current.physicalPath); fs
    }
    val orderedFS = policy.orderPaths(dirs).foldRight(fs)((fh, tab) => _walk(fh, tab))
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
  def isPathWritable(virtualPath: String) = if (writeDir.isDefined) virtualPath.contains("write/") else false

  /**
   * Request that the ResourceManager handle the file at a given path.
   *
   * ResourceManager attempts to cache all FileHandles requested, so if you request a FileHandle once and then
   * request it again later, you will receive the same FileHandle, if possible. This is because making new FileHandles
   * is a relatively expensive operation.
   *
   * @param path The virtual path to the requested object
   * @return A [[com.meteorcode.pathway.io.FileHandle]] wrapping the object that exists at the requested path
   */
  @throws(classOf[IOException])
  def handle(path: String): FileHandle = {
    if (cachedHandles.keySet contains path)
      cachedHandles.getOrElseUpdate(path, makeHandle(path))
    else {
      val f = makeHandle(path)
      cachedHandles += (path -> f)
      f
    }
  }

  private def makeHandle(fakePath: String): FileHandle = {
    logger.log(this.toString, "making a FileHandle for " + fakePath)
    val realPath: String = paths.get(trailingSlash(fakePath)) match {
      case Some(s: String) => s
      // If the path is not in the tree, handle write attempts.
      case None if isPathWritable(fakePath) =>
        logger.log(this.toString, s"handling write attempt to empty path $fakePath")
        paths put (fakePath, writeDir.get.physicalPath + fakePath.replace(writeDir.get.path, ""))
        logger.log(this.toString, "successfully handled write attempt")
        paths(fakePath)
      case None =>  throw new IOException(s"A filehandle to an empty path ($fakePath) was requested, and the requested path was not writable")
    }
    realPath.split('.').drop(1).lastOption match {
      case Some("jar") => new JarFileHandle(fakePath, new File(realPath), this)
      case Some("zip") => new ZipFileHandle(fakePath, new File(realPath), this)
      case _ => inArchiveRE findFirstIn realPath match {
        case Some(inArchiveRE(path, ".zip", name)) =>
            val parent = new ZipFileHandle("/", new File(s"$path.zip"), this)
            new ZipEntryFileHandle(fakePath, parent.zipfile.getEntry(name), parent)
        case Some(inArchiveRE(path, ".jar", name)) =>
            val parent = new JarFileHandle("/", new File(s"$path.jar"), this)
            new JarEntryFileHandle(fakePath, parent.jarfile.getJarEntry(name), parent)
        case _ => new DesktopFileHandle(fakePath, realPath, this)
      }
    }
  }

  override def toString = "ResourceManager" + directories
    .map(_.physicalPath.split(File.separatorChar).last)
    .mkString(",")
}
