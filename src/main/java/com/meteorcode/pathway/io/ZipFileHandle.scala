package com.meteorcode.pathway.io

import java.io.{
File,
IOException,
InputStream
}
import java.util
import java.util.zip.ZipFile

/**
 * A FileHandle into the top level of a Zip archive (treated as a directory).
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
 * @param virtualPath The virtual path to the object this FileHandle represents
 * @param back A [[java.io.File]] representing the Zip archive to handle.
 * @param manager the ResourceManager managing this FileHandle
 * @author Hawk Weisman
 * @see [[com.meteorcode.pathway.io.ResourceManager ResourceManager]]
 */
class ZipFileHandle (virtualPath: String,
                     private val back: File,
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

  To add insult to injury, I'm pretty sure this idiocy isn't the Zip ile format's
  fault - Python's 'zipfile' module basically allows you to treat files within a Zip
  archive transparently as though they were regular files in the filesystem, because
  (unlike java.util.zip), it wasn't designed by a committee of people dead-set on
  making my day miserable. Also, documentation for Python's module is much better.

  In short, I hate java.util.zip.
  */
  protected[io] var zipfile = new ZipFile(file)

  def this(fileHandle: FileHandle//,
           //token: IOAccessToken
            ) = this(fileHandle.path, fileHandle.file, fileHandle.manager//, token
  )

  def this(virtualPath: String,
          fileHandle: FileHandle//,
          //token: IOAccessToken
            ) = this(virtualPath, fileHandle.file, fileHandle.manager//, token
  )

  /**
   * Returns a [[java.io.File]] that represents this file handle.
   * @return a [[java.io.File]] that represents this file handle, or null if this file is inside a Jar or Zip archive.
   */
  protected[io] def file = back

  /**
   * Returns the physical path to the actual filesystem object represented by this FileHandle.
   */
  protected[io] def physicalPath = back.getPath

  /** Returns true if the file exists. */
  def exists: Boolean = back.exists

  /** Returns true if this file is a directory.
    *
    * Note that this may return false if a directory exists but is empty.
    * This is Not My Fault, it's [[java.io.File]] behaviour.
    *
    * @return true if this file is a directory, false otherwise
    */
  def isDirectory: Boolean = true   // Remember, we are pretending that zips are directories

  /** Returns true if this FileHandle represents something that can be written to */
  def writable = false // Zips can never be written to (at least by java.util.zip)

  def length = if (isDirectory) 0 else back.length

  def delete = if(writable && exists) back.delete else false

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
  @throws(classOf[IOException])
  def list: util.List[FileHandle] = {
    var result = new util.ArrayList[FileHandle]
    try {
      val entries = zipfile.entries
      // furthermore, I also loathe java.util.zip for making me use the braindead
      // Enumeration<T> class which appears to be a dumb knockoff of Iterator created
      // specifically for use in ZipFile just to make it EVEN WORSE
      // I HATE JAVA
      while (entries.hasMoreElements) {
        val e = entries.nextElement()
        if (e.getName.matches("""^[^\/]+\/*$""")) { // is the entry a top-level child
          result.add(new ZipEntryFileHandle(this.path + e.getName, e, this//, this.token
          ))
        }
      }
      zipfile = new ZipFile(back) // reset the archive
      result
    } catch {
      case e: IllegalStateException => throw new IOException("Could not list ZipFile entries, file " + path + " appears to have been closed.", e)
    }
  }

  /** Returns an [[java.io.OutputStream]] for writing to this file.
    * @return an [[java.io.OutputStream]] for writing to this file, or null if this file is not writable.
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if something went wrong while opening the file.
    */
  @throws(classOf[IOException])
  def write(append: Boolean) = null


  /** Returns a stream for reading this file as bytes, or null if it is not readable (does not exist or is a directory).
    * @return a [[java.io.InputStream]] for reading the contents of this file, or null if it is not readable.
    * @throws IOException if something went wrong while opening the file.
    */
  @throws(classOf[IOException])
  def read: InputStream = null
}
