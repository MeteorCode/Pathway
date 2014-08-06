package com.meteorcode.pathway.io
import java.io.{
  InputStream,
  IOException
}
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


class JarEntryFileHandle protected[io] (private val entry: JarEntry,
                                        private val parent: JarFile,
                                        private val pathTo: String,
                                        manager: ResourceManager)
  extends FileHandle(manager) {
    def file = null
    def writeable = false
    def exists = true
    def isDirectory = entry.isDirectory
    def path = pathTo + entry.getName//TODO: coerce paths into Unix paths.

    def read: InputStream = {
      if (!exists) throw new IOException("Could not read file:" + path + ", the requested file does not exist.")
      else if (isDirectory) throw new IOException("Could not read file:" + path + ", the requested file is a directory.")
      else try {
        parent.getInputStream(entry)
      } catch {
        case ze: ZipException => throw new IOException("Could not read file " + path + ", a ZipException occured", ze)
        case se: SecurityException => throw new IOException("Could not read file " + path + ", a Jar entry was improperly signed", se)
        case ise: IllegalStateException => throw new IOException("Could not read file " + path + " appears to have been closed", ise)
        case up: IOException => throw up //because you've spent too long dealing with java.util.zip and you hate everything.
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
              result.add( new JarEntryFileHandle(e, parent, pathTo, manager) )
          }
         result
        } catch {
          case e: IllegalStateException => throw new IOException ("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
        }
      } else Collections.emptyList()
    }

    def write(append: Boolean) = null
    }