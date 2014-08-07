package com.meteorcode.pathway.io

import java.io.File
import scala.collection.mutable.Map

class ResourceManager (private val directories: List[FileHandle]) {
  def this(directory: FileHandle) = this(List(directory))
  def this() = this(List[FileHandle](new DesktopFileHandle("assets")))

  private val cachedPaths = Map[String, String]

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
  private val cachedHandles = Map[String, FileHandle]()

  def handle (path: String) = cachedHandles.getOrElseUpdate(path, makeHandle(path))

  private def makeHandle (fakePath: String): FileHandle = {
    val realPath = cachedPaths get fakePath
    realPath.split('.').drop(1).lastOption match {
      case Some("jar") => new JarFileHandle(realPath)
      case Some("zip") => new ZipFileHandle(realPath)
      case _ => new DesktopFileHandle(realPath) //TODO: check if contained by {Zip|Jar} and return {Zip|Jar}EntryFileHandle
    }
  }

}