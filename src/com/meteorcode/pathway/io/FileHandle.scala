package com.meteorcode.pathway.io
import java.io.{
  File,
  InputStream,
  OutputStream,
  BufferedOutputStream,
  BufferedInputStream,
  FileInputStream,
  FileOutputStream,
  IOException
}
import java.util.{
  List,
  ArrayList
}
import java.nio.charset.Charset
import java.util.zip.ZipInputStream
import java.util.jar.JarInputStream
import java.util.Collections

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
 * @author Hawk Weisman
 */
abstract class FileHandle (
    protected val path: String,
    protected val in: InputStream,
    protected val out: OutputStream
    ) {


  /** Returns true if the file exists. */
  def exists(): Boolean

  /** Returns true if this file is a directory. */
  def isDirectory(): Boolean

  /** Returns true if this FileHandle represents something that can be written to */
  def writeable(): Boolean = exists && (out != null)

  /**
   *  <p>Returns a list containing FileHandles to the contents of FileHandle .</p>
   *  <p> Returns an empty list if this file is not a directory or does not have contents.</p>
   */
  def list: List[FileHandle]

  /**
   *  <p>Returns a list containing FileHandles to the contents of this FileHandle with the specified suffix.</p>
   *  <p> Returns an empty list if this file is not a directory or does not have contents.</p>
   *  @param suffix
   */
  def list(suffix: String): java.util.List[FileHandle]

  /** Returns a stream for reading this file as bytes.
   *  @throws IOException if the file does not exist or is a directory.
   */
  @throws(classOf[IOException])
  def read(): InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    in
  }

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
  def readString(charset: Charset): String = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    new String(Files.readAllBytes(Paths.get(path)), charset)
  }

  /** Returns an {@link java.io.OutputStream OutputStream} for writing to this file.
   * @return an {@link java.io.OutputStream OutputStream} for writing to this file, or null if this file is not writeable.
   */
   def write(): OutputStream = if (writeable) { out } else null //TODO: Make this support an 'append' flag

  /** Returns an {@link java.io.BufferedOutputStream BufferedOutputStream} for writing to this file.
   * @return an {@link java.io.BufferedOutputStream BufferedOutputStream} for writing to this file, or null if this file is not writeable.
   * @param bufferSize The size of the buffer
   */
   def write(bufferSize: Integer): BufferedOutputStream = if (writeable) { new BufferedOutputStream(out, bufferSize) } else null //TODO: Make this support an 'append' flag

   /** <p>Writes the specified string to the file using the default charset.</p>
    * <p>Throws an {@link java.io.IOException IOException} if the FileHandle represents something that is not writeable; yes, I am aware
    * that having some of these methods return null and others throw exceptions is Wrong and I should feel Bad,
    * I wanted them to return an {@link scala.Option Option}, but Max wouldn't let me.</p>
    * @param string the string to write to the file
    * @throws IOException if this file is not writeable
    */
   @throws(classOf[IOException])
   def writeString (string: String): Unit = if (writeable) { writeString(string, Charset.defaultCharset()) } else { throw new IOException("FileHandle " + path + " is not writeable.") }

   /** Writes the specified string to the file using the specified  charset.
    * <p>Throws an {@link java.io.IOException IOException} if the FileHandle represents something that is not writeable; yes, I am aware
    * that having some of these methods return null and others throw exceptions is Wrong and I should feel Bad,
    * I wanted them to return an {@link scala.Option Option}, but Max wouldn't let me.</p>
    * @param string the string to write to the file
    * @param charset the {@link java.nio.charset.Charset charset} to use while writing to the file
    * @throws IOException if this file is not writeable
    */
   @throws(classOf[IOException])
   def writeString (string: String, charset: Charset): Unit = if (writeable) { out.write(string.getBytes(charset)) } else { throw new IOException("FileHandle " + path + " is not writeable.") }
}

case class DesktopFileHandle (pathTo: String, inStream: InputStream, outStream: OutputStream) extends FileHandle(pathTo, inStream, outStream) {
  val file = new File(path)

  def exists: Boolean = file exists
  def isDirectory: Boolean = file isDirectory

  def list: List[FileHandle] = {
    if (isDirectory) {
      var result = new java.util.ArrayList[FileHandle]
      for (item <- file.list) {
        item.split('.').drop(1).lastOption match {
          case Some("jar") => result add new JarFileHandle(item, new JarInputStream(new FileInputStream(item)))
          case Some("zip") => result add new ZipFileHandle(item, new ZipInputStream(new FileInputStream(item)))
          case Some(_) => result add new DesktopFileHandle(item, new FileInputStream(item), new FileOutputStream(item))
          case None if new File(item) isDirectory => result add new DesktopFileHandle(item, null, null)
          case None if new File(item) isFile => result add new DesktopFileHandle(item, new FileInputStream(item), new FileOutputStream(item))
        }
      }
      result
    } else Collections.emptyList()
  }

  def list(suffix: String): java.util.List[FileHandle] = {
    if (isDirectory) {
      var result = new java.util.ArrayList[FileHandle]
      for (item <- file.list if item endsWith (suffix)) {
        suffix match {
          case ".jar" => result add new JarFileHandle(item, new JarInputStream(new FileInputStream(item)))
          case ".zip" => result add new ZipFileHandle(item, new ZipInputStream(new FileInputStream(item)))
          case "" if new File(item) isDirectory => result add new DesktopFileHandle(item, null, null)
          case "" if new File(item) isFile => result add new DesktopFileHandle(item, new FileInputStream(item), new FileOutputStream(item))
          case _ => result add new DesktopFileHandle(item, new FileInputStream(item), new FileOutputStream(item))
        }
      }
      result
    } else Collections.emptyList()
  }
}

// TODO: Implement these
case class ZipFileHandle (pathTo: String, inStream: ZipInputStream) extends FileHandle (pathTo, inStream, null) {
  def exists(): Boolean = ??? //TODO: Implement
  def isDirectory(): Boolean = ??? //TODO: Implement
  def list(suffix: String): List[FileHandle] = ??? //TODO: Implement
  def list: List[FileHandle] = ??? //TODO: Implement
}

case class JarFileHandle (pathTo: String, inStream: JarInputStream) extends FileHandle (pathTo, inStream, null)  {
  def exists(): Boolean = ??? //TODO: Implement
  def isDirectory(): Boolean = ??? //TODO: Implement
  def list(suffix: String): List[FileHandle] = ??? //TODO: Implement
  def list: List[FileHandle] = ??? //TODO: Implement
}

case class AndroidFileHandle (pathTo: String, inStream: InputStream, outStream: OutputStream) extends FileHandle(pathTo, inStream, outStream) {
  def exists(): Boolean = ??? //TODO: Implement
  def isDirectory(): Boolean = ??? //TODO: Implement
  def list(suffix: String): List[FileHandle] = ??? //TODO: Implement
  def list: List[FileHandle] = ??? //TODO: Implement
}


object FileHandle {
  val platform = System getProperties

  def apply(path: String): FileHandle = platform.getProperty("os.name") match {
    case "Linux" => platform.getProperty("java.vendor") match {
      case "The Android Project" => new AndroidFileHandle(path, null, null)
      case _ => new DesktopFileHandle(path, null, null) // TODO: make sure the initial path is a directory
    }
    case "MacOSX" => new DesktopFileHandle(path, null, null) // TODO: make sure the initial path is a directory
    // TODO: Special-case for Windows
  }
}