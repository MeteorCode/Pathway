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


class JarFileHandle protected[io] (private val pathTo: String,
                                   private val back: File,
                                   manager: ResourceManager)
  extends FileHandle(manager) {
  // I also hate java.util.jar
  private val jarfile = new JarFile(file)

  protected[io] def this(fileHandle: FileHandle) = this(fileHandle.path, fileHandle.file, fileHandle.manager)

  def file = this.back
  def path = back.getPath
  def exists: Boolean = file.exists
  def isDirectory: Boolean = true
  def writeable = false

  @throws(classOf[IOException])
  def list: List[FileHandle] = {
    var result = new ArrayList[FileHandle]
    try {
      val entries = jarfile.entries

     while (entries.hasMoreElements) result.add( new JarEntryFileHandle(entries.nextElement(), jarfile, path, manager) )
     result
    } catch {
      case e: IllegalStateException => throw new IOException ("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
    }
  }

  @throws(classOf[IOException])
  def write(append: Boolean) = null
  def read = null
}