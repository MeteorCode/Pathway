package com.meteorcode.pathway.io

import java.io.{
InputStream,
IOException,
File
}
import java.util
import java.util.{
List,
ArrayList
}
import java.util.zip.ZipException
import java.util.jar.{
JarFile,
JarEntry,
JarInputStream
}
import java.util.Collections


class JarEntryFileHandle (private val entry: JarEntry,
                          private val parent: JarFileHandle,
                          private val back: File,
                          manager: ResourceManager)
  extends JarFileHandle(parent.path + "/" + entry.getName, back, manager) {

  def this(entry: JarEntry, parent: JarFileHandle) = this(entry, parent, parent.file, parent.manager)

  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] def physicalPath = parent.physicalPath + "/" + entry.getName

  /**
   * @return true if this file is a directory, false otherwise
   */
  override def isDirectory = entry.isDirectory

  /**
   *
   * @return a [[java.io.InputStream]] for reading this file, or null if the file does not exist or is a directory.
   */
  override def read: InputStream = {
    if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
    else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
    else try {
      jarfile.getInputStream(entry)
    } catch {
      case ze: ZipException => throw new IOException("Could not read file " + path + ", a ZipException occured", ze)
      case se: SecurityException => throw new IOException("Could not read file " + path + ", a Jar entry was improperly signed", se)
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
      jarfile = new JarFile(back) // reset the jarfile
      try {
        val entries = jarfile.entries
        while (entries.hasMoreElements) {
          val e = entries.nextElement
          if (e.getName.split("/").dropRight(1).lastOption == Some(entry.getName.dropRight(1)))
            result.add(new JarEntryFileHandle(e, parent))
        }
        result
      } catch {
        case e: IllegalStateException => throw new IOException("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
      }
    } else Collections.emptyList()
  }
}