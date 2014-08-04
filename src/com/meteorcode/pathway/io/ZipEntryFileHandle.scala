package com.meteorcode.pathway.io
import java.io.{
  File,
  InputStream,
  IOException
}
import java.util.{
  List,
  ArrayList
}
import java.util.zip.{
  ZipFile,
  ZipEntry,
  ZipInputStream,
  ZipException
}
import java.util.Collections

  /**
   * <p>A FileHandle into a file or directory within a zip archive.</p>
   * <p>DON'T MAKE THESE - if you want to handle a file, please get it from {@link com.meteorcode.pathway.io.FileHandle apply()}.
   * The FileHandle system is supposed to allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only works
   * if you treat all files you have to access as instances of {@link com.meteorcode.pathway.io.FileHandle FileHandle}. If you ever refer to files as
   * DesktopFileHandle, ZipFileHandle, or JarFileHandle explicitly in your code, you are doing the Wrong Thing and negating a whole lot of time and effort I
   * put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.</p>
   *
   * @param entry
   *              the {@link java.util.zip.ZipEntry ZipEntry} representing the file
   * @param parent
   *              a reference to the the {@link java.util.zip.ZipFile ZipFile} containing the ZipEntry - this is necessary so that we can do things
   *              like list the children of a directory in a Zip archive.
   * @param path
   *             the path to the top-level ZipFile that contains the thing this handle is to
   * @author Hawk Weisman
   */
class ZipEntryFileHandle protected[io] (private val entry: ZipEntry, private val parent: ZipFile, private val pathTo: String) extends FileHandle {
    def writeable = false // Zip files cannot be written to :c
    def exists = true // if this ZipEntry was found in the ZipFile, it is Real And Has Been Proven To Exist
                      // (this is okay because ZipEntries are apparently un-deleteable; HAVE I MENTIONED HOW MUCH I HATE java.util.zip LATELY?)
    def isDirectory = entry.isDirectory
    def path = pathTo + entry.getName//TODO: coerce paths into Unix paths.

    @throws(classOf[IOException])
    def read: InputStream = {
      if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
      else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
      else try {
        parent.getInputStream(entry)
      } catch {
        case ze: ZipException => throw new IOException("Could not read file " + path + " a ZipException occured", ze)
        case ise: IllegalStateException => throw new IOException("Could not read file " + path + " appears to have been closed", ise)
        case up: IOException => throw up //haha!
      }
    }

    def list: List[FileHandle] = {
      if (isDirectory) {
        var result = new ArrayList[FileHandle]
        try {
          val entries = parent.entries
          while (entries.hasMoreElements) {
            val e = entries.nextElement
            if (e.getName.split("/").dropRight(1).equals(entry.getName))
              result.add( new ZipEntryFileHandle(e, parent, pathTo) )
          }
         result
        } catch {
          // Don't close my ZipFile while I'm getting its' entries! Geez!
          case e: IllegalStateException => throw new IOException ("Could not list ZipFile entries, file " + path + " appears to have been closed.", e)
        }
      } else Collections.emptyList()
    }

    @throws(classOf[IOException])
    def write(append: Boolean) = null

  }