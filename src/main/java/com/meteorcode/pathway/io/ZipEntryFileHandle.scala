package com.meteorcode.pathway.io

import java.io.{
InputStream,
IOException,
File
}
import java.util.zip.{
ZipEntry,
ZipException,
ZipFile
}
import java.util
import java.util.Collections

/**
 * A FileHandle into a file or directory within a zip archive.
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
 * @param entry  the [[java.util.zip.ZipEntry]] representing the file
 * @param parentZipfile a reference to the the [[java.util.zip.ZipFile]] containing the ZipEntry - this is necessary so that
 *               we can do things like list the children of a directory in a Zip archive.
 * @param back the [[java.util.File]] that backs this FileHandle
 * @param manager the ResourceManager managing the virtual filesystem containing this FileHandle
 * @author Hawk Weisman
 * @see [[com.meteorcode.pathway.io.ResourceManager ResourceManager]]
 */
class ZipEntryFileHandle (virtualPath: String,
                          private val entry: ZipEntry,
                          private val parentZipfile: ZipFileHandle,
                          private val back: File,
                          manager: ResourceManager,
                          token: IOAccessToken)
  extends ZipFileHandle(virtualPath, back, manager, token) {

  def this(virtualPath: String,
           entry: ZipEntry,
           parent: ZipFileHandle,
           token: IOAccessToken) = this(virtualPath, entry, parent, parent.file, parent.manager, token)

  /**
   * @return  the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] def physicalPath = if (parentZipfile.physicalPath.endsWith(".zip")) {
    parentZipfile.physicalPath + "/" + entry.getName
  } else {
    parentZipfile.physicalPath + entry.getName
  }

  /**
   * @return true if this file is a directory, false otherwise
   */
  override def isDirectory = entry.isDirectory


  /** Returns a stream for reading this file as bytes, or null if it is not readable (does not exist or is a directory).
    * @return a [[java.io.InputStream]] for reading the contents of this file, or null if it is not readable.
    * @throws IOException if something went wrong while reading from the file.
    */
  @throws(classOf[IOException])
  override def read: InputStream = {
    if (!exists || isDirectory) null
    else try {
      zipfile.getInputStream(entry)
    } catch {
      case ze: ZipException => throw new IOException("Could not read file " + path + ", a ZipException occured", ze)
      case se: SecurityException => throw new IOException("Could not read file " + path + ", a Zip entry was improperly signed", se)
      case ise: IllegalStateException => throw new IOException("Could not read file " + path + " appears to have been closed", ise)
      case up: IOException => throw up //because you've spent too long dealing with java.util.zip and you hate everything.
    }
  }

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
  override def list: util.List[FileHandle] = {
    if (isDirectory) {
      var result = new util.ArrayList[FileHandle]
      zipfile = new ZipFile(back) // reset the zipfile
      try {
        val entries = zipfile.entries
        while (entries.hasMoreElements) {
          val e = entries.nextElement
          if (e.getName.split("/").dropRight(1).lastOption == Some(entry.getName.dropRight(1)))
            result.add(new ZipEntryFileHandle(this.path + e.getName.split("/").last, e, parentZipfile))
        }
        result
      } catch {
        case e: IllegalStateException => throw new IOException("Could not list ZipFile entries, file " + path + " appears to have been closed.", e)
      }
    } else Collections.emptyList()
  }
}
