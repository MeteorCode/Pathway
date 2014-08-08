package com.meteorcode.pathway.io

import java.io.{
File,
InputStream,
FileInputStream,
FileOutputStream,
IOException
}
import java.util.Collections
import scala.collection.JavaConversions._

/**
 * A FileHandle into a regular file.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[com.meteorcode.pathway.io.ResourceManager.handle()]]. The FileHandle system is supposed to allow you to treat files in
 * zip/jar archives as though they were on the filesystem as regular files, but this only works if you treat all files
 * you have to access as instances of [[com.meteorcode.pathway.io.FileHandle]]. If you  ever refer to files as
 * [[com.meteorcode.pathway.io.DesktopFileHandle]], [[com.meteorcode.pathway.io.ZipFileHandle]], or
 * [[com.meteorcode.pathway.io.JarFileHandle]] explicitly in your code, you are doing the  Wrong Thing and negating a
 * whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * @param logicalPath
 * the logical path to the file in the fake filesystem
 * @param realPath
 * the physical path to the actual FilSystem object
 * @param manager
 * An [[com.meteorcode.pathway.io.ResourceManager]] managing this FileHandle
 * @author Hawk Weisman
 */
class DesktopFileHandle protected[io](logicalPath: String,
                                      realPath: String,
                                      manager: ResourceManager) extends FileHandle(logicalPath, manager) {

  private val back = new File(realPath)

  //def this(physicalPath: String, manager: ResourceManager) = this(null, physicalPath, manager)

  def file = back

  def read: InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    else new FileInputStream(back)
  }

  def exists: Boolean = back.exists

  def isDirectory: Boolean = back.isDirectory

  def list: java.util.List[FileHandle] = {
    if (isDirectory) {
      for (item <- back.list.toList) yield new DesktopFileHandle(path + "/" + item, physicalPath + "/" + item, manager)
    } else Collections.emptyList()
  }

  def physicalPath: String = realPath

  def write(append: Boolean) = if (writable) {
    new FileOutputStream(back, append)
  } else null

  def writable: Boolean = {
    if (isDirectory)
      false
    else if (exists)
      back.canWrite
    else
      try {
        back.createNewFile()
      } catch {
        case up: IOException => if (up.getMessage == "Permission denied") false else throw up
      }
  }
}
