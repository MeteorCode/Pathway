package com.meteorcode.pathway.io.scala_api

import java.io.{File, IOException, InputStream}
import java.util
import java.util.Collections
import java.util.jar.{JarEntry, JarFile}
import java.util.zip.ZipException

import com.meteorcode.pathway.io.ResourceManager

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

/**
 * A [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]] into an item within a Jar archive.
 *
 * DON'T MAKE THESE - if you want to handle a file, please get it from
 * [[com.meteorcode.pathway.io.ResourceManager.handle ResourceManager.handle()]]. The FileHandle system is supposed to
 * allow you to treat files in zip/jar archives as though they were on the filesystem as regular files, but this only
 * works if you treat all files you have to access as instances of [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]].
 * If you  ever refer to files as [[com.meteorcode.pathway.io.DesktopFileHandle DesktopFileHandle]],
 * [[com.meteorcode.pathway.io.scala_api.ZipFileHandle, ZipFileHandle]], or
 * [[JarFileHandle JarFileHandle]] explicitly in your code, you are doing the Wrong Thing and
 * negating a whole lot of time and effort I  put into this system. To reiterate: DO NOT CALL THE CONSTRUCTOR FOR THIS.
 *
 * Since Zip and Jar archives are unmodifyable by Pathway, we can assume that they will not change during the
 * game's execution. Therefore, a number of API methods for [[FileHandle]] can be memoized in Zip and Jar
 * [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]]s. While some external source may theoretically modify the
 * archives while the game is running, we don't support this behaviour as it would lead to issues even if we didn't
 * memoize some method calls (if somebody deletes an asset that various game components have a handle to, you're more or
 * less hosed anyway), so therefore, I think it's  reasonable to assume that nobody's gonna be futzing around with the
 * archives at runtime, and if they are, well, it's their own damn fault for breaking stuff.
 *
 * @param entry  the [[java.util.jar.JarEntry]] representing the file
 * @param parentJarfile a reference to the the [[java.util.jar.JarFile]] containing the
 *                     [[java.util.jar.JarEntry JarEntry]]  - this is necessary so that we can do things like
 *                     list the children of a directory in a Jar archive.
 * @param back the [[java.io.File]] that backs this [[FileHandle]]
 * @param manager the [[com.meteorcode.pathway.io.ResourceManager ResourceManager]] managing the virtual
 *                filesystem containing this [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]]
 * @author Hawk Weisman
 * @see [[com.meteorcode.pathway.io.ResourceManager ResourceManager]]
 * @see [[com.meteorcode.pathway.io.ResourceManager ResourceManager]]
 */
class JarEntryFileHandle (virtualPath: String,
                          private val entry: JarEntry,
                          private val parentJarfile: JarFileHandle,
                          private val back: File,
                          manager: ResourceManager//,
                          //token: IOAccessToken
                          )
  extends JarFileHandle(virtualPath, back, manager
    //, token
  ) {

  def this(virtualPath: String,
           entry: JarEntry,
           parent: JarFileHandle//,
           //token: IOAccessToken
           ) = this(virtualPath, entry, parent, parent.file, parent.manager//, token
  )

  /**
   * @return the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] lazy val physicalPath = parentJarfile.physicalPath + "/" + entry.getName

  /**
   * @return true if this file is a directory, false otherwise
   */
  override lazy val isDirectory = entry.isDirectory

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
   * Returns a list containing this [[FileHandle]]'s children.
   *
   * Since Zip and Jar file handles are not writable and therefore can be guaranteed to not change during
   * Pathway execution, we can memoize their contents, meaning that we only ever have to perform this operation
   * a single time.
   *
   * @return a list containing [[FileHandles]] to the contents of [[FileHandles]], or an empty list if this
   *         file is not a directory or does not have contents.
   */
  @throws(classOf[IOException])
  override lazy val list: util.List[FileHandle] = if (isDirectory) {
    val result = Try(
      Collections.list(jarfile.entries)
        withFilter ( _.getName.split("/").dropRight(1).lastOption == Some(entry.getName.dropRight(1)) )
        map ( (e) => new JarEntryFileHandle(s"${this.path}/${e.getName.split("/").last}", e, parentJarfile) )
    )
    jarfile = new JarFile(back) // reset the jarfile
    result match {
      case Failure(e: IllegalStateException) =>
        throw new IOException("Could not list JarFile entries, file " + path + " appears to have been closed.", e)
      case Failure(up) => throw up
      case Success(list) => list
    }
  } else Collections.emptyList()
}
