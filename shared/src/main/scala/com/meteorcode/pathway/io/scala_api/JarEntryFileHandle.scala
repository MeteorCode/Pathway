package com.meteorcode.pathway.io.scala_api

import java.io.{File, IOException, InputStream}
import java.util.Collections
import java.util.jar.{JarEntry, JarFile}
import java.util.zip.ZipException

import com.meteorcode.pathway.io.scala_api.ResourceManager

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.{Failure, Success, Try}

/**
 * A [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]] into an item within a Jar archive.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[ResourceManager.handle ResourceManager.handle()]]. The FileHandle system is supposed to
 * allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only
 * works if you treat all files you have to access as instances of [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]].
 * If you  ever refer to files as [[com.meteorcode.pathway.io.DesktopFileHandle DesktopFileHandle]],
 * [[com.meteorcode.pathway.io.scala_api.ZipFileHandle, ZipFileHandle]], or
 * [[JarFileHandle JarFileHandle]] explicitly in your code, you are doing the Wrong Thing and
 * negating a whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * Since Zip and Jar archives are unmodifyable by Pathway, we can assume that they will not change during the
 * game's execution. Therefore, a number of API methods for [[FileHandle]] can be memoized in Zip and Jar
 * [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]]s. While some external source may theoretically modify the
 * archives while the game is running, we don't support this behaviour as it would lead to issues even if we didn't
 * memoize some method calls (if somebody deletes an asset that various game components have a handle to, you're more or
 * less hosed anyway), so therefore, I think it's  reasonable to assume that nobody's gonna be futzing around with the
 * archives at runtime, and if they are, well, it's their own damn fault for breaking stuff.
 *
 * @param entry  the [[java.util.jar.JarEntry]] representing the file
 * @param parentJarfile a reference to the the [[java.util.jar.JarFile]] containing the
 *                     [[java.util.jar.JarEntry JarEntry]]  - this is necessary so that we can do things like
 *                     list the children of a directory in a Jar archive.
 * @param back the [[java.io.File]] that backs this [[FileHandle]]
 * @param manager the [[ResourceManager ResourceManager]] managing the virtual
 *                filesystem containing this [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]]
 * @author Hawk Weisman
 * @since v2.0.0
 * @see [[ResourceManager ResourceManager]]
 * @see [[ResourceManager ResourceManager]]
 */
class JarEntryFileHandle protected[io] (virtualPath: String,
                          private[this] val entry: JarEntry,
                          private[this] val parentJarfile: JarFileHandle,
                          private[this] val back: File,
                          manager: ResourceManager//,
                          //token: IOAccessToken
                          )
  extends JarFileHandle(virtualPath, back, manager
    //, token
  ) {

  protected[io] def this(virtualPath: String,
    entry: JarEntry, parent: JarFileHandle)
    = this(virtualPath, entry, parent,
            parent
              .file
              .getOrElse(throw new IOException(s"Could not create JarEntryFileHandle from nonexistant file $parent")),
            parent.manager//, token
          )
  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] lazy val physicalPath: Option[String]
    = parentJarfile.physicalPath map { s:String =>
      if (s.endsWith(".jar")) {
        s"$s/${entry.getName}"
      } else {
        s"$s${entry.getName}"
      }
    }

  /**
   * @return true if this file is a directory, false otherwise
   */
  override lazy val isDirectory: Boolean = entry.isDirectory

  /** Returns a stream for reading this file as bytes, or null if it is not readable (does not exist or is a directory).
    * @return a [[java.io.InputStream]] for reading the contents of this file, or null if it is not readable.
    * @throws IOException if something went wrong while reading from the file.
    */
  override def read: Try[InputStream] = this match {
    case _ if this.isDirectory => Failure(new IOException(s"Could not read from $path, file is a directory"))
    case _ if !this.exists     => Failure(new IOException(s"Could not read from $path, file does not exist"))
    case _                     => Try(new JarFile(back).getInputStream(entry)) recoverWith {
      case ze: ZipException => Failure(new IOException(s"Could not read file $path, a ZipException occured", ze))
      case se: SecurityException => Failure(new IOException(s"Could not read file $path, a Zip entry was improperly signed", se))
      case ise: IllegalStateException => Failure(new IOException(s"Could not read file $path appears to have been closed", ise))
      case e => Failure(new IOException(s"Could not read file $path, an unexpected error occured.", e))
    }
  }

  /**
   * Returns a list containing this [[FileHandle]]'s children.
   *
   * Since Zip and Jar file handles are not writable and therefore can be
   * guaranteed to not change during Pathway execution, we can memoize their
   * contents, meaning that we only ever have to perform this operation a
   * single time.
   *
   * @return a list containing [[FileHandle]]s to the contents of this
   *         [[FileHandle]], or an empty list if this file is not a
   *         directory or does not have contents.
   */
  override lazy val list: Try[Seq[FileHandle]]
    = if (isDirectory) {
        Try(Collections
          .list(new JarFile(back).entries)
          .asScala
          .withFilter { je: JarEntry =>
            je.getName
              .split("/")
              .dropRight(1)
              .lastOption
              .contains(entry.getName
                             .dropRight(1)) }
         .map { je: JarEntry =>
           new JarEntryFileHandle(
             s"${this.path}/${je.getName.split("/").last}",
             je,
             parentJarfile)
          })
      } else {
        Success(Seq[FileHandle]())
      }
}
