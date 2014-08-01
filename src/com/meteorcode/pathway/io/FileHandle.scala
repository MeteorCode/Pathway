package com.meteorcode.pathway.io
import java.io.{
  File,
  InputStream,
  OutputStream,
  BufferedInputStream,
  FileInputStream,
  IOException
}
import java.util.{
  List,
  ArrayList
}
import java.nio.charset.Charset
import java.nio.file.{ Files, Paths }

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
trait FileHandle {
  protected val path: String
  protected val manager: ResourceManager
  protected val in: InputStream
  protected val out: OutputStream
  
  /** Returns true if the file exists. */
  def exists(): Boolean
  
  /** Returns true if this file is a directory. */
  def isDirectory(): Boolean
  
  /** Returns the paths to the children of this directory. */
  def list: List[FileHandle]
  
  /** Returns the paths to the children of this directory with the specified suffix. */
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
  
  /**Reads the entire file into a string using the specified charset.*/
  @throws(classOf[IOException])
  def readString(charset: Charset): String = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    new String(Files.readAllBytes(Paths.get(path)), charset)
  }

}
case class DesktopFileHandle(path: String, manager: ResourceManager, in: InputStream, out: OutputStream) extends FileHandle {
  val file = new File(path)

  def exists: Boolean = file exists
  def isDirectory: Boolean = file isDirectory

  def list: List[FileHandle] = {
    var result = new ArrayList[FileHandle]
    for (path <- file.list) { result add manager.read(path) }
    return result
  }

  def list(suffix: String): java.util.List[FileHandle] = {
    var result = new java.util.ArrayList[FileHandle]
    for (path <- file.list() if path endsWith (suffix)) { result add manager.read(path) }
    return result
  }


}