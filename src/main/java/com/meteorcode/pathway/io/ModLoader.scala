/**
 *
 */
package com.meteorcode.pathway.io

import com.meteorcode.pathway.io.scala_api.{ResourceManager, FileHandle}
import com.meteorcode.pathway.logging.Logging

import scala.collection.JavaConversions._
import java.io.IOException
import com.meteorcode.pathway.script.{ScriptEnvironment, ScriptContainerFactory, ScriptContainer}

/**
 * @author Hawk Weisman
 * @since v2.0.0
 */
object ModLoader extends Logging {
  private[this] val env = new ScriptEnvironment("require(path) { return container.eval(files.handle(path)); }")
  private[this] val beanshell: ScriptContainer = (new ScriptContainerFactory).getNewInstanceWithEnvironment(env)
  env.addBinding("container", beanshell)


  /**
   * Loads all mods in the target directory.
   *
   * Mods are loaded by running a BeanShell script called `init.java` contained within the mod's archive or directory.
   * This script is always executed within a clean BeanShell interpreter. The game's
   * [[scala_api.ResourceManager]] is bound to a variable called `files` in the
   * BeanShell script environment.
   *
   * @param directory A [[com.meteorcode.pathway.io.scala_api.FileHandle]] representing the mods directory
   * @throws IOException if the mods directory is invalid.
   */
  @throws(classOf[IOException])
  def load(directory: FileHandle): Unit = {
    if (!directory.exists) // if the directory doesn't exist, throw an IOException
      logger.log("Could not load mods directory " + directory + ", was not a directory.")
    if (!directory.isDirectory) // if the mods directory isn't a directory, throw an IOException
      logger.log("Could not load mods directory " + directory + ", did not exist.")
    // otherwise, get all the jarfiles from the directory and load them
    logger.log("loading mods from " + directory)
    env.addBinding("files",directory.manager)
    directory.list("init.java").get.foreach { initScript => beanshell.eval(initScript) }
  }
}
