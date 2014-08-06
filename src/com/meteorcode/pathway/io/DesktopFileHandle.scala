package com.meteorcode.pathway.io
import java.io.{
  File,
  InputStream,
  FileInputStream,
  FileOutputStream,
  IOException
}
import java.util
import java.util.List
import java.util.Collections
import scala.collection.JavaConversions._
/**
 * <p>A FileHandle into a regular file.</p>
 * <p>DON'T MAKE THESE - if you want to handle a file, please get it from an instance of {@link com.meteorcode.pathway.io.ResourceManager ResourceManager}.
 * The FileHandle system is supposed to allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only works
 * if you treat all files you have to access as instances of {@link com.meteorcode.pathway.io.FileHandle FileHandle}. If you ever refer to files as
 * DesktopFileHandle, ZipFileHandle, or JarFileHandle explicitly in your code, you are doing the Wrong Thing and negating a whole lot of time and effort I
 * put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.</p>
 *
 * @param pathTo the path to the file
 * @author Hawk Weisman
 */
class DesktopFileHandle protected[io](pathTo: String) extends FileHandle {
  protected val back = new File(pathTo)

  def path = back.getPath //TODO: Add code to coerce Windows paths into Unix-style paths as documented (This Sounds Like A Job For regular expressions)
  def file = this.back
  def exists(): Boolean = back.exists
  def isDirectory: Boolean = back.isDirectory
  def writeable(): Boolean = exists && back.canWrite

  def read(): InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    else new FileInputStream(back)
  }

  def list: util.List[FileHandle] = {
    if (isDirectory) {
      for (item <- file.list().toList) yield { // This is necessary so that yield() returns a List
        FileHandle(path + "/" + item)
      }
    } else Collections.emptyList()
  }

  def write(append: Boolean) = if (writeable()) { new FileOutputStream(back, append) } else null
}
