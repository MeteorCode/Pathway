package com.meteorcode.pathway.io.java_api

import java.io.IOException
import java.util

import com.meteorcode.pathway.io.scala_api
import com.meteorcode.pathway.io.scala_api._

import scala.language.implicitConversions
import scala.collection.JavaConverters.asScalaBufferConverter

/**
 * Pathway ResourceManager - Java Edition
 * --------------------------------------
 *
 * A ResourceManager "fuses" a directory or directories into a virtual filesystem, abstracting Zip and Jar archives
 * as though they were directories.
 *
 * Root directories and archives archives are attached at `/` in the virtual filesystem, and directories within archives
 * are "fused" into one directory in the virtual filesystem. For example, if we have a file `foo.zip` containing the
 * path `foo/images/spam.png` and a directory `bar` containing `bar/images/eggs.jpeg`, the virtual directory `images/`
 * contains `spam.png` and `eggs.jpeg`.
 *
 * For security reasons, paths within the virtual filesystem are non-writable by default, unless they are within an
 * optional specified write directory. The write directory may exist at any writable physical path, but it will always
 * be attached at `/write/` in the virtual filesystem. Note that if the write directory doesn't exist when this
 * ResourceManager is initialized, it will be created, along with any directories containing it, if necessary.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * Created by Hawk on 6/10/15.
 */
class ResourceManager protected[io](protected val underlying: scala_api.ResourceManager) {
  /**
   * Constructor for a ResourceManager with multiple root directories
   *
   * @param dirs A list of [[FileHandle]] into the directories to be fused into the top level roots of the virtual
   *             filesystem.
   * @param writeDir An optional [[FileHandle]] into the specified write directory. The write directory's virtual
   *                 path will be set to `/write/`.
   * @param loadPolicy A [[LoadOrderPolicy]] representing the game's load-order
   * @return A new ResourceManager
   */
  def this(dirs: util.List[FileHandle], writeDir: FileHandle, loadPolicy: LoadOrderProvider) = this(
    new scala_api.ResourceManager(
      dirs.asScala.map(_.underlying),
      writeDir = Some(writeDir.underlying),
      order    = loadPolicy))

  /**
   * Constructor for a ResoruceManager with a single root directory
   * @param rootDir A String containing the path to the root directory
   * @param writeDir A String containing the path to the write directory
   * @param loadPolicy A [[LoadOrderPolicy]] representing the game's load-order
   * @return A new ResourceManager
   */
  def this(rootDir: String, writeDir: String, loadPolicy: LoadOrderProvider) = this(
    new scala_api.ResourceManager(rootDir,writeDir,loadPolicy)
  )

  /**
   * Request that the ResourceManager handle the file at a given path
   * as a Java [[FileHandle]].
   * @param path the path in the virtual filesystem to handle
   * @throws java.io.IOException if the FileHandle cannot be created
   * @return A [[FileHandle]] for the object that exists at the requested path
   *         in the virutal filesystem.
   */
  @throws(classOf[IOException])
  def handleJava(path: String): FileHandle = underlying.handle(path).get

}

object ResourceManager {
  implicit def asScala(mangler: ResourceManager): scala_api.ResourceManager = mangler.underlying
  implicit def asJava(mangler: scala_api.ResourceManager): ResourceManager = new ResourceManager(mangler)
}