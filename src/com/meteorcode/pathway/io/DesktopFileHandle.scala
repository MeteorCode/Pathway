package com.meteorcode.pathway.io
import java.io.{
  File,
  InputStream,
  FileInputStream,
  FileOutputStream,
  IOException
}
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
 * @param logicalPath the logical path to the file in the fake filesystem
 * @param realPath the physical path to the actual FilSystem object
 * @param manager the instance of {@link com.meteorcode.pathway.io.ResourceManager ResourceManager} managing this FileHandle
 * @author Hawk Weisman
 */
class DesktopFileHandle protected[io](logicalPath: String,
                                      realPath: String,
                                      manager: ResourceManager) extends FileHandle(logicalPath, manager) {

  def this(physicalPath: String, manager: ResourceManager) = this (null, physicalPath, manager)

  private val file = new File(realPath)

  def exists: Boolean = file.exists
  def isDirectory: Boolean = file.isDirectory
  def writable: Boolean =  if (isDirectory) false else { if (exists) file.canWrite else file.createNewFile() }
  def physicalPath: String = realPath

  def read: InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    else new FileInputStream(file)
  }

  def list: List[FileHandle] = {
    if (isDirectory) {
      for (item <- file.list.toList) yield new DesktopFileHandle(item, path + "/" + item, manager)
    } else Collections.emptyList()
  }

  def write(append: Boolean) = if (writable) { new FileOutputStream(file, append) } else null
}
