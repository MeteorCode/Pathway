package com.meteorcode.pathway.io

import java.io.{
InputStream,
IOException
}
import java.util
import java.util.{
List,
ArrayList
}
import java.util.zip.{
ZipEntry,
ZipException
}
import java.util.Collections

/**
 * A FileHandle into a file or directory within a zip archive.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[com.meteorcode.pathway.io.ResourceManager.handle()]]. The FileHandle system is supposed to allow you to treat files in
 * zip/jar archives as though they were on the filesystem as regular files, but this only works if you treat all files
 * you have to access as instances of [[com.meteorcode.pathway.io.FileHandle]]. If you  ever refer to files as
 * [[com.meteorcode.pathway.io.DesktopFileHandle]], [[com.meteorcode.pathway.io.ZipFileHandle]], or
 * [[com.meteorcode.pathway.io.JarFileHandle]] explicitly in your code, you are doing the  Wrong Thing and negating a
 * whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * @param entry
 * the [[java.util.zip.ZipEntry]] representing the file
 * @param parent
 * a reference to the the [[java.util.zip.ZipFile]] containing the ZipEntry - this is necessary so that we can do
 * things like list the children of a directory in a Zip archive.
 * @author Hawk Weisman
 */
class ZipEntryFileHandle (private val entry: ZipEntry,
                          private val parent: ZipFileHandle,
                          manager: ResourceManager) extends FileHandle(parent.path + "/" + entry.getName, manager) {

  def this(entry: ZipEntry, parent: ZipFileHandle) = this(entry, parent, parent.manager)

  protected[io] def physicalPath = parent.physicalPath + "/" + entry.getName

  def writable = false

  // Zip files cannot be written to :c
  def exists = parent.exists

  // if the ZipFile this zip entry lives in exists, it is Real And Has Been Proven To Exist
  def isDirectory = entry.isDirectory

  def file = null

  @throws(classOf[IOException])
  def read: InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    else try {
      parent.zipfile.getInputStream(entry)
    } catch {
      case ze: ZipException => throw new IOException("Could not read file " + path + " a ZipException occured", ze)
      case ise: IllegalStateException => throw new IOException("Could not read file " + path + " appears to have been closed", ise)
      case up: IOException => throw up //haha!
    }
  }

  def list: util.List[FileHandle] = {
    if (isDirectory) {
      var result = new util.ArrayList[FileHandle]
      try {
        val entries = parent.zipfile.entries
        while (entries.hasMoreElements) {
          val e = entries.nextElement
          if (e.getName.split("/").dropRight(1).last == entry.getName) // if e is a child of this
            result.add(new ZipEntryFileHandle(e, parent, manager))
        }
        result
      } catch {
        // Don't close my ZipFile while I'm getting its' entries! Geez!
        case e: IllegalStateException => throw new IOException("Could not list ZipFile entries, file " + path + " appears to have been closed.", e)
      }
    } else Collections.emptyList()
  }

  @throws(classOf[IOException])
  def write(append: Boolean) = null

}