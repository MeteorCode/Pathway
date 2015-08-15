package com.meteorcode.pathway
package script

import java.io.{BufferedReader, InputStreamReader}
import java.util
import javax.script._
import jdk.nashorn.api.scripting.NashornScriptEngineFactory

import io.FileHandle

import scala.util.{Success, Try}
import scala.collection.JavaConverters.mapAsJavaMapConverter

/**
 * A ScriptContainer wraps a script interpreter and a set of variable bindings.
 *
 * @author Hawk Weisman
 *
 * Created by hawk on 8/10/15.
 */
class ScriptMonad(
  private[this] val engine: ScriptEngine,
  private[this] val bindings: util.Map[String,AnyRef]
) {

  def this(engine: ScriptEngine, bindings: Map[String,AnyRef] = Map())
    = this(engine, bindings.asJava)

  def this(engine: ScriptEngine)
    = this(engine, new util.HashMap[String,AnyRef]())

  type Value = Option[AnyRef]

  private[this] val ctx: ScriptContext
    = engine.getContext

  private[this] def _bindings: Bindings
    = { val b: Bindings = engine.createBindings()
        b.asInstanceOf[util.Map[String, AnyRef]].putAll(bindings)
        b
      }

  if (!bindings.isEmpty)
    ctx setBindings (_bindings, ScriptContext.ENGINE_SCOPE)

  @inline private[this] def cleanUp(): Unit
    // this should reset all of the context's bindings
    = ctx setBindings (_bindings, ScriptContext.ENGINE_SCOPE)

  /**
   * Evaluate a script from a String.
   * @param script the script to evaluate
   * @return a [[scala.util.Try Try]] containing the return value from the
   *         script or a [[javax.script.ScriptException ScriptException]]
   *         if the script could not be evaluated.
   */
  def apply(script: String): Try[ScriptMonad]
    = compile(script) match {
        case Some(cs) => apply(cs)
        case None     => Try(engine eval script, ctx) map { _ =>
          val b_prime = ctx getBindings ScriptContext.ENGINE_SCOPE
          cleanUp()
          new ScriptMonad(engine, b_prime)
        }
      }

  def apply(script: CompiledScript): Try[ScriptMonad]
    = Try(script eval ctx) map { _ =>
        val b_prime = ctx getBindings ScriptContext.ENGINE_SCOPE
        cleanUp()
        new ScriptMonad(engine, b_prime)
      }

  /**
   * Evaluate a script from a [[FileHandle]].
   * @param file the [[FileHandle]] containing the script to evaluate.
   * @return a [[scala.util.Try Try]] containing the return value from the
   *         script, an [[java.io.IOException IOException]] if the script
   *         could not be read from the file, or a
   *         [[javax.script.ScriptException ScriptException]] if the script
   *         could not be evaluated.
   */
  def apply(file: FileHandle): Try[ScriptMonad]
    = compile(file) match {
        case Some(thing) => thing flatMap apply _
        case None        => file.read flatMap { stream =>
          val script = new BufferedReader(new InputStreamReader(stream))
          Try(engine.eval(script, ctx)) map { _ =>
            val b_prime = ctx getBindings ScriptContext.ENGINE_SCOPE
            cleanUp()
            new ScriptMonad(engine, b_prime)
          }
        }
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
    = Try(Option(
        ctx.getBindings(ScriptContext.ENGINE_SCOPE)
           .put(name, value)
      ))

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
    = Try(Option(
        ctx.getBindings(ScriptContext.ENGINE_SCOPE)
           .get(name)
      ))

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
    = Try(Option(
      ctx.getBindings(ScriptContext.ENGINE_SCOPE)
         .remove(name)
    ))

  def compile(script: String): Option[CompiledScript]
    = engine match {
      case e: Compilable => Some(e compile script)
      case _             => None
    }

  def compile(file: FileHandle): Option[Try[CompiledScript]]
    = engine match {
      case e: Compilable => Some(file.read map { stream =>
          e compile new BufferedReader(new InputStreamReader(stream))
        })
      case _             => None
    }

}
object ScriptMonad {

  private[this] val factory
    = new NashornScriptEngineFactory

  /**
   * Construct a new ScriptContainer with the default [[ScriptEngine]].
   * @return a new ScriptContainer with the default [[ScriptEngine]]
   */
  def apply(bindings: Map[String,AnyRef] = Map()): ScriptMonad
    = new ScriptMonad(factory.getScriptEngine, bindings)
}
