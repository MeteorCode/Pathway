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
import java.util.jar.{JarEntry, JarFile}


class JarFileHandle protected[io](logicalPath: String,
                                  private val back: File,
                                  manager: ResourceManager) extends FileHandle(logicalPath, manager) {
  // I also hate java.util.jar
  protected[io] val jarfile = new JarFile(back)

  protected[io] def this(logicalPath: String, fileHandle: FileHandle) = this(logicalPath, fileHandle.file, fileHandle.manager)

  protected[io] def this(logicalPath: String, fileHandle: FileHandle, manager: ResourceManager) = this(logicalPath, fileHandle.file, manager)

  protected[io] def this(file: File, manager: ResourceManager) = this(null, file, manager)

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

      while (entries.hasMoreElements) result.add(new JarEntryFileHandle(entries.nextElement(), this, manager))
      result
    } catch {
      case e: IllegalStateException => throw new IOException("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
    }
  }

  @throws(classOf[IOException])
  def write(append: Boolean) = null

  def read = null
}