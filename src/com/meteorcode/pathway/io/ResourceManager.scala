package com.meteorcode.pathway.io

import java.io.{File, IOException}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * A ResourceManager "fuses" a directory or directories into a virtual filesystem, abstracting Zip and Jar archives
 * as though they were directories.
 *
 * Archives are attached at /" in the virtual filesystem, and directories within archives are "fused" into one directory
 * in the virtual filesystem. For example, if we have a file foo.zip containing the path foo/images/spam.png and another
 * file bar.jar, containing bar/images/eggs.jpeg, the virtual directory bar/ contains spam.png and eggs.jpeg.
 *
 * For security reasons, paths within the virtual filesystem are non-writable by default, unless they are within an
 * optional specified write directory. The write directory must be within one of the root directories of the virtual
 * filesystem, and must not be contained in an archive.
 *
 * @param directories A list of directories to be fused into the top level of the virtual filesystem.
 */
class ResourceManager protected (private val directories: List[FileHandle],
                                 private val writeDir: Option[FileHandle],
                                 private val policy: LoadOrderProvider) {
  /**
   * Constructor for a ResourceManager with a single managed directory.
   *
   * @param directory a FileHandle into the directory to manage.
   * @param policy a [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]] for resolving load collisions
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directory: FileHandle, policy: LoadOrderProvider) = this(List(directory), None, policy)

  /**
   * Constructor for a ResourceManager with a String representing a path to the managed directory.
   *
   * Note that this defaults to using [[com.meteorcode.pathway.io.DesktopFileHandle]] if you  want to use a different type of
   * FileHandle, use [[com.meteorcode.pathway.io.ResourceManager.handle]]  instead.
   *
   * @param path the path to the directory to manage
   * @param policy a [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]] for resolving load collisions
   * @return a new ResourceManager managing the specified directory.
   */
  def this(path: String, policy: LoadOrderProvider) = this(List(new DesktopFileHandle("", path, null)), None, policy)

  /**
   * Constructor for a ResourceManager with a single managed directory and a specified directory for writing.
   *
   * @param directory a FileHandle into the directory to manage.
   * @param policy a [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]] for resolving load collisions
   * @param writeDir a FileHandle into the write directory
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directory: FileHandle,
           writeDir: FileHandle,
           policy: LoadOrderProvider) = this(List(directory), Some(writeDir), policy)

  def this(path: String,                    // it's okay for the Manager to be null because if it has a path,
           writePath: String,               // it will never need to get the path from the ResourceManager
           policy: LoadOrderProvider) = this(List(new DesktopFileHandle("", path, null)),
                                             Some(new DesktopFileHandle(writePath, writePath, null)),
                                             policy)

  def this(directories: List[FileHandle],
           writeDir: FileHandle,
           policy: LoadOrderProvider) = this(directories, Some(writeDir), policy)



  private val ArchiveMatch = """([\s\S]*[^\/]*)(.zip|.jar)\/([^\/]+.*[^\/]*)""".r
  private val paths: mutable.Map[String,String] = buildVirtualFS(collectVirtualPaths(directories))
  private val cachedHandles = mutable.Map[String, FileHandle]()


  /**
   * Recursively walk the filesystem down from each FileHandle in a list
   * @param directories a list of FileHandles to seed the recursive walk
   */
  private def collectVirtualPaths(directories: List[FileHandle]): (List[String], List[FileHandle]) = {
    val roots = new ListBuffer[FileHandle]
    val virtualPaths = new ListBuffer[String]
    directories.foreach{directory => roots.add(directory)
                                     walk(directory, "/", virtualPaths, roots)
                        }
    // recursively walk the directories and cache the paths
    def walk(h: FileHandle, fakePath: String, virtualPaths: ListBuffer[String], roots: ListBuffer[FileHandle]) {
      for (f <- h.list) f.extension match {
          case "jar" =>
            // virtual path for an archive is attached at /, so we don't add it to the paths
            val handle = new JarFileHandle("/", f)
            roots.add(handle) // but we do add it to the roots
            walk(handle, "", virtualPaths, roots) // and we add the paths to its' children
          case "zip" =>
            val handle = new ZipFileHandle("/", f)
            roots.add(handle)
            walk(handle, "", virtualPaths, roots) // walk all children of this dir
          case _ =>
            if (f.extension == "") virtualPaths.add(fakePath + f.name) // add the path
            else virtualPaths.add(fakePath + f.name + "." + f.extension)
            if (f.isDirectory) walk(f, fakePath + f.name + "/", virtualPaths, roots)  // and walk (if it's a dir)
        }
      }
    (virtualPaths.toList, roots.toList)
    }

  private def buildVirtualFS(pathsAndRoots: (List[String], List[FileHandle])): mutable.Map[String, String] = {
    val virtualPaths = pathsAndRoots._1
    val orderedRoots = policy.orderPaths(pathsAndRoots._2) // have the load-order policy rank all the roots
    val map = mutable.Map[String, String]()

    for (root <- orderedRoots) walk(root, map)

    def walk(handle: FileHandle, m: mutable.Map[String, String]): Unit = handle.list.foreach {
      file =>
        m += (file.path -> file.physicalPath)
        if (file.isDirectory) walk(file, m)
      }
    map
  }

  /**
   * @return the path to the designated write directory, or null if there is no write directory.
   */
  def getWritePath = if (writeDir.isDefined) writeDir.get.physicalPath else null

  /**
   * Returns true if a given virtual path is writable, false if it is not.
   * @param virtualPath a path in the virtual filesystem
   * @return true if that path can be written to, false if it cannot
   */
  def isPathWritable(virtualPath: String) = if (writeDir.isDefined) (virtualPath.contains(getWritePath)) else false

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
    val realPath: String = paths.get(fakePath) match {
      case s:Some[String] => s.get
      case None => // If the path is not in the tree, handle write attempts.
        if (isPathWritable(fakePath)) {
          paths += (fakePath -> (writeDir.get.physicalPath + fakePath.replace(writeDir.get.path, "")))
          paths(fakePath)
        } else {
          throw new IOException("A filehandle to an empty path was requested, and the requested path was not writable")
        }
    }
    realPath.split('.').drop(1).lastOption match {
      case Some("jar") => new JarFileHandle(fakePath, new File(realPath), this)
      case Some("zip") => new ZipFileHandle(fakePath, new File(realPath), this)
      case _ => ArchiveMatch.findFirstIn(realPath) match {
        case Some(ArchiveMatch(path, extension, name)) => extension match {
          case ".zip" =>
            val parent = new ZipFileHandle("/", new File(path + extension), this)
            new ZipEntryFileHandle(fakePath, parent.zipfile.getEntry(name), parent)
          case ".jar" =>
            val parent = new JarFileHandle("/", new File(path + extension), this)
            new JarEntryFileHandle(fakePath, parent.jarfile.getJarEntry(name), parent)
        }
        case _ => new DesktopFileHandle(fakePath, realPath, this)
      }
    }
  }

}