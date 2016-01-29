package com.meteorcode.pathway.io

import java.io.{
  File,
  InputStream,
  OutputStream,
  FileInputStream,
  FileOutputStream,
  IOException
}
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * A [[FileHandle]] into a regular file.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[ResourceManager.handle ResourceManager.handle()]].
 *
 * The FileHandle system is intended to allow you to treat exotic resources,
 * such as files in zip/jar  archives or resources accessed over the netweork,
 * as though they were on the filesystem as regular files, but this only works
 * if you treat all files you  have to access as instances of [[FileHandle]]].
 * If you  ever refer to files as [[FilesystemFileHandle]],  [[ZipFileHandle]],
 * or [[JarFileHandle]] explicitly in your code, you are doing the Wrong Thing
 * and  negating a whole lot of time and effort I put into this system.
 * So don't do that.
 *
 * To reiterate, do NOT call the constructor for this
 *
 * @param virtualPath the virtual path to the file in the fake filesystem
 * @param back a [[java.io.File]] representing the file in the filesystem
 * @param manager An [[ResourceManager ResourceManager]] managing
 *                this FileHandle
 * @author Hawk Weisman
 * @see [[ResourceManager ResourceManager]]
 * @see [[FileHandle]]
 * @since v2.0.0
 */
class FilesystemFileHandle (
  virtualPath: String
, realPath: String
, private[this] val back: File
, manager: Option[ResourceManager]
) extends FileHandle(virtualPath, manager) {

  require(realPath != "", "Physical path cannot be empty.")

  def this(virtualPath: String, realPath: String, manager: ResourceManager)
    = this (virtualPath, realPath, new File(realPath), Some(manager))

  def this( virtualPath: String, realPath: String
          , manager: Option[ResourceManager])
    = this (virtualPath, realPath, new File(realPath), manager)

  override val file = Some(back)

  @inline
  @throws[IOException]
  private[this] def getManager
    = this.manager
          .getOrElse(throw new IOException(
            "FATAL: ResourceManager instance required!"))

  override def read: Try[InputStream]
    = if (!exists || isDirectory) {
        Failure(new IOException(s"FileHandle $path is not readable."))
      } else Try(new FileInputStream(back))

  override def exists: Boolean = back.exists

  override lazy val isDirectory: Boolean
    = back.isDirectory

  override def length: Long
    = if (isDirectory) 0 else back.length

  override def list: Try[Seq[FileHandle]]
    = Try(if (isDirectory) {
        back.list map {
          case isArchiveRE(name, ".jar") ⇒
            new JarFileHandle( "/"
                             , new File(s"$assumePhysPath/$name.jar")
                             , manager)
          case isArchiveRE(name, ".zip") ⇒
            new ZipFileHandle( "/"
                             , new File(s"$assumePhysPath/$name.zip")
                             , manager)
          case item ⇒
            new FilesystemFileHandle( s"$path/$item"
                                    , s"$assumePhysPath/$item"
                                    , manager)
        }
      } else { Seq() })

  override lazy val physicalPath: Some[String]
    = Some(realPath.replace('/', File.separatorChar))

  override def delete: Boolean
    = if (writable && exists) back.delete else false

  override def write(append: Boolean): Option[OutputStream]
    = if (writable) { Some(new FileOutputStream(back, append)) } else None

  @throws[IOException]("if something unexpected went wrong")
  override def writable: Boolean // TODO: should this be Try[Boolean]?
    = manager.exists(_ isPathWritable this.path) && // is the path writable at fs level?
      !isDirectory && // directories are not writable
      (back.canWrite || // file exists and is writable, or...
        (Try(back.createNewFile()) match { // try to create the file
          case Failure(i: IOException)  // if not permitted to write, that's OK
            if i.getMessage == "Permission denied" ⇒ false
          case Failure(NonFatal(e)) ⇒ throw new IOException(
              s"Could not create FileHandle $this, an exception occured.", e)
          case Success(result) ⇒ result
        })
      )
}
