package com.meteorcode.pathway.io

import java.io.File

class ResourceManager (private val assetsDir: String) {
  def this() = this("assets")
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