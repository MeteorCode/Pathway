package com.meteorcode.pathway.io

import java.io.{
File,
InputStream,
IOException
}
import java.util
import java.util.jar.JarFile
import java.util.Collections

import scala.util.{Try,Success,Failure}
import scala.collection.JavaConversions._
/**
 * A [[FileHandle]] into the top level of a Jar archive
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[com.meteorcode.pathway.io.ResourceManager.handle ResourceManager.handle()]]. The FileHandle system is supposed to
 * allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only
 * works if you treat all files you have to access as instances of [[com.meteorcode.pathway.io.FileHandle FileHandle]].
 * If you  ever refer to files as [[com.meteorcode.pathway.io.DesktopFileHandle DesktopFileHandle]],
 * [[com.meteorcode.pathway.io.ZipFileHandle, ZipFileHandle]], or
 * [[com.meteorcode.pathway.io.JarFileHandle JarFileHandle]] explicitly in your code, you are doing the Wrong Thing and
 * negating a whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * Since Zip and Jar archives are unmodifyable by Pathway, we can assume that they will not change during the
 * game's execution. Therefore, a number of API methods for [[FileHandle]] can be memoized in Zip and Jar
 * [[FileHandle]]s. While some external source may theoretically modify the archives while the game is running,
 * we don't support this behaviour as it would lead to issues even if we didn't memoize some method calls (if somebody
 * deletes an asset that various game components have a handle to, you're hosed anyway), so therefore, I think it's
 * reasonable to assume that nobody's gonna be futzing around with the archives at runtime, and if they are, well,
 * it's their own damn fault for breaking stuff.
 *
 * @param virtualPath The virtual path to the object this [[FileHandle]] represents
 * @param back A [[java.io.File]] representing the Jar archive to handle.
 * @param manager the ResourceManager managing this FileHandle
 * @author Hawk Weisman
 * @see [[com.meteorcode.pathway.io.ResourceManager ResourceManager]]
 */
class JarFileHandle (virtualPath: String,
                     private val back: File,
                     manager: ResourceManager//,
                     //token: IOAccessToken
                      ) extends FileHandle(virtualPath, manager//,token
                      ) {
  // I also hate java.util.jar
  protected[io] var jarfile = new JarFile(back)

  def this(virtualPath: String,
          fileHandle: FileHandle//,
          //token: IOAccessToken
            ) = this(virtualPath, fileHandle.file, fileHandle.manager//, token
  )

  /**
   * @return the [[java.io.File]] backing this file handle, or null if this file is inside a Jar or Zip archive.
   */
  override protected[io] val file = back

  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] lazy val physicalPath = back.getPath

  /**
   * @return true if this FileHandle represents something that exists, false if it does not exist.
   */
  override def exists: Boolean = back.exists

  /**
   * @return true if this file is a directory, false otherwise
   */
  override lazy val isDirectory: Boolean = true

  /**
   * @return true if this FileHandle can be written to, false if it cannot.
   */
  override val writable = false

  override lazy val length = if (isDirectory) 0 else back.length

  override def delete = if(writable && exists) back.delete else false

  /**
   * Returns a list containing this [[FileHandle]]'s children.
   *
   * Since Zip and Jar file handles are not writable and therefore can be guaranteed to not change during
   * Pathway execution, we can memoize their contents, meaning that we only ever have to perform this operation
   * a single time.
   *
   * @return a list containing [[FileHandles]] to the contents of [[FileHandles]], or an empty list if this
   *         file is not a directory or does not have contents.
   */
  @throws(classOf[IOException])
  override lazy val list: util.List[FileHandle] = {
    val result = Try(
      Collections.list(jarfile.entries) // TODO: memoize this?
        filter ( subdirRE findFirstIn _.getName isDefined )
        map ( (e) => new JarEntryFileHandle( this.path + trailingSlash(e.getName), e, this) )
    )
    jarfile = new JarFile(back) // reset the file
    result match {
      case Failure(e: IllegalStateException) =>
        throw new IOException("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
      case Failure(up) => throw up
      case Success(list) => list
    }
  }

  /**
   * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
   * @throws java.io.IOException if something went wrong while writing
   * @return an [[java.io.OutputStream]] for writing to this file, or null if this file is not writable.
   */
  @throws(classOf[IOException])
  override def write(append: Boolean) = null

  /**
   * @return a [[java.io.InputStream]] for reading this file, or null if the file does not exist or is a directory.
   */
  override def read: InputStream = null
}
