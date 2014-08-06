package com.meteorcode.pathway.io
import java.io.{
  InputStream,
  OutputStream,
  BufferedOutputStream,
  BufferedInputStream,
  IOException
}
import java.util
import java.nio.charset.Charset
import scala.io.Source
import scala.collection.JavaConversions._

/**
 * <p>An abstraction wrapping a file in the filesystem.</p>
 *
 * <p>FileHandle paths are relative to the game's Assets directory. This is a "virtual directory" managed by the game's
 * {@link com.meteorcode.pathway.io.ResourceManager ResourceManager}. Zip and Jar files within the virtual assets directory are
 * treated as though they were directories, rather than as flat files, so files within a Zip or Jar file may be accessed as though
 * that file was a directory containing them. </p>
 *
 * <p>On the desktop, the Assets directory is located at <approot>/assets, although a different directory may be specified when the
 * game's ResourceManager is initialized. On Android, the game's virtual Assets directory encompasses the files stored within the
 * game's APK as assets, and files stored in the application's associated portion of the device's internal storage. The ResourceManager
 * "fuses" thetop-level index of both of these locations  into one virtual directory tree, to which all FileHandle paths are relative.</p>
 *
 * <p> All FileHandles are backed by {@link java.io.InputStream InputStream}s and {@link java.io.OutputStream OutputStream}s,
 * so reading and writing to a file should be consistent across operating systems, filesystem locations, and file types.</p>
 *
 * <p>DO NOT use the FileHandle() constructor if you want a FileHandle into a file. Instead, get it from
 * {@link com.meteorcode.pathway.io.FileHandle apply(path)}  (call FileHandle(path)) from Scala, or
 * {@link com.meteorcode.pathway.io.FileHandle#handle handle(path)} from Java. Calling new FileHandle() will NOT give you what you want.</p>
 *
 * @author Hawk Weisman
 */
abstract class FileHandle {
  /** Returns true if the file exists. */
  def exists(): Boolean

  /** Returns true if this file is a directory. */
  def isDirectory: Boolean

  /** Returns true if this FileHandle represents something that can be written to */
  def writeable(): Boolean

  /** <p>Returns the path to this FileHandle.</p>
   * <p>All paths are treated as into Unix-style paths for cross-platform purposes.
   */
  def path: String

  /**
   *  <p>Returns a list containing FileHandles to the contents of FileHandle .</p>
   *  <p> Returns an empty list if this file is not a directory or does not have contents.</p>
   */
  def list: util.List[FileHandle]

  /**
   *  <p>Returns a list containing FileHandles to the contents of this FileHandle with the specified suffix.</p>
   *  <p> Returns an empty list if this file is not a directory or does not have contents.</p>
   *  @param suffix
   */
  def list(suffix: String): util.List[FileHandle] = list.filter(entry => entry.path.endsWith(suffix))

  /** Returns a stream for reading this file as bytes.
   *  @throws IOException if the file does not exist or is a directory.
   */
  @throws(classOf[IOException])
  def read(): InputStream

  /** Returns a buffered stream for reading this file as bytes.
   *  @throws IOException if the file does not exist or is a directory.
   */
  @throws(classOf[IOException])
  def read(bufferSize: Integer): BufferedInputStream = new BufferedInputStream (read(), bufferSize)

  /** Reads the entire file into a string using the platform's default charset.
	 *  @throws IOException if the file does not exist or is a directory.
   */
  @throws(classOf[IOException])
  def readString(): String = readString(Charset.defaultCharset())

  /** Reads the entire file into a string using the specified charset.*/
  @throws(classOf[IOException])
  def readString(charset: Charset): String = Source.fromInputStream(read()).mkString

  /** Returns an {@link java.io.OutputStream OutputStream} for writing to this file.
   * @return an {@link java.io.OutputStream OutputStream} for writing to this file, or null if this file is not writeable.
   * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
   */
   def write(append: Boolean): OutputStream

  /** Returns an {@link java.io.BufferedOutputStream BufferedOutputStream} for writing to this file.
   * @return an {@link java.io.BufferedOutputStream BufferedOutputStream} for writing to this file, or null if this file is not writeable.
   * @param bufferSize The size of the buffer
   * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
   */
   def write(bufferSize: Integer, append: Boolean): BufferedOutputStream = if (writeable()) { new BufferedOutputStream(write(append), bufferSize) } else null

   /** <p>Writes the specified string to the file using the default charset.</p>
    * <p>Throws an {@link java.io.IOException IOException} if the FileHandle represents something that is not writeable; yes, I am aware
    * that having some of these methods return null and others throw exceptions is Wrong and I should feel Bad,
    * I wanted them to return an {@link scala.Option Option}, but Max wouldn't let me.</p>
    * @param string the string to write to the file
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if this file is not writeable
    */
   @throws(classOf[IOException])
   def writeString (string: String, append: Boolean): Unit = if (writeable()) { writeString(string, Charset.defaultCharset(), append) } else { throw new IOException("FileHandle " + path + " is not writeable.") }

   /** Writes the specified string to the file using the specified  charset.
    * <p>Throws an {@link java.io.IOException IOException} if the FileHandle represents something that is not writeable; yes, I am aware
    * that having some of these methods return null and others throw exceptions is Wrong and I should feel Bad,
    * I wanted them to return an {@link scala.Option Option}, but Max wouldn't let me.</p>
    * @param string the string to write to the file
    * @param charset the {@link java.nio.charset.Charset charset} to use while writing to the file
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if this file is not writeable
    */
   @throws(classOf[IOException])
   def writeString (string: String, charset: Charset, append: Boolean): Unit = if (writeable()) { write(append).write(string.getBytes(charset)) } else { throw new IOException("FileHandle " + path + " is not writeable.") }

   override def toString = path
}

object FileHandle {
  /**
   * The Correct Way to get new FileHandles for a specified path
   * @param path the path you want a FileHandle into
   */
  def apply(path: String) = path.split('.').drop(1).lastOption match {
    // TODO: Special-case files within archives, this placeholder pattern match will have a Hard Time
    // if you try to get a handle into a file within an archive directly instead of requesting the top-level archive
    case Some("jar") => new JarFileHandle(path)
    case Some("zip") => new ZipFileHandle(path)
    case _ => new DesktopFileHandle(path)
  }

  /**
   * <p>Alternate (wrapper) version of {@link com.meteorcode.pathway.io.FileHandle apply()} for Java callers who can't call apply() as a constructor.</p>
   * <p>Java programmers should treat FileHandle.handle(path) as equivalent to FileHandleFactory.getNewInstance(path).</p>
   * @param path the path to the thing you want a handle into
   */
  def handle(path: String) = apply(path)
}