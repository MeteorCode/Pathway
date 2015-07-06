package com.meteorcode.pathway.io.scala_api

import java.io.{
File,
InputStream,
OutputStream,
FileInputStream,
FileOutputStream,
IOException
}
import java.util.Collections
import com.meteorcode.pathway.io.isArchiveRE
import com.meteorcode.pathway.io.scala_api.ResourceManager

import scala.collection.JavaConversions._

import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * A FileHandle into a regular file.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 *
 * [[ResourceManager.handle ResourceManager.handle()]]. The FileHandle system is supposed to
 * allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only
 * works if you treat all files you have to access as instances of [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]].
 * If you  ever refer to files as [[com.meteorcode.pathway.io.scala_api.FilesystemFileHandle FilesystemFileHandle]],
 * [[com.meteorcode.pathway.io.scala_api.ZipFileHandle, ZipFileHandle]], or
 * [[JarFileHandle JarFileHandle]] explicitly in your code, you are doing the Wrong Thing and
 * negating a whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * @param virtualPath
* the virtual path to the file in the fake filesystem
 * @param back a [[java.io.File]] representing the file in the filesystem
 * @param manager
 * An [[ResourceManager ResourceManager]] managing this FileHandle
 * @author Hawk Weisman
 * @since v2.0.0
 */
class FilesystemFileHandle (
  virtualPath: String,
  realPath: String,
  private[this] val back: File,
  manager: ResourceManager//,
  //token: IOAccessToken
  ) extends FileHandle(virtualPath, manager) {

  require(realPath != "", "Physical path cannot be empty.")

  def this(virtualPath: String, realPath: String, manager: ResourceManager)
    = this (virtualPath, realPath, new File(realPath), manager)
  //def this(physicalPath: String, manager: ResourceManager) = this(null, physicalPath, manager)

  /**
   * Returns the [[java.io.File]] backing this file handle.
   * @return a [[java.io.File]] that represents this file handle, or null if this file is inside a Jar or Zip archive.
   */
  override val file = Some(back)

  /** Returns a buffered stream for reading this file as bytes.
    * @throws IOException if the file does not exist or is a directory.
    */
  override def read: Try[InputStream] = if (!exists || isDirectory) {
    Failure(new IOException(s"FileHandle $path is not readable."))
  } else Try(new FileInputStream(back))

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

  override def length: Long = if (isDirectory) 0 else back.length

  /**
   * @return a list containing FileHandles to the contents of FileHandle,
   *          or an empty list if this file is not a directory or does
   *          not have contents.
   */
  override def list: Try[Seq[FileHandle]] = Try(
    if (isDirectory) {
      for (item <- back.list) yield item match {
        case isArchiveRE(_, archType) =>
          val file = new File(s"$assumePhysPath/$item")
          archType match {
            case ".jar" => new JarFileHandle("/", file, this.manager)
            case ".zip" => new ZipFileHandle("/", file, this.manager)
          }
        case _ =>
          new FilesystemFileHandle(
            s"$path/$item", s"$assumePhysPath/$item", manager)
      }
    } else Nil)

  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  override lazy val physicalPath: Some[String]
    = Some(realPath.replace('/', File.separatorChar))

  override def delete: Boolean
    = if(writable && exists) back.delete else false

  /**
   * @param append If false, this file will be overwritten if it exists,
   *               otherwise it will be appended.
   * @return a [[scala.Option Option]] containing a  [[java.io.OutputStream]]
   *         for writing to this file, or [[scala.None None]] if this file is
   *          not writable.
   */
  override def write(append: Boolean): Option[OutputStream]
    = if (writable) { Some(new FileOutputStream(back, append)) } else None

  /**
   * @throws java.io.IOException if something went wrong while determining if this FileHandle is writable.
   * @return true if this file is writable, false if it is not
   */
  @throws(classOf[IOException])
  override def writable: Boolean = { // todo: refactor
    if (manager.isPathWritable(this.path)) {
      if (isDirectory) false
      else if (exists)
        back.canWrite
      else try {
        back.createNewFile()
        } catch {
          case up: IOException => if (up.getMessage == "Permission denied") false else throw up
          case NonFatal(e)     =>
            throw new IOException(s"Could not create FileHandle $this, an exception occured.", e)
        }
    } else false
  }
}
