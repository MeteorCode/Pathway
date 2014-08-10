package com.meteorcode.pathway.io

import java.io.{
File,
InputStream,
IOException
}
import java.util.{
List,
ArrayList
}
import java.util.jar.JarFile


class JarFileHandle (logicalPath: String,
                     private val back: File,
                     manager: ResourceManager) extends FileHandle(logicalPath, manager) {
  // I also hate java.util.jar
  protected[io] var jarfile = new JarFile(back)

  def this(logicalPath: String, fileHandle: FileHandle) = this(logicalPath, fileHandle.file, fileHandle.manager)

  protected[io] def file = back

  protected[io] def physicalPath = back.getPath

  def exists: Boolean = back.exists

  def isDirectory: Boolean = true

  def writable = false

  @throws(classOf[IOException])
  def list: List[FileHandle] = {
    var result = new ArrayList[FileHandle]
    try {
      val entries = jarfile.entries
      while (entries.hasMoreElements) {
        val e = entries.nextElement()
        if (e.getName.matches("""^[^\/]+\/*$""")) { // is the entry a top-level child
          result.add(new JarEntryFileHandle(e, this))
        }
      }
      jarfile = new JarFile(back) // reset the archive
      result
    } catch {
      case e: IllegalStateException => throw new IOException("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
    }
  }

  @throws(classOf[IOException])
  def write(append: Boolean) = null

  def read: InputStream = null
}