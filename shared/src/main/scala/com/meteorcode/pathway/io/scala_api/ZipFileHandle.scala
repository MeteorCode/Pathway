package com.meteorcode.pathway.io.scala_api

import java.io.{OutputStream, File, IOException, InputStream}
import java.util.zip.ZipFile
import java.util.Collections

import com.meteorcode.pathway.io.scala_api.ResourceManager
import com.meteorcode.pathway.io.{subdirRE,trailingSlash}

import scala.util.{Try,Failure}
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.language.postfixOps

/**
 * A [[FileHandle]] into an entry in a Zip archive file.
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
 * @param virtualPath The virtual path to the object this FileHandle represents
 * @param back A [[java.io.File]] representing the Zip archive to handle.
 * @param manager the ResourceManager managing this FileHandle
 * @author Hawk Weisman
 * @see [[scala_api.ResourceManager ResourceManager]]
 * @see [[scala_api.FileHandle FileHandle]]
 * @since v2.0.0
 */
class ZipFileHandle protected[io] (virtualPath: String,
                     private[this] val back: File,
                     manager: ResourceManager//,
                     //token: IOAccessToken
                      ) extends FileHandle(virtualPath, manager//, token
) {
  /*
  Let's take a moment to discuss how Java's Zip API is Not My Favourite Thing.

  In the API, a Zip file is represented by a ZipFile object. Files WITHIN that
  Zip file, however, are represented by ZipEntries. Unfortunately, the only way
  to coerce Java to give you an InputStream for any given file in a Zip is to
  call a method on the parent ZipFile, passing the ZipEntry as an argument.
  This means that if you have a ZipEntry that you want to read, you also need
  a pointer to the ZipFile it lives in if you want to do anything useful with it.
  As any sane person can tell, this is not the right thing.

  This incredibly brain-dead design is why we have to have ZipFileHandles AND
  ZipEntryFileHandles - we can't treat ZipFiles as though they were regular
  files, since we want to allow them to be accessed transparently as though
  they were directories - but we also can't handle files within those Zip files
  using FileHandles OR ZipFileHandles.

  To add insult to injury, I'm pretty sure this idiocy isn't the Zip file
  format's fault - Python's 'zipfile' module basically allows you to treat
  files within a Zip archive transparently as though they were regular files in
  the filesystem, because (unlike java.util.zip), it wasn't designed by a
  committee of people dead-set on making my day miserable. Also, documentation
  for Python's module is much better.

  In short, I hate java.util.zip.
  */

  protected[io] def this(fileHandle: FileHandle) = this(
    fileHandle.path,
    fileHandle.file
      .getOrElse(throw new IOException(
        "Could not create ZipFileHandle from nonexistant file")),
    fileHandle.manager)

  protected[io] def this(virtualPath: String, fileHandle: FileHandle ) = this(
    virtualPath,
    fileHandle.file
      .getOrElse(throw new IOException(
        "Could not create ZipFileHandle from nonexistant file")),
    fileHandle.manager)

  override protected[io] def file: Option[File] = Some(back)

  override protected[io] def physicalPath: Option[String] = Some(back.getPath)

  override def exists: Boolean = back.exists

  override lazy val isDirectory: Boolean
    = true   // Remember, we are pretending that zips are directories

  override val writable: Boolean
    = false // Zips can never be written to (at least by java.util.zip)

  override lazy val length: Long
    = if (isDirectory) 0 else back.length

  override def delete: Boolean
    = if(writable && exists) back.delete else false

  /** @inheritdoc
   *
   * Since Zip and Jar file handles are not writable and therefore can be
   * guaranteed to not change during Pathway execution, we can memoize their
   * contents, meaning that we only ever have to perform this operation a
   * single time.
   *
   * @return @inheritdoc
   */
  override lazy val list: Try[Seq[FileHandle]]
    = Try(Collections.list(new ZipFile(back).entries)) map {
         _.asScala
          .withFilter { entry =>
            subdirRE findFirstIn entry.getName isDefined
          }
          .map { entry =>
            new ZipEntryFileHandle(
              s"${this.path}${trailingSlash(entry.getName)}",
              entry,
              this
            )
          }
      }

  override def write(append: Boolean): Option[OutputStream] = None

  override def read: Try[InputStream]
    = Failure(new IOException (s"Could not read from $path, file is a directory"))
}
