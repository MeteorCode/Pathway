package com.meteorcode.pathway.io.scala_api

import java.io.{BufferedInputStream, BufferedOutputStream, File, IOException, InputStream, OutputStream}
import java.nio.charset.Charset

import com.meteorcode.pathway.io.ResourceManager

import scala.io.Source
import scala.util.{Try,Success,Failure}

protected object IOAccessToken

/**
 * An abstraction wrapping a file in the filesystem.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 */
abstract class FileHandle(protected val virtualPath: String,
                          protected[io] var manager: ResourceManager//,
                          //protected val token: IOAccessToken // todo: implement
                           ) {
  // TODO: Implement
  //if (token != FileHandle.correctToken) { // validate that the access token is from a valid source
  //  throw new SecurityException("Could not create FileHande with bad security access token.")
  //}

  /** Returns true if the file exists. */
  def exists: Boolean

  /** Returns true if this file is a directory.
    *
    * Note that this may return false if a directory exists but is empty.
    * This is Not My Fault, it's [[java.io.File]] behaviour.
    *
    * @return true if this file is a directory, false otherwise
    * */
  def isDirectory: Boolean

  /** Returns true if this FileHandle represents something that can be written to */
  def writable: Boolean

  /** Returns the virtual path to this FileHandle.
    *
    * All paths are treated as into Unix-style paths for cross-platform purposes.
    * @return the virtual path to the filesystem uobject wrapped by this FileHandle
    */
  def path: String = virtualPath

  /**
   * Returns the physical path to the actual filesystem object represented by this FileHandle.
   */
  protected[io] def physicalPath: Option[String]

  /**
   * @return The filename extension of the object wrapped by this FileHandle, or emptystring if there is no extension.
   */
  def extension: String = path.split('.').drop(1).lastOption.getOrElse("")

  /**
   * @return the name of this object, without the filename extension and path.
   */
  def name: String = path.split('/').lastOption.getOrElse("/").split('.').head

  /**
   * Returns the [[java.io.File]] backing this file handle.
   * @return an [[scala.Option Option]] containing the [[java.io.File]] that represents this file handle,
   *         or [[scala.None None]] if this file is inside a Jar or Zip archive.
   */
  protected[io] def file: Option[File]

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
  def list: Try[Seq[FileHandle]]

  /**
   * @return a list containing FileHandles to the contents of this FileHandle with the specified suffix, or an
   *         an empty list if this file is not a directory or does not have contents.
   * @param suffix the the specified suffix.
   *
   */
  @deprecated(message = "just use FilterMonadic at call site for better performance", since = "v2.0.0")
  def list(suffix: String): Try[Seq[FileHandle]] = list map ( _ filter ( _.path.endsWith(suffix)) )

  /** Returns a stream for reading this file as bytes.
    * @return a [[scala.util.Success Success]] containing a [[java.io.InputStream InputStream]]
    *         for writing to this file, or a [[scala.util.Failure Failure]] containing a [[java.io.IOException IOException]] if this file is not writeable.
    */
  def read: Try[InputStream]

  /** Returns a buffered stream for reading this file as bytes.
    * @return a [[scala.util.Success Success]] containing a [[java.io.BufferedInputStream BufferedInputStream]]
    *         for writing to this file, or a [[scala.util.Failure Failure]] containing a [[java.io.IOException  IOException]] if this file is not writeable.
    */
  def read(bufferSize: Integer): Try[BufferedInputStream] = read map (new BufferedInputStream(_, bufferSize))

  /** Reads the entire file into a string using the platform's default charset.
    * @return a [[scala.util.Success Success]] containing the contents of the file as a String,
    *         or a [[scala.util.Failure Failure]] containing an [[java.io.IOException IOException]]
    *         if the rontents could not be read.
    */
  def readString: Try[String] = readString(Charset.defaultCharset())

  /** Reads the entire file into a string using the specified charset. */
  def readString(charset: Charset): Try[String] = read map (Source fromInputStream _ mkString)

  /** Returns a [[java.io.OutputStream]] for writing to this file.
    * @return an [[Option]] containing a [[java.io.OutputStream OutputStream]]
    *  for writing to this file, or [[None]] if this file is not writeable.
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if something went wrong while opening the file.
    */
  def write(append: Boolean): Option[OutputStream]

  /** Returns a [[java.io.BufferedOutputStream]] for writing to this file.
    * @return an [[Option]] containing a [[java.io.BufferedOutputStream BufferedOutputStream]]
    *  for writing to this file, or [[None]] if this file is not writeable.
    * @param bufferSize The size of the buffer
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    */
  def write(bufferSize: Integer, append: Boolean): Option[BufferedOutputStream] = write(append) map ( new BufferedOutputStream(_, bufferSize) )

  /** Writes the specified string to the file using the default charset.
  *
  * Returns [[scala.util.Success Success]]`(Unit)` if the string was successfully written,
  * or a [[scala.util.Failure Failure]] containing an [[java.io.IOException IOException]]
  * if writing failed or the [[FileHandle]] was not writable.
  *
  * @param string the string to write to the file
  * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
  * @return `Success(Unit)` if the string was written, `Failure(IOException)` if this file is not writeable
    */
  def writeString(string: String, append: Boolean): Try[Unit] = writeString(string, Charset.defaultCharset(), append)

  /** Writes the specified string to the file using the specified charset.
    *
    * Returns [[scala.util.Success Success]]`(Unit)` if the string was successfully written,
    * or a [[scala.util.Failure Failure]] containing an [[java.io.IOException IOException]]
    * if writing failed or the [[FileHandle]] was not writable.
    *
    * @param string the string to write to the file
    * @param charset the [[java.nio.charset.Charset Charset]] to use while writing to the file
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @return `Success(Unit)` if the string was written, `Failure(IOException)` if this file is not writeable
    */
  def writeString(string: String, charset: Charset, append: Boolean): Try[Unit] = write(append) match {
    case Some(stream) => stream.write(string.getBytes(charset)); Success(Unit)
    case None         => Failure(new IOException("FileHandle " + path + " is not writable."))
  }

  /**
   * Returns a FileHandle into the a sibling of this file with the specified name
   * @param siblingName the name of the sibling file to handle
   * @return a FileHandle into the sibling of this file with the specified name
   */
  def sibling(siblingName: String): Try[FileHandle] = manager.handle(path.replace(
    if (extension == "") name else name + "." + extension, siblingName)
  )

  /**
   * Return a FileHandle into the parent of this file.
   * @return a FileHandle into the parent of this file.
   */
  def parent: Try[FileHandle] = manager.handle(path.replace(
    if (extension == "") "/" + name else "/" + name + "." + extension, "")
  )

  /**
   * Returns a FileHandle into the a child of this file with the specified name
   * @param childName the name of the child file to handle
   * @return a FileHandle into the child of this file with the specified name
   */
  def child(childName: String): Try[FileHandle] = manager.handle(path + "/" + childName)

  /**
   * Returns the length of this file in bytes, or 0 if this FileHandle is a directory or does not exist
   * @return the length of the file in bytes, or 0 if this FileHandle is a directory or does not exist
   */
  def length: Long

  /**
   * Delete this file if it exists and is writable.
   * @return true if this file was successfully deleted, false if it could not be deleted
   */
  def delete: Boolean

  override lazy val toString = this.getClass.getSimpleName + ": " + path

  /**
   * Overriden equality method for FileHandles. Returns true if the other FileHandle is:
   *  1. the same path as this Filehandle
   *  2. the same subclass of FileHandle as this FileHandle
   * @param other another object
   * @return true if other is a FileHandle of the same class and path as this
   */
  override def equals(other: Any) = other match {
    case handle: FileHandle => (handle.path == path) && (handle.getClass == this.getClass)
    case _ => false
  }
}
/**
object FileHandle {
  protected val correctToken = new IOAccessToken
}
*/
object FileHandle {
  protected[io] def unapply(f: FileHandle): Option[(String,String)] = f.physicalPath map {
    (physPath) => (f.path,physPath)
  }
}
