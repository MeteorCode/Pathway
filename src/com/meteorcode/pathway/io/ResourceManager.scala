package com.meteorcode.pathway.io

import java.io.File

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * A ResourceManager "fuses" a directory or directories into a virtual filesystem, abstracting Zip and Jar archives
 * as though they were directories.
 *
 * Archives are attached at /" in the virtual filesystem, and directories within archives are "fused" into one directory
 * in the virtual filesystem. For example, if we have a file foo.zip containing the path foo/images/spam.png and another
 * file bar.jar, containing bar/images/eggs.jpeg, the virtual directory bar/ contains spam.png and eggs.jpeg.
 *
 * @param directories A list of directories to be fused into the top level of the virtual filesystem.
 */
class ResourceManager(private val directories: List[FileHandle]) {
  /**
   * Constructor for a ResourceManager with a single managed directory.
   *
   * @param directory a FileHandle into the directory to manage.
   * @return a new ResourceManager managing the specified directory.
   */
  def this(directory: FileHandle) = this(List(directory))

  /**
   * Constructor for a ResourceManager with a String representing a path to the managed directory.
   *
   * Note that this defaults to using [[com.meteorcode.io.DesktopFileHandle]] if you  want to use a different type of
   * FileHandle, use the [[com.meteorcode.io.ResourceManager#ResourceManagerFileHandle)]] constructor  instead.
   *
   * @param path the path to the directory to manage
   * @return a new ResourceManager managing the specified directory.
   */
  def this(path: String) = this(new DesktopFileHandle("", path, null))


  // it's okay for the Manager to be null because if it has a path,
  // it will never need to get the path from the ResourceManager
  private val ArchiveMatch = """([\s\S]*[^\/]*)(.zip|.jar)\/([^\/]+.*[^\/]*)""".r
  private var paths = Map[String, String]()
  private val cachedHandles = mutable.HashMap[String, FileHandle]()

  /**
   * Recursively walk the filesystem down from a given FileHandle
   * @param h the FileHandle tos eed the recursive walk
   * @param fakePath the virtual path represented by h
   */
  private def walk(h: FileHandle, fakePath: String) {
    // recursively walk the directories and cache the paths
    h.list.foreach { f: FileHandle =>
      f.extension match {
        case "jar" =>
          // virtual path for an archive is attached at /, so we don't add it to the paths
          walk(new JarFileHandle("", f), "") // but we do add the paths to its' children
        case "zip" =>
          walk(new ZipFileHandle("", f), "") // walk all children of this dir
        case _ =>
          if (f.extension == "") {
            paths += (fakePath + f.name -> f.physicalPath) // otherwise, add virtual path maps to real path
          } else {
            paths += (fakePath + f.name + "." + f.extension -> f.physicalPath) // otherwise, map virtual path to real
          }
          if (f.isDirectory) walk(f, fakePath + f.name + "/") // and walk (if it's a dir)
      }
    }
  }

  directories.foreach { directory => walk(directory, directory.name)}

  /**
   * Request the virtual path for a given physical path.
   *
   * @param physicalPath a physical path in the filesystem
   * @return the virtual path corresponding to that physical path.
   * @deprecated As you can no longer make FileHandles with null paths, this should no longer be necessary.
   */
  protected[io] def getVirtualPath(physicalPath: String): String = paths.map(_.swap).get(physicalPath).get

  /**
   * Request that the ResourceManager handle the file at a given path.
   *
   * ResourceManager attempts to cache all FileHandles requested, so if you request a FileHandle once and then
   * request it again later, you will receive the same FileHandle, if possible. This is because making new FileHandles
   * is a relatively expensive operation.
   *
   * @param path The virtual path to the requested object
   * @return A [[com.meteorcode.io.FileHandle]] wrapping the object that exists at the requested path
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