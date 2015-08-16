package com.meteorcode.pathway
package script

import java.io.{BufferedReader, InputStreamReader}
import java.util
import javax.script._
import jdk.nashorn.api.scripting.NashornScriptEngineFactory

import io.FileHandle

import scala.util.Try
import scala.collection.JavaConverters.{mapAsJavaMapConverter, mapAsScalaMapConverter}
import scala.collection.immutable
import scala.languageFeature.postfixOps

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
  type Result = Try[(ScriptMonad, Value)]

  private[this] val ctx: ScriptContext
    = new SimpleScriptContext()

  private[this] val _bindings: Bindings
    = ctx.getBindings(ScriptContext.ENGINE_SCOPE)

  _bindings.asInstanceOf[util.Map[String, AnyRef]]
           .putAll(bindings)

  /**
   * Perform all clean-up actions after evaluating a script.
   *
   * This ensures that the bindings set by the script have been
   * unset.
   */
  @inline private[this] def cleanUp(): Unit
    = { _bindings.asScala
          .keySet
            // select only bindings that didn't exist previously
          .withFilter { bindings containsKey _ == false }
            // remove any new bindings
          .foreach { key =>
            ctx removeAttribute (key, ScriptContext.ENGINE_SCOPE)
           }

        // reset to original bindings
        _bindings.asInstanceOf[util.Map[String,AnyRef]]
                 .putAll(bindings)
      }

  /**
   * Perform all post-evaluation actions.
   *
   * This caches the current state of bindings in the engine, and then calls
   * [[cleanUp()]] to ensure that any new state  was removed. It then returns
   * a new [[ScriptMonad]] containing the new bindings cached prior to cleaning
   * up the engine (the ''b&prime;'' state).
   * @return A new [[ScriptMonad]] instance wrapping the ''b&prime;'' bindings
   *         state of the engine.
   */
  @inline private[this] def postEval(): Try[ScriptMonad]
    = Try {
        val b_prime = immutable.Map[String,AnyRef]() ++
          engine.getBindings(ScriptContext.ENGINE_SCOPE)
                .asScala
        cleanUp()
        new ScriptMonad(engine, b_prime)
      }

  /**
   * Evaluate a script from a String.
   * @param script the script to evaluate
   * @return a [[scala.util.Try Try]] containing the return value from the
   *         script or a [[javax.script.ScriptException ScriptException]]
   *         if the script could not be evaluated.
   */
  def apply(script: String): Result
    = compile(script) match {
        case Some(cs) => apply(cs)
        case None     => Try(engine eval script) flatMap { result =>
          postEval() map { (_, Option(result)) }
        }
      }

  def apply(script: CompiledScript): Result
    = Try(script eval) flatMap { result =>
        postEval() map { (_, Option(result)) }
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
  def apply(file: FileHandle): Result
    = compile(file) match {
        case Some(thing) => thing flatMap apply _
        case None        => file.read flatMap { stream =>
          val script = new BufferedReader(new InputStreamReader(stream))
          Try {
            engine eval script
          } flatMap { result =>
            postEval() map { (_, Option(result)) }
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
    = Try {
        Option( _bindings.put(name, value) )
      } map { result =>
        // mirror changes in our map
        bindings put (name, value)
        result
      }

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
    = Try( Option(ctx getAttribute name) )

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
    = Try { // try to remove the value from the script context
        Option( ctx removeAttribute (name, ScriptContext.ENGINE_SCOPE) )
      } map { result =>
        // mirror the changes in our map transactionally
        bindings remove name
        result
      }

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
