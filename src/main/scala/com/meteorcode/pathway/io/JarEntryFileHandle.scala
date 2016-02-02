package com.meteorcode.pathway.io

import java.io.{File, IOException, InputStream}
import java.util.Collections
import java.util.jar.{JarEntry, JarFile}
import java.util.zip.ZipException

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

/**
 * A [[FileHandle]] into an entry in a Jar archive file.
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
 * @param manager An [[ResourceManager]] managing
 *                this FileHandle
 * @author Hawk Weisman
 * @since v2.0.0
 * @see [[ResourceManager]]
 * @see [[FileHandle]]
 * @see [[java.util.jar.JarEntry]]
 */
class JarEntryFileHandle protected[io](
  virtualPath: String
, private[this] val entry: JarEntry
, private[this] val parentJarfile: JarFileHandle
, private[this] val back: File
, manager: Option[ResourceManager]
) extends JarFileHandle(virtualPath, back, manager) {

  protected[io] def this(virtualPath: String,
    entry: JarEntry, parent: JarFileHandle)
    = this( virtualPath
          , entry
          , parent
          , parent.file
                  .getOrElse(throw new IOException(
                    s"Could not create JarEntryFileHandle from nonexistant"
                    + s" file $parent"))
          , parent.manager )

  override protected[io] lazy val physicalPath: Option[String]
    = parentJarfile.physicalPath map { s: String ⇒
      if (s.endsWith(".jar")) {
        s"$s/${entry.getName}"
      } else {
        s"$s${entry.getName}"
      }
    }

  override lazy val isDirectory: Boolean = entry isDirectory

  override def read: Try[InputStream] = this match {
    case _ if this.isDirectory ⇒ Failure(new IOException(
      s"Could not read from $path, file is a directory"))
    case _ if !this.exists     ⇒ Failure(new IOException(
      s"Could not read from $path, file does not exist"))
    case _ ⇒ Try(new JarFile(back).getInputStream(entry)) recoverWith {
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
          Collections.list(new JarFile(back).entries).asScala
            .withFilter { je: JarEntry ⇒
              je.getName.parent contains (entry.getName dropRight 1)
            } map { je: JarEntry ⇒
               new JarEntryFileHandle(
                  s"${this.path}/${je.getName split '/' last}"
                , je
                , parentJarfile)
              })
      } else {
        Success(Seq[FileHandle]())
      }
}
