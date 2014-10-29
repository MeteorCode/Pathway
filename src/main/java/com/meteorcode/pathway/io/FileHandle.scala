package com.meteorcode.pathway.io

import java.io.{
File,
InputStream,
OutputStream,
BufferedOutputStream,
BufferedInputStream,
IOException
}
import java.util
import java.util.List
import java.nio.charset.Charset
import scala.io.Source
import scala.collection.JavaConversions._

/**
 * An abstraction wrapping a file in the filesystem.
 *
 * @author Hawk Weisman
 */
abstract class FileHandle(protected val virtualPath: String,
                          protected[io] var manager: ResourceManager) {
  /** Returns true if the file exists. */
  def exists: Boolean

  /** Returns true if this file is a directory.
    *
    * Note that this may return false if a directory exists but is empty.
    * This is Not My Fault, it's [[java.util.File]] behaviour.
    *
    * @return true if this file is a directory, false otherwise
    * */
  def isDirectory: Boolean

  /** Returns true if this FileHandle represents something that can be written to */
  @throws(classOf[IOException])
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
  protected[io] def physicalPath: String

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
   * @return a [[java.io.File]] that represents this file handle, or null if this file is inside a Jar or Zip archive.
   */
  protected[io] def file: File

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
  @throws(classOf[IOException])
  def list: util.List[FileHandle]

  /**
   * @return a list containing FileHandles to the contents of this FileHandle with the specified suffix, or an
   *         an empty list if this file is not a directory or does not have contents.
   * @param suffix the the specified suffix.
   */
  def list(suffix: String): util.List[FileHandle] = list.filter(entry => entry.path.endsWith(suffix))

  /** @return a [[java.io.InputStream]] for reading this file, or null if the file does not exist or is a directory.
    */
  @throws(classOf[IOException])
  def read: InputStream

  /** Returns a buffered stream for reading this file as bytes.
    * @throws IOException if the file does not exist or is a directory.
    */
  @throws(classOf[IOException])
  def read(bufferSize: Integer): BufferedInputStream = new BufferedInputStream(read, bufferSize)

  /** Reads the entire file into a string using the platform's default charset.
    * @throws IOException if the file does not exist or is a directory.
    */
  @throws(classOf[IOException])
  def readString: String = readString(Charset.defaultCharset())

  /** Reads the entire file into a string using the specified charset. */
  @throws(classOf[IOException])
  def readString(charset: Charset): String = Source.fromInputStream(read).mkString

  /** Returns an [[java.io.OutputStream]] for writing to this file.
    * @return an [[java.io.OutputStream]] for writing to this file, or null if this file is not writable.
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if something went wrong while opening the file.
    */
  @throws(classOf[IOException])
  def write(append: Boolean): OutputStream

  /** Returns a [[java.io.BufferedOutputStream]] for writing to this file.
    * @return a [[java.io.BufferedOutputStream ]] for writing to this file, or null if this file is not writeable.
    * @param bufferSize The size of the buffer
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    */
  def write(bufferSize: Integer, append: Boolean): BufferedOutputStream = if (writable) {
    new BufferedOutputStream(write(append), bufferSize)
  } else null

  /** Writes the specified string to the file using the default charset.
    *
    * Throws an [[java.io.IOException IOException ]] if the FileHandle represents something that is not writeable;
    * yes, I am aware that having some of these methods return null and others throw exceptions is Wrong and I should
    * feel Bad, I wanted them to return an [[scala.Option Option]], but Max wouldn't let me.
    *
    * @param string the string to write to the file
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if this file is not writeable
    */
  @throws(classOf[IOException])
  def writeString(string: String, append: Boolean): Unit = if (writable) {
    writeString(string, Charset.defaultCharset(), append)
  } else {
    throw new IOException("FileHandle " + path + " is not writeable.")
  }

  /** Writes the specified string to the file using the specified  charset.
    *
    * Throws an [[java.io.IOException IOException]] if the FileHandle represents something that is not writeable;
    * yes, I am aware  that having some of these methods return null and others throw exceptions is Wrong and I should
    * feel Bad, I wanted them to return an [[scala.Option Option]], but Max wouldn't let me.
    *
    * @param string the string to write to the file
    * @param charset the [[java.nio.charset.Charset Charset]] to use while writing to the file
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if this file is not writeable
    */
  @throws(classOf[IOException])
  def writeString(string: String, charset: Charset, append: Boolean): Unit = if (writable) {
    write(append).write(string.getBytes(charset))
  } else {
    throw new IOException("FileHandle " + path + " is not writeable.")
  }

  /**
   * Returns a FileHandle into the a sibling of this file with the specified name
   * @param siblingName the name of the sibling file to handle
   * @return a FileHandle into the sibling of this file with the specified name
   */
  def sibling(siblingName: String): FileHandle = manager.handle(path.replace(
    if (extension == "") name else name + "." + extension, siblingName)
  )

  /**
   * Return a FileHandle into the parent of this file.
   * @return a FileHandle into the parent of this file.
   */
  def parent: FileHandle = manager.handle(path.replace(
    if (extension == "") "/" + name else "/" + name + "." + extension, "")
  )

  /**
   * Returns a FileHandle into the a child of this file with the specified name
   * @param childName the name of the child file to handle
   * @return a FileHandle into the child of this file with the specified name
   */
  def child(childName: String): FileHandle = {
    manager.handle(path + "/" + childName)
  }



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

  override def toString = this.getClass.getSimpleName + ": " + path

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