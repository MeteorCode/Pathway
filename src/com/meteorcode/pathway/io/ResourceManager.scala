package com.meteorcode.pathway.io

import java.io.File
import scala.collection.immutable.HashMap

class ResourceManager (private val directories: List[FileHandle]) {
  def this(directory: FileHandle) = this(List(directory))
  def this() = this(List[FileHandle](new DesktopFileHandle("assets")))

  private var cachedPaths = HashMap[String, String]

  directories.foreach{ directory =>
    def walk(h: FileHandle, currentPath: String) { // recursively walk the directories and cache the paths
      h.list.foreach { f: FileHandle =>
        f.extension match {
          case "jar" | "zip" =>             // special-case jars/zips because we want them to have directory-like paths
            cachedPaths += f.name -> f.path // add (logical path -> real path) to the cached paths
            walk(f, f.name)                 // walk all children of this dir
          case _ =>
            cachedPaths += currentPath + f.name + f.extension -> f.path
            walk (f, currentPath)
          }
        }
      }
    walk(directory, directory.name)
    }
  }

  // TODO: traverse the tree from the initial FileHandle down and call list(), building the tree?
  private val cachedHandles = collection.mutable.Map[String, FileHandle]()

  def handle (path: String) = cachedHandles.getOrElseUpdate(path, makeHandle(path))

  private def makeHandle (path: String): FileHandle = {
    // TODO: Special-case files within archives, this placeholder pattern match will have a Hard Time
    // if you try to get a handle into a file within an archive directly instead of requesting the top-level archive
    // TODO: detect if requested path is on the classpath and if so, return a ClasspathFileHandle.
    if (path.startsWith("/")) {
      // handle absolute paths
      path.split('.').drop(1).lastOption match {
        case Some("jar") => new JarFileHandle(path, new File(path), this)
        case Some("zip") => new ZipFileHandle(path, new File(path), this)
        case _ => new DesktopFileHandle(path, new File(path), this)
      }
    } else {
      // handle relative paths relative to assets dir
      path.split('.').drop(1).lastOption match {
        case Some("jar") => new JarFileHandle(path, new File(assetsDir + path), this)
        case Some("zip") => new ZipFileHandle(path, new File(assetsDir + path), this)
        case _ => new DesktopFileHandle(path, new File(assetsDir + path), this)
      }
    }
  }

}