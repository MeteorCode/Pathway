package com.meteorcode.pathway.io

import java.io.{File, IOException, InputStream}
import java.util.Collections
import java.util.zip.{ZipEntry, ZipException, ZipFile}

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.language.postfixOps
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * A [[FileHandle]] into an entry in a Zip archive file.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
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
 * @param entry  the [[java.util.zip.ZipEntry]] representing the file
 * @param parentZipfile a reference to the the [[java.util.zip.ZipFile]]
 *                      containing the ZipEntry - this is necessary so that
 *                      we can do things like list the children of a directory
 *                      in a Zip archive.
 * @param back the [[java.io.File]] that backs this FileHandle
 * @param manager the [[ResourceManager]] managing the virtual filesystem
 *                containing this FileHandle
 * @author Hawk Weisman
 * @since v2.0.0
 * @see [[ResourceManager]]
 * @see [[FileHandle]]
 * @see [[java.util.zip.ZipEntry]]
 */
class ZipEntryFileHandle protected[io] (
    virtualPath: String,
    private[this] val entry: ZipEntry,
    private[this] val parentZipfile: ZipFileHandle,
    private[this] val back: File,
    manager: Option[ResourceManager]
) extends ZipFileHandle(virtualPath, back, manager) {

  protected[io] def this(
    virtualPath: String,
    entry: ZipEntry,
    parent: ZipFileHandle)
    = this( virtualPath, entry, parent,
      parent.file
            .getOrElse(throw new IOException(
              "Could not create ZipEntryFileHandle from nonexistant file" +
              parent)),
      parent.manager)

  override protected[io] lazy val physicalPath: Option[String]
    = parentZipfile.physicalPath map { s: String ⇒
      if (s.endsWith(".zip")) {
        s"$s/${entry.getName}"
      } else {
        s"$s${entry.getName}"
      }
    }

  override lazy val isDirectory = entry.isDirectory

  override def read: Try[InputStream] = this match {
    case _ if this.isDirectory ⇒ Failure(new IOException(
      s"Could not read from $path, file is a directory"))
    case _ if !this.exists     ⇒ Failure(new IOException(
      s"Could not read from $path, file does not exist"))
    case _  ⇒ Try(new ZipFile(back).getInputStream(entry)) recoverWith {
      case ze: ZipException ⇒ Failure(new IOException(
        s"Could not read file $path, a ZipException occured", ze))
      case se: SecurityException ⇒ Failure(new IOException(
        s"Could not read file $path, a Zip entry was improperly signed", se))
      case ise: IllegalStateException ⇒ Failure(new IOException(
        s"Could not read file $path appears to have been closed", ise))
      case NonFatal(e) ⇒ Failure(new IOException(
        s"Could not read file $path, an unexpected error occured.", e))
    }
  }

  override lazy val list: Try[Seq[FileHandle]]
    = if (isDirectory) {
        Try(
          Collections.list(new ZipFile(back).entries)
            .asScala
            .withFilter { ze: ZipEntry ⇒
              ze.getName.parent contains (entry.getName dropRight 1)
            } map { ze: ZipEntry ⇒
              new ZipEntryFileHandle(
                s"${this.path}/${ze.getName split '/' last}"
              , ze
              , parentZipfile
              )
            })
      } else {
        Success(Seq[FileHandle]())
      }
}
