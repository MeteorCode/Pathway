package com.meteorcode.pathway.io

import java.io.File

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * <p>A ResourceManager "fuses" a directory or directories into a virtual filesystem, abstracting Zip and Jar archives
 * as though they were directories.</p>
 *
 * <p>Archives are attached at "/" in the virtual filesystem, and directories within
 * archives are "fused" into one directory in the virtual filesystem. For example, if we have a file foo.zip containing
 * the path foo/images/spam.png and another file bar.jar, containing bar/images/eggs.jpeg, the virtual directory bar/
 * contains spam.png and eggs.jpeg.</p>
 * @param directories A list of directories to be fused into the top level of the virtual filesystem.
 */
class ResourceManager(private val directories: List[FileHandle]) {
  /**
   * Constructor for a ResourceManager with a single managed directory.
   * @param directory a FileHandle into the directory to manage.
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directory: FileHandle) = this(List(directory))

  /**
   * <p>Constructor for a ResourceManager with a String representing a path to the managed directory.</p>
   * <p>Note that this defaults to using {@link com.meteorcode.io.DesktopFileHandle DesktopFileHandles}, if you
   * want to use a different type of FileHandle, use the
   * {@link com.meteorcode.io.ResourceManager#ResourceManagerFileHandle) ResourceManager(FileHandle)} constructor
   * instead.</p>
   * @param path the path to the directory to manage
   * @return a new ResourceManager managing the specified directory.
   */
  def this(path: String) = this(new DesktopFileHandle("", path, null))


  // it's okay for the Manager to be null because if it has a path,
  // it will never need to get the path from the ResourceManager
  private val ArchiveMatch = """([A-Za-z0-9_/]*\w*)(.zip|.jar)\/(\w+.*\w*)""".r
  private var paths = Map[String, String]()
  private val cachedHandles = mutable.HashMap[String, FileHandle]()

  /**
   * Recursively walk the filesystem down from a given FileHandle
   * @param h the FileHandle tos eed the recursive walk
   * @param fakePath the logical path represented by h
   */
  private def walk(h: FileHandle, fakePath: String) {
    // recursively walk the directories and cache the paths
    h.list.foreach { f: FileHandle =>
      f.extension match {
        case "jar" =>
          // logical path for an archive is attached at /, so we don't add it to the paths
          walk(new JarFileHandle("", f), "") // but we do add the paths to its' children
        case "zip" =>
          walk(new ZipFileHandle("", f), "") // walk all children of this dir
        case _ =>
          if (f.extension == "") {
            paths += (fakePath + f.name -> f.physicalPath) // otherwise, add logical path maps to real path
          } else {
            paths += (fakePath + f.name + "." + f.extension -> f.physicalPath) // otherwise, map logical path to real
          }
          if (f.isDirectory) walk(f, fakePath) // and walk (if it's a dir)
      }
    }
  }

  directories.foreach { directory => walk(directory, directory.name)}

  /**
   * Request the logical path for a given physical path.
   * @param physicalPath a physical path in the filesystem
   * @return the logical path corresponding to that physical path.
   * @deprecated As you can no longer make FileHandles with null paths, this should no longer be necessary.
   */
  protected[io] def getLogicalPath(physicalPath: String): String = paths.map(_.swap).get(physicalPath).get

  /**
   * Request that the ResourceManager handle the file at a given path.
   * @param path The virtual path to the requested object
   * @return A { @link com.meteorcode.io.FileHandle FileHandle} wrapping the object that exists at the requested path
   */
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
        //TODO: define a better write location, directories(0) may not always be correct
        paths += (fakePath -> (directories(0).physicalPath + "/" + fakePath))
        paths(fakePath)
    }
    realPath.split('.').drop(1).lastOption match {
        //TODO: Handle write attempts into archives by uncompressing the archive, writing the file, and recompressing
      case Some("jar") => new JarFileHandle(fakePath, new File(realPath), this)
      case Some("zip") => new ZipFileHandle(fakePath, new File(realPath), this)
      case _ => ArchiveMatch.findFirstIn(realPath) match {
        case Some(ArchiveMatch(path, extension, name)) => extension match {
          case ".zip" =>
            val parent = new ZipFileHandle("/", new File(path + extension), this)
            new ZipEntryFileHandle(parent.zipfile.getEntry(name), parent)
          case ".jar" =>
            val parent = new JarFileHandle("/", new File(path + extension), this)
            new JarEntryFileHandle(parent.jarfile.getJarEntry(name), parent)
        }
        case _ => new DesktopFileHandle(fakePath, realPath, this)
      }
    }
  }

}