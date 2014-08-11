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


class JarFileHandle (virtualPath: String,
                     private val back: File,
                     manager: ResourceManager) extends FileHandle(virtualPath, manager) {
  // I also hate java.util.jar
  protected[io] var jarfile = new JarFile(back)

  def this(virtualPath: String, fileHandle: FileHandle) = this(virtualPath, fileHandle.file, fileHandle.manager)

  /**
   * @return the [[java.io.File]] backing this file handle, or null if this file is inside a Jar or Zip archive.
   */
  protected[io] def file = back

  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  protected[io] def physicalPath = back.getPath

  /**
   * @return true if this FileHandle represents something that exists, false if it does not exist.
   */
  def exists: Boolean = back.exists

  /**
   * @return true if this file is a directory, false otherwise
   */
  def isDirectory: Boolean = true

  /**
   * @return true if this FileHandle can be written to, false if it cannot.
   */
  def writable = false

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
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

  /**
   * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
   * @throws java.io.IOException
   * @return an [[java.io.OutputStream]] for writing to this file, or null if this file is not writable.
   */
  @throws(classOf[IOException])
  def write(append: Boolean) = null

  /**
   * @return a [[java.io.InputStream]] for reading this file, or null if the file does not exist or is a directory.
   */
  def read: InputStream = null
}