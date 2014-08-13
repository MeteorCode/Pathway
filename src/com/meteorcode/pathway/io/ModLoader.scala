/**
 *
 */
package com.meteorcode.pathway.io

import scala.collection.JavaConversions._
import java.io.IOException
import com.meteorcode.pathway.script.{ScriptContainerFactory, ScriptContainer}

/**
 * @author Hawk Weisman
 */
object ModLoader {
  private val beanshell: ScriptContainer = (new ScriptContainerFactory).getNewInstance()

  /**
   * Loads all mods in the target directory.
   * @param directory A [[com.meteorcode.pathway.io.FileHandle]] representing the mods directory
   * @throws IOException if the mods directory is invalid.
   */
  @throws(classOf[IOException])
  def load(directory: FileHandle): Unit = {
    if (directory.exists == false) // if the directory doesn't exist, throw an IOException
      throw new IOException("Could not load mods directory " + directory + ", was not a directory.")
    if (directory.isDirectory == false ) // if the mods directory isn't a directory, throw an IOException
      throw new IOException("Could not load mods directory " + directory + ", did not exist.")
    // otherwise, get all the jarfiles from the directory and load them
    beanshell injectObject("ResourceManager",directory.manager)
    directory.list("init.java").foreach { initScript => beanshell.eval(initScript) }
    beanshell removeObject("ResourceManager") //unset so that next time this is called, a different manager is set
  }
}