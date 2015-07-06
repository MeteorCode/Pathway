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
 * A [[FileHandle]] into a regular file.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 *
 * [[scala_api.ResourceManager.handle ResourceManager.handle()]].
 * The FileHandle system is intended to allow you to treat exotic resources,
 * such as files in zip/jar  archives or resources accessed over the netweork,
 * as though they were on the filesystem as regular files, but this only works
 * if you treat all files you  have to access as instances of
 * [[scala_api.FileHandle FileHandle]]. If you  ever refer to files as
 * [[scala_api.FilesystemFileHandle FilesystemFileHandle]],
 * [[scala_api.ZipFileHandle ZipFileHandle]], or
 * [[scala_api.JarFileHandle JarFileHandle]] explicitly in your code, you are
 * doing the Wrong Thing and  negating a whole lot of time and effort I put into
 * this system. So don't do that.
 *
 * To reiterate, do NOT call the constructor for this
 *
 * @param virtualPath the virtual path to the file in the fake filesystem
 * @param back a [[java.io.File]] representing the file in the filesystem
 * @param manager An [[scala_api.ResourceManager ResourceManager]] managing
 *                this FileHandle
 * @author Hawk Weisman
 * @see [[scala_api.ResourceManager ResourceManager]]
 * @see [[scala_api.FileHandle FileHandle]]
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

  override val file = Some(back)

  override def read: Try[InputStream]
    = if (!exists || isDirectory) {
      Failure(new IOException(s"FileHandle $path is not readable."))
    } else Try(new FileInputStream(back))

  override def exists: Boolean = back.exists

  override lazy val isDirectory: Boolean
    = back.isDirectory

  override def length: Long
    = if (isDirectory) 0 else back.length

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

  override lazy val physicalPath: Some[String]
    = Some(realPath.replace('/', File.separatorChar))

  override def delete: Boolean
    = if(writable && exists) back.delete else false

  override def write(append: Boolean): Option[OutputStream]
    = if (writable) { Some(new FileOutputStream(back, append)) } else None

  @throws[IOException]("if something unexpected went wrong")
  override def writable: Boolean // TODO: should this be Try[Boolean]?
    = manager.isPathWritable(this.path) && // is the path writable at fs level?
      !isDirectory && // directories are not writable
      (back.canWrite || // file exists and is writable, or...
        (Try(back.createNewFile()) match { // try to create the file
          case Failure(i: IOException)  // if not permitted to write, that's OK
            if i.getMessage == "Permission denied" => false
          case Failure(NonFatal(e)) => throw new IOException(
              s"Could not create FileHandle $this, an exception occured.", e)
          case Success(result) => result
        })
      )
}
