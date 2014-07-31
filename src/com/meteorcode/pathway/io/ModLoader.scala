/**
 *
 */
package com.meteorcode.pathway.io
import scala.collection.JavaConversions._
import java.io.{ IOException, FilenameFilter}
import java.util.jar.{ JarInputStream, JarEntry }
import com.meteorcode.pathway.script.{ ScriptContainerFactory, ScriptContainer }
import scala.io.Source
import java.util.zip.ZipEntry

/**
 * @author Hawk Weisman
 *
 */
object ModLoader {
  private val beanshell: ScriptContainer = new ScriptContainerFactory getNewInstance ()
  //beanshell injectObject("gdx",gdx)

  private def loadMod(mod: FileHandle) = {
    val stream = new JarInputStream(mod.read(), true)
    def next() {
      val entry = stream.getNextEntry
      entry.getName match {
        case "init.java" =>           
          val buffer = new Array[Byte](entry.getSize.asInstanceOf[Integer])
          stream.read(buffer,0,buffer.length)
          beanshell.eval(Source.fromRawBytes(buffer).mkString)
          stream.closeEntry
        case _ => next
      }
    }
    next
    stream close
  }

  /**
   * Loads all mods in the target directory.
   */
  def loadDir(modsDir: String) = {
    val directory = ResourceManager.read(modsDir)
    if (directory.exists) { // if the directory doesn't exist, throw an IOException
      if (directory.isDirectory()) { // if the mods directory isn't a directory, throw an IOException
        directory.list(".jar").foreach { loadMod } // otherwise, get all the jarfiles from the directory and load them
      } else throw new IOException("Could not load mods directory " + modsDir + ", was not a directory.")
    } else throw new IOException("Could not load mods directory " + modsDir + ", did not exist.")
  }
}