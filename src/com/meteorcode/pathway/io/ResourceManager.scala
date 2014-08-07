package com.meteorcode.pathway.io

import java.io.File

import scala.collection.mutable.{Map, HashMap}
import scala.collection.JavaConversions._

class ResourceManager (private val directories: List[FileHandle]) {
  def this(directory: FileHandle) = this(List(directory))
  def this(path: String) = this(new DesktopFileHandle("/", path, this)) // default to desktopfilehandle
  def this() = this("assets")

  private val ZipMatch = """(\/*\w*\/*\w*.zip)(\/\w+.*\w*)+""".r
  private val JarMatch = """(\/*\w*\/*\w*.jar)(\/\w+.*\w*)+""".r
  private val paths: Map[String, String] = new HashMap[String, String]

  private def walk(h: FileHandle, currentPath: String) { // recursively walk the directories and cache the paths
    h.list.foreach { f: FileHandle =>
      f.extension match {
        case "jar" =>
          // logical path for an archive is attached at /, so we don't add it to the paths
          walk(new JarFileHandle("/", f), "/")  // but we do add the paths to its' children
        case "zip" =>
          walk(new ZipFileHandle("/", f), "/")  // walk all children of this dir
        case _ =>
          paths += currentPath + f.name + f.extension -> f.path // otherwise, add logical path maps to real path
          if (f.isDirectory) walk(f, currentPath) // and walk (if it's a dir)
      }
    }
  }

  directories.foreach{ directory => walk(directory, directory.name) }

  protected[io] def getLogicalPath(physicalPath: String): String = paths.map(_.swap).get(physicalPath).get

  // TODO: traverse the tree from the initial FileHandle down and call list(), building the tree?
  private val cachedHandles: Map[String, FileHandle] = new HashMap[String, FileHandle]

  def handle (path: String) = cachedHandles.getOrElseUpdate(path, makeHandle(path))

  private def makeHandle (fakePath: String): FileHandle = {
    val realPath: String = paths(fakePath)
    realPath.split('.').drop(1).lastOption match {
      case Some("jar") => new JarFileHandle(fakePath, new File(realPath), this)
      case Some("zip") => new ZipFileHandle(fakePath, new File(realPath), this)
      case _ => realPath match {
        case ZipMatch(zipfile, name) =>
          val parent = new ZipFileHandle(paths(zipfile), new File(zipfile), this)
          new ZipEntryFileHandle(parent.zipfile.getEntry(name), parent)
        case JarMatch(jarfile, name) => //TODO: Extracting a match is deprecated, refactor
          val parent = new JarFileHandle(paths(jarfile), new File(jarfile), this)
          new JarEntryFileHandle(parent.jarfile.getJarEntry(name), parent)
        case _ => new DesktopFileHandle(fakePath, realPath, this)
      }
    }
  }

}