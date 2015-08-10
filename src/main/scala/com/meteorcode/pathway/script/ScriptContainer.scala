package com.meteorcode.pathway
package script

import java.io.{BufferedReader, InputStreamReader}
import javax.script.ScriptEngine
import jdk.nashorn.api.scripting.NashornScriptEngineFactory

import io.FileHandle

import scala.util.Try
import scala.collection.JavaConverters.mapAsJavaMapConverter

/**
 * A ScriptContainer wraps a script interpreter and a set of variable bindings.
 *
 * @author Hawk Weisman
 *
 * Created by hawk on 8/10/15.
 */
class ScriptContainer(
  private[this] val engine: ScriptEngine,
  bindings: Map[String,AnyRef] = Map[String,AnyRef]()
) {

  type Value = Option[AnyRef]

  private[this] val _bindings = engine.createBindings

  if (bindings.nonEmpty) _bindings putAll bindings.asJava

  /**
   * Evaluate a script from a String.
   * @param script the script to evaluate
   * @return a [[scala.util.Try Try]] containing the return value from the
   *         script or a [[javax.script.ScriptException ScriptException]]
   *         if the script could not be evaluated.
   */
  def eval(script: String): Try[AnyRef]
    = Try(engine eval (script, _bindings))

  /**
   * Evaluate a script from a [[FileHandle]].
   * @param file the [[FileHandle]] containing the script to evaluate.
   * @return a [[scala.util.Try Try]] containing the return value from the
   *         script, an [[java.io.IOException IOException]] if the script
   *         could not be read from the file, or a
   *         [[javax.script.ScriptException ScriptException]] if the script
   *         could not be evaluated.
   */
  def eval(file: FileHandle): Try[AnyRef]
    = file.read flatMap { stream =>
        val script = new BufferedReader(new InputStreamReader(stream))
        Try(engine eval (script, _bindings))
      }

  /**
   * Set a variable within this ScriptContainer, returning the previous value
   * (if one exists).
   * @param name the name of the variable to set
   * @param value the value to set to that variable
   * @return a [[scala.util.Try Try]] on an [[scala.Option Option]] containing
   *         the previous value bound to that variable, or [[scala.None None]]
   *         if no value was bound, or an [[IllegalArgumentException]] if the
   *         name is invalid, or a [[NullPointerException]] if the name or
   *         the value is null.
   *
   */
  def set(name: String, value: AnyRef): Try[Value]
    = Try(Option(_bindings put (name, value)))

  /**
   * Accesses a variable within this ScriptContainer, returning value bound to
   * that variable.
   * @param name the name of the variable to access
   * @return a [[scala.util.Try Try]] on an [[scala.Option Option]] containing
   *         the value bound to that variable, or [[scala.None None]]  if no
   *         value was bound, or an [[IllegalArgumentException]] if the name
   *         is invalid, or a [[NullPointerException]] if the name is null.
   *
   */
  def get(name: String): Try[Value]
    = Try(Option(_bindings get name))

  /**
   * Unbind a variable within this ScriptContainer, returning the previous
   * value (if one exists).
   * @param name the name of the variable to unbind.
   * @return a [[scala.util.Try Try]] on an [[scala.Option Option]] containing
   *         the value bound to that variable, or [[scala.None None]] if no
   *         value was bound, or an [[IllegalArgumentException]] if the  name
   *         is invalid, or a [[NullPointerException]] if the name is null.
   *
   */
  def remove(name: String): Try[Value]
    = Try(Option(_bindings remove name))


}
object ScriptContainer {

  private[this] val factory
    = new NashornScriptEngineFactory

  /**
   * Construct a new ScriptContainer with the default [[ScriptEngine]].
   * @return a new ScriptContainer with the default [[ScriptEngine]]
   */
  def apply(): ScriptContainer
    = new ScriptContainer(factory.getScriptEngine)
}
