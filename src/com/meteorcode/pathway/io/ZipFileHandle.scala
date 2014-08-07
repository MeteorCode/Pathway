package com.meteorcode.pathway.io
import java.io.{
  File,
  IOException
}
import java.util.{
  List,
  ArrayList
}
import java.util.zip.{ZipEntry, ZipFile}
/**
 * <p>A FileHandle into the top level of a Zip archive (treated as a directory).</p>
 * <p>DON'T MAKE THESE - if you want to handle a file, please get it from an instance of {@link com.meteorcode.pathway.io.ResourceManager ResourceManager}.
 * The FileHandle system is supposed to allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only works
 * if you treat all files you have to access as instances of {@link com.meteorcode.pathway.io.FileHandle FileHandle}. If you ever refer to files as
 * DesktopFileHandle, ZipFileHandle, or JarFileHandle explicitly in your code, you are doing the Wrong Thing and negating a whole lot of time and effort I
 * put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.</p>
 *
 * @param back A java.util.File representing the Zip archive to handle.
 * @author Hawk Weisman
 */
class ZipFileHandle protected[io] (logicalPath: String,
                                   private val back: File,
                                   manager: ResourceManager) extends FileHandle(logicalPath, manager) {
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
  protected[io] val zipfile = new ZipFile(file)

  protected[io] def this(fileHandle: FileHandle) = this(fileHandle.path, fileHandle.file, fileHandle.manager)
  protected[io] def this(fileHandle: FileHandle, manager: ResourceManager) = this(fileHandle.path, fileHandle.file, manager)
  protected[io] def this(logicalPath: String, fileHandle: FileHandle) = this(logicalPath, fileHandle.file, fileHandle.manager)
  protected[io] def this(back: File, manager: ResourceManager) = this (null, back, manager)

  protected[io]def file = back

  def exists: Boolean = back.exists
  def isDirectory: Boolean = true // Remember, we are pretending that zips are directories
  def writable = false // Zips can never be written to (at least by java.util.zip)

  @throws(classOf[IOException])
  def list: List[FileHandle] = {
    var result = new ArrayList[FileHandle]
    try {
      val entries = zipfile.entries
      // furthermore, I also loathe java.util.zip for making me use the braindead
      // Enumeration<T> class which appears to be a dumb knockoff of Iterator created
      // specifically for use in ZipFile just to make it EVEN WORSE
      // I HATE JAVA
     while (entries.hasMoreElements) result.add( new ZipEntryFileHandle(entries.nextElement(), this, manager) )
     result
    } catch {
      // Don't close my ZipFile while I'm getting its' entries! Geez!
      case e: IllegalStateException => throw new IOException ("Could not list ZipFile entries, file " + path + " appears to have been closed.", e)
    }
  }

  @throws(classOf[IOException])
  def write(append: Boolean) = null

  @throws(classOf[IOException])
  def read = null

}
