package com.meteorcode.pathway.io
package scala_api

import java.io.{File, IOException, InputStream}
import java.util.Collections
import java.util.zip.{ZipEntry, ZipException, ZipFile}

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.{Failure, Success, Try}

/**
 * A FileHandle into a file or directory within a zip archive.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[com.meteorcode.pathway.io.ResourceManager.handle ResourceManager.handle()]]. The FileHandle system is supposed to
 * allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only
 * works if you treat all files you have to access as instances of [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]].
 * If you  ever refer to files as [[com.meteorcode.pathway.io.DesktopFileHandle DesktopFileHandle]],
 * [[com.meteorcode.pathway.io.scala_api.ZipFileHandle, ZipFileHandle]], or
 * [[JarFileHandle JarFileHandle]] explicitly in your code, you are doing the Wrong Thing and
 * negating a whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * @param entry  the [[java.util.zip.ZipEntry]] representing the file
 * @param parentZipfile a reference to the the [[java.util.zip.ZipFile]] containing the ZipEntry - this is necessary so that
 *               we can do things like list the children of a directory in a Zip archive.
 * @param back the [[java.io.File]] that backs this FileHandle
 * @param manager the ResourceManager managing the virtual filesystem containing this FileHandle
 * @author Hawk Weisman
 * @see [[com.meteorcode.pathway.io.ResourceManager ResourceManager]]
 */
class ZipEntryFileHandle (virtualPath: String,
                          private val entry: ZipEntry,
                          private val parentZipfile: ZipFileHandle,
                          private val back: File,
                          manager: ResourceManager//,
                          //token: IOAccessToken
                          )
  extends ZipFileHandle(virtualPath, back, manager//, token
  ) {

  def this(virtualPath: String,
           entry: ZipEntry,
           parent: ZipFileHandle//,
           //token: IOAccessToken
           ) = this(virtualPath, entry, parent, parent.file, parent.manager//, token
           )

  /**
   * @return  the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] lazy val physicalPath = if (parentZipfile.physicalPath.getOrElse("").endsWith(".zip")) {
    parentZipfile.physicalPath + "/" + entry.getName
  } else {
    parentZipfile.physicalPath + entry.getName
  }

  /**
   * @return true if this file is a directory, false otherwise
   */
  override lazy val isDirectory = entry.isDirectory

  /** Returns a stream for reading this file as bytes, or null if it is not readable (does not exist or is a directory).
    * @return a [[java.io.InputStream]] for reading the contents of this file, or null if it is not readable.
    * @throws IOException if something went wrong while reading from the file.
    */
  override def read: Try[InputStream] = this match {
    case _ if this.isDirectory => Failure(new IOException(s"Could not read from $path, file is a directory"))
    case _ if !this.exists     => Failure(new IOException(s"Could not read from $path, file does not exist"))
    case _                     => Try(zipfile.getInputStream(entry)) recoverWith {
      case ze: ZipException => Failure(new IOException(s"Could not read file $path, a ZipException occured", ze))
      case se: SecurityException => Failure(new IOException(s"Could not read file $path, a Zip entry was improperly signed", se))
      case ise: IllegalStateException => Failure(new IOException(s"Could not read file $path appears to have been closed", ise))
      case e => Failure(new IOException(s"Could not read file $path, an unexpected error occured.", e))
    }
  }

  /**
   * Returns a list containing this [[FileHandle]]'s children.
   *
   * Since Zip and Jar file handles are not writable and therefore can be guaranteed to not change during
   * Pathway execution, we can memoize their contents, meaning that we only ever have to perform this operation
   * a single time.
   *
   * @return a list containing [[FileHandle]]s to the contents of this [[FileHandle]], or an empty list if this
   *         file is not a directory or does not have contents.
   */
  override lazy val list: Try[Seq[FileHandle]] = if (isDirectory) {
    Try(
      Collections.list(zipfile.entries).asScala
        withFilter ( _.getName.split("/").dropRight(1).lastOption contains entry.getName.dropRight(1) )
        map ( (e) => new ZipEntryFileHandle(s"${this.path}/${e.getName.split("/").last}", e, parentZipfile) )
    )
  } else Success(Seq[FileHandle]())
}