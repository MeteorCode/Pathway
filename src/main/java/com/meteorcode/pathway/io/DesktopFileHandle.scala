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
 *
 * [[com.meteorcode.pathway.io.ResourceManager.handle ResourceManager.handle()]]. The FileHandle system is supposed to
 * allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only
 * works if you treat all files you have to access as instances of [[com.meteorcode.pathway.io.FileHandle FileHandle]].
 * If you  ever refer to files as [[com.meteorcode.pathway.io.DesktopFileHandle DesktopFileHandle]],
 * [[com.meteorcode.pathway.io.ZipFileHandle, ZipFileHandle]], or
 * [[com.meteorcode.pathway.io.JarFileHandle JarFileHandle]] explicitly in your code, you are doing the Wrong Thing and
 * negating a whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * @param virtualPath
* the virtual path to the file in the fake filesystem
 * @param back a [[java.io.File]] representing the file in the filesystem
 * @param manager
 * An [[com.meteorcode.pathway.io.ResourceManager ResourceManager]] managing this FileHandle
 * @author Hawk Weisman
 */
class DesktopFileHandle (virtualPath: String,
                         realPath: String,
                         private[this] val back: File,
                         manager: ResourceManager//,
                         //token: IOAccessToken
                          ) extends FileHandle(virtualPath, manager//, token
) {

  def this(virtualPath: String,
           realPath: String,
           manager: ResourceManager//,
           //token:IOAccessToken
            ) = this (virtualPath, realPath, new File(realPath), manager//, token
  )
  //def this(physicalPath: String, manager: ResourceManager) = this(null, physicalPath, manager)

  /**
   * Returns the [[java.io.File]] backing this file handle.
   * @return a [[java.io.File]] that represents this file handle, or null if this file is inside a Jar or Zip archive.
   */
  override val file = back

  /** Returns a buffered stream for reading this file as bytes.
    * @throws IOException if the file does not exist or is a directory.
    */
  override def read: InputStream = if (!exists || isDirectory) null else new FileInputStream(back)

  /** @return true if the file exists, false if it does not */
  override def exists: Boolean = back.exists

  /** Returns true if this file is a directory.
    *
    * Note that this may return false if a directory exists but is empty.
    * This is Not My Fault, it's [[java.io.File]] behaviour.
    *
    * @return true if this file is a directory, false otherwise
    * */
  override lazy val isDirectory: Boolean = back.isDirectory

  override def length = if (isDirectory) 0 else back.length

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
  override def list: java.util.List[FileHandle] = {
    if (isDirectory) {
      for (item <- back.list.toList)
        yield item match {
        case isArchiveRE(dirname,kind) => kind match {
          case ".jar" => new JarFileHandle("/", new File(physicalPath + "/" + item), this.manager)
          case ".zip" => new ZipFileHandle("/", new File(physicalPath + "/" + item), this.manager)
        }
        case _ => new DesktopFileHandle(path + "/" + item, physicalPath + "/" + item, manager//, this.token
        )
      }
    } else Collections.emptyList()
  }

  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  override lazy val physicalPath: String = realPath.replace('/', File.separatorChar)

  override def delete = if(writable && exists) back.delete else false

  /**
   * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
   * @return an [[java.io.OutputStream]] for writing to this file, or null if this file is not writable.
   */
  override def write(append: Boolean) = if (writable) {
    new FileOutputStream(back, append)
  } else null

  /**
   * @throws java.io.IOException if something went wrong while determining if this FileHandle is writable.
   * @return true if this file is writable, false if it is not
   */
  @throws(classOf[IOException])
  override def writable: Boolean = {
    if (manager.isPathWritable(this.path)) {
      if (isDirectory) false
      else if (exists)
        back.canWrite
      else try {
        back.createNewFile()
        } catch {
          case up: IOException => if (up.getMessage == "Permission denied") false else throw up
        }
    } else false
  }
}
