package com.meteorcode.pathway.io

import java.io.{OutputStream, File, IOException, InputStream}
import java.util.jar.JarFile
import java.util.Collections

import scala.util.{Try,Failure}
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.language.postfixOps

/**
 * A FileHandle into the top level of a Jar archive (treated as a directory).
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
 * @param manager An [[ResourceManager]] managing this FileHandle
 * @author Hawk Weisman
 * @since v2.0.0
 * @see [[ResourceManager]]
 * @see [[FileHandle]]
 * @see [[java.util.jar.JarEntry]]
 */
class JarFileHandle protected[io](
  virtualPath: String
, private[this] val back: File
, manager: Option[ResourceManager]
) extends FileHandle(virtualPath, manager) {

  protected[io] def this(fileHandle: FileHandle)
    = this( fileHandle.path
          , fileHandle.file
                      .getOrElse(throw new IOException(
                        "Could not create JarFileHandle from nonexistant file")
                      )
          , fileHandle.manager
          )

  protected[io] def this(virtualPath: String, fileHandle: FileHandle )
    = this( virtualPath
          , fileHandle.file
                      .getOrElse(throw new IOException(
                        "Could not create JarFileHandle from nonexistant file")
                      )
          , fileHandle.manager
          )

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
    = Try(Collections.list(new JarFile(back).entries)) map {
         _.asScala
          .withFilter { entry ⇒
            subdirRE findFirstIn entry.getName isDefined
          }
          .map { entry ⇒
            new JarEntryFileHandle(
                s"${this.path}${entry.getName withoutTrailingSlash}"
              , entry
              , this
              )
          }
      }

  override def write(append: Boolean): Option[OutputStream] = None

  override def read: Try[InputStream]
    = Failure(new IOException(
        s"Could not read from $path, file is a directory"))
}
