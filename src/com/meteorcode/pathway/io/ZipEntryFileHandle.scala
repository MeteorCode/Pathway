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
import java.util.{
List,
ArrayList
}
import java.util.Collections

/**
 * A FileHandle into a file or directory within a zip archive.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[com.meteorcode.pathway.io.ResourceManager.handle()]]. The FileHandle system is supposed to allow you to treat files in
 * zip/zip archives as though they were on the filesystem as regular files, but this only works if you treat all files
 * you have to access as instances of [[com.meteorcode.pathway.io.FileHandle]]. If you  ever refer to files as
 * [[com.meteorcode.pathway.io.DesktopFileHandle]], [[com.meteorcode.pathway.io.ZipFileHandle]], or
 * [[com.meteorcode.pathway.io.ZipFileHandle]] explicitly in your code, you are doing the  Wrong Thing and negating a
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
                          private val back: File,
                          manager: ResourceManager)
  extends ZipFileHandle(parent.path + "/" + entry.getName, back, manager) {

  def this(entry: ZipEntry, parent: ZipFileHandle) = this(entry, parent, parent.file, parent.manager)

  override protected[io] def physicalPath = if (parent.physicalPath.endsWith(".zip")) {
    parent.physicalPath + "/" + entry.getName
  } else {
    parent.physicalPath + entry.getName
  }

  override def isDirectory = entry.isDirectory

  override def read: InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    else try {
      zipfile.getInputStream(entry)
    } catch {
      case ze: ZipException => throw new IOException("Could not read file " + path + ", a ZipException occured", ze)
      case se: SecurityException => throw new IOException("Could not read file " + path + ", a Zip entry was improperly signed", se)
      case ise: IllegalStateException => throw new IOException("Could not read file " + path + " appears to have been closed", ise)
      case up: IOException => throw up //because you've spent too long dealing with java.util.zip and you hate everything.
    }
  }

  override def list: List[FileHandle] = {
    if (isDirectory) {
      var result = new ArrayList[FileHandle]
      zipfile = new ZipFile(back) // reset the zipfile
      try {
        val entries = zipfile.entries
        while (entries.hasMoreElements) {
          val e = entries.nextElement
          if (e.getName.split("/").dropRight(1).lastOption == Some(entry.getName.dropRight(1)))
            result.add(new ZipEntryFileHandle(e, parent))
        }
        result
      } catch {
        case e: IllegalStateException => throw new IOException("Could not list ZipFile entries, file " + path + " appears to have been closed.", e)
      }
    } else Collections.emptyList()
  }
}