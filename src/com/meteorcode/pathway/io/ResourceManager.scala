package com.meteorcode.pathway.io

import java.io.File

import scala.collection.JavaConversions._
import scala.collection.mutable

class ResourceManager (private val directories: List[FileHandle]) {
  def this(directory: FileHandle) = this(List(directory))
  def this(path: String) = this(new DesktopFileHandle("", path, null)) // default to DesktopFileHandle
  def this() = this("assets")             // it's okay for the Manager to be null because if it has a path,
                                          // it will never need to get the path from the ResourceManager
  private val ZipMatch = """(\/*\w*\/*\w*.zip)(\/\w+.*\w*)+""".r
  private val JarMatch = """(\/*\w*\/*\w*.jar)(\/\w+.*\w*)+""".r
  private var paths = Map[String, String]()
  private val cachedHandles = mutable.HashMap[String, FileHandle]()

  private def walk(h: FileHandle, fakePath: String) { // recursively walk the directories and cache the paths
    h.list.foreach { f: FileHandle =>
      f.extension match {
        case "jar" =>
          // logical path for an archive is attached at /, so we don't add it to the paths
          walk(new JarFileHandle("", f), "")  // but we do add the paths to its' children
        case "zip" =>
          walk(new ZipFileHandle("", f), "")  // walk all children of this dir
        case _ =>
          if (f.extension == "") {
            paths += (fakePath + f.name  -> f.physicalPath) // otherwise, add logical path maps to real path
          } else {
            paths += (fakePath + f.name + "." + f.extension -> f.physicalPath) // otherwise, add logical path maps to real path
          }
          if (f.isDirectory) walk(f, fakePath) // and walk (if it's a dir)
      }
    }
  }

  directories.foreach{ directory => walk(directory, directory.name) }

  protected[io] def getLogicalPath(physicalPath: String): String = paths.map(_.swap).get(physicalPath).get

  def handle (path: String): FileHandle = {
    if (cachedHandles.keySet contains path)
      cachedHandles.getOrElseUpdate(path, makeHandle(path))
    else {
      val f = makeHandle(path)
      cachedHandles += (path -> f)
      f
    }
  }

  private def makeHandle (fakePath: String): FileHandle = {
    val realPath: String = paths(fakePath)
    realPath.split('.').drop(1).lastOption match {
      case Some("jar") => new JarFileHandle(fakePath, new File(realPath), this)
      case Some("zip") => new ZipFileHandle(fakePath, new File(realPath), this)
      case _ => realPath match {
        case ZipMatch(zipfile, name) =>
          val parent = new ZipFileHandle(paths(zipfile), new File(zipfile), this)
          new ZipEntryFileHandle(parent.zipfile.getEntry(name), parent)
        case JarMatch(jarfile, name) =>
          val parent = new JarFileHandle(paths(jarfile), new File(jarfile), this)
          new JarEntryFileHandle(parent.jarfile.getJarEntry(name), parent)
        case _ => new DesktopFileHandle(fakePath, realPath, this)
      }
    }
  }

}