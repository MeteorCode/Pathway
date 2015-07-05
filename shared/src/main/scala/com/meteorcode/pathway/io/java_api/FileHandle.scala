package com.meteorcode.pathway.io.java_api

import java.io._
import java.nio.charset.Charset
import java.util
import java.util.Optional

import com.meteorcode.pathway.io.scala_api
import me.hawkweisman.util.TryWithFold

import scala.language.implicitConversions
import scala.language.postfixOps
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.util.{Try,Success,Failure}

/**
 * FileHandle API for Java callers.
 *
 * Created by hawk on 6/10/15.
 */
class FileHandle protected[io] (
  protected[io] val underlying: scala_api.FileHandle) {
  import FileHandle.TryToRead

  /** @return true if the file exists. */
  def exists: Boolean = underlying.exists

  /** Returns true if this file is a directory.
    *
    * Note that this may return false if a directory exists but is empty.
    * This is Not My Fault, it's [[java.io.File]] behaviour.
    *
    * @return true if this file is a directory, false otherwise
    * */
  def isDirectory: Boolean = underlying.isDirectory

  /** @return true if this FileHandle represents something that can be
   * written to
   */
  def writable: Boolean = underlying.writable

  /** Returns the virtual path to this FileHandle.
    *
    * All paths are treated as into Unix-style paths for cross-platform
    * purposes.
    * @return the virtual path to the filesystem uobject wrapped by this
    *         FileHandle
    */
  def path: String = underlying.path

  /**
   * @return The filename extension of the object wrapped by this FileHandle,
   *        or an empty string if there is no extension.
   */
  def extension: String = underlying.extension

  /**
   * @return the name of this object, without the filename extension and path.
   */
  def name: String = underlying.name

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or
   *         an empty list if this file is not a directory or does not have
   *         contents.
   */
  @throws[IOException](
    "if something went wrong while accessing the FileHandle's contents")
  def list: util.List[FileHandle]
    = underlying.list.fold(
      up    => throw up,
      list  => list map { new FileHandle(_) } asJava
    )

  /**
   * @return a list containing FileHandles to the contents of this FileHandle
   *         with the specified suffix, or an an empty list if this file is not
   *         a directory or does not have contents.
   * @param suffix the the specified suffix.
   *
   */
  @throws[IOException](
    "if something went wrong while accessing the FileHandle's contents")
  def list(suffix: String): util.List[FileHandle]
    = underlying.list fold (
      up    => throw up,
      list  => list.filter { _.path.endsWith(suffix) }
                   .map    { new FileHandle(_) }
                   .asJava
    )

  /** Returns a stream for reading this file as bytes.
    * @return  a [[java.io.InputStream InputStream]] for reading from this
    *          file, or null if this file is not readable
    */
  @throws[IOException]("if something went wrong while reading from the file")
  def read: Optional[InputStream]
    = underlying.read.unwrap

  /** Returns a buffered stream for reading this file as bytes.
    * @param bufferSize the size of the buffer
    * @return a [[java.io.BufferedInputStream BufferedInputStream]] for
    *         reading from this file, or null if this file is not readable
    */
  @throws[IOException]("if something went wrong while reading from the file")
  def read(bufferSize: Int): Optional[BufferedInputStream]
    = underlying.read(bufferSize).unwrap

  /** Reads the entire file into a string using the platform's default charset.
    * @return a String containing the contents of the file
    */
  @throws[IOException]("if something went wrong while reading from the file")
  def readString: Optional[String]
    = underlying.readString.unwrap

  /** Reads the entire file into a string using the specified charset.
    * @param charset the [[Charset]] for reading the file
    * @return a String containing the contents of the file read with the
    *         specified charset
    */
  @throws[IOException]("if something went wrong while reading from the file")
  def readString(charset: Charset): Optional[String]
    = underlying.readString(charset).unwrap

  /** Returns a [[java.io.OutputStream]] for writing to this file.
    * @return a [[java.io.OutputStream OutputStream]], or null if this file
    *         is not writable
    * @param append If false, this file will be overwritten if it exists,
    *               otherwise it will be appended.
    * @throws IOException if something went wrong while opening the file.
    */
  @throws[IOException]("if something went wrong while reading from the file")
  def write(append: Boolean): Optional[OutputStream]
    = underlying.write(append)

  /** Returns a [[java.io.BufferedOutputStream]] for writing to this file.
    * @return a [[java.io.BufferedOutputStream BufferedOutputStream]] for
    *         writing to this file, or null if this file is not writeable.
    * @param bufferSize The size of the buffer
    * @param append If false, this file will be overwritten if it exists,
    *               otherwise it will be appended.
    */
  @throws[IOException]("if something went wrong while reading from the file")
  def write(bufferSize: Int, append: Boolean): Optional[BufferedOutputStream]
    = underlying.write(bufferSize,append)

  /** Writes the specified string to the file using the default charset.
    *
    * @param string the string to write to the file
    * @param append If false, this file will be overwritten if it exists,
    *               otherwise it will be appended.
    *
    */
  @throws[IOException]("if the file does not exist or could not be written to")
  def writeString(string: String, append: Boolean): Unit
    = underlying.writeString(string, Charset.defaultCharset(), append) fold (
        up => throw up,
        _  => {}
      )

  /** Writes the specified string to the file using the specified charset.
    *
    * @param string the string to write to the file
    * @param charset the [[java.nio.charset.Charset Charset]] to use while
    *                writing to the file
    * @param append If false, this file will be overwritten if it exists,
    *               otherwise it will be appended.
    */
  @throws[IOException]("if the file does not exist or could not be written to")
  def writeString(string: String, charset: Charset, append: Boolean): Unit
    = underlying.writeString(string, charset, append) fold (
        up => throw up,
        _  => {}
      )

  /**
   * Returns a FileHandle into the a sibling of this file with the specified
   * name.
   * @param siblingName the name of the sibling file to handle
   * @return a FileHandle into the sibling of this file with the specified
   * name.
   */
  @throws[IOException]("if the requested sibling cannot be accessed")
  def sibling(siblingName: String): FileHandle
    = underlying.sibling(siblingName) fold (
      up      => throw up,
      sibling => sibling
    )

  /**
   * Return a FileHandle into the parent of this file.
   * @return a FileHandle into the parent of this file.
   */
  @throws[IOException]("if the parent cannot be accessed")
  def parent: FileHandle
    = underlying.parent fold (
      up      => throw up,
      parent  => parent
    )


  /**
   * Returns a FileHandle into the a child of this file with the
   * specified name.
   * @param childName the name of the child file to handle
   * @return a FileHandle into the child of this file with
   * the specified name.
   */
  @throws[IOException]("if the requested child cannot be accessed")
  def child(childName: String): FileHandle
    = underlying.child(childName) fold (
      up    => throw up,
      child => child
    )

  /**
   * Returns the length of this file in bytes, or 0 if this FileHandle
   * is a directory or does not exist.
   * @return the length of the file in bytes, or 0 if this FileHandle
   * is a directory or does not exist.
   */
  def length: Long = underlying.length

  /**
   * Delete this file if it exists and is writable.
   * @return true if this file was successfully deleted,
   * false if it could not be deleted.
   */
  def delete: Boolean = underlying.delete

  override lazy val toString = this.getClass.getSimpleName + ": " + path

  /**
   * Overriden equality method for FileHandles.
   * Returns true if the other FileHandle is:
   *  1. the same path as this Filehandle
   *  2. the same subclass of FileHandle as this FileHandle
   * @param other another object
   * @return true if other is a FileHandle of the same class and path as this
   */
  override def equals(other: Any) = other match {
    case handle: FileHandle =>
      (handle.underlying == underlying) && (handle.getClass == this.getClass)
    case scala_api.FileHandle(virtPath, physPath) =>
      path == virtPath && underlying.physicalPath.contains(physPath)
    case _ => false
  }

}
/**
 * This contains mostly just implicit defs for internal use only.
 * @author Hawk Weisman
 */
object FileHandle {

  implicit def fromScala(handle: scala_api.FileHandle): FileHandle
    = new FileHandle(handle)

  implicit def asScala(handle: FileHandle): scala_api.FileHandle
    = handle.underlying

  /**
   * Enriches a [[scala.util.Try]] with an `unwrap()` method that handles
   * [[FileHandle read errors]].
   * @param  underlying the underlying [[scala.util.Try]]
   * @return a [[TryToRead]]
   */
  protected implicit class TryToRead[T >: Null](
    private[this] val underlying: Try[T]
  ) {
    @throws[IOException]
    protected[java_api] def unwrap: Optional[T]
      = underlying
        .recover {
          case up // if the failure is not a "could not read from" failure
            if !up.getMessage.startsWith("Could not read from") => throw up }
        .toOption
    }
}
