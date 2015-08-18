package com.meteorcode.pathway
package script

import java.io.{BufferedReader, InputStreamReader}
import java.util
import javax.script._
import jdk.nashorn.api.scripting.{ScriptObjectMirror, NashornScriptEngineFactory, NashornScriptEngine}

import io.FileHandle

import scala.util.Try
import scala.collection.JavaConverters.{
  mapAsJavaMapConverter,
  mapAsScalaMapConverter,
  asScalaSetConverter
}
import scala.languageFeature.postfixOps

/**
 * A ScriptContainer wraps a script interpreter and a set of variable bindings.
 *
 * Yeah, I hate the number of casts in this code, too. Blame Java for making
 * claims of being a "strongly-typed" language, and then having a type system
 * that isn't expressive enough for you to actually _do things_ in. Java loves
 * casts, and this code interacts with the (remarkably ugly) Nashorn Java API.
 *
 * Good job, Oracle.
 *
 * @author Hawk Weisman
 *
 * Created by hawk on 8/10/15.
 */
class ScriptMonad(
  private[this] val engine: NashornScriptEngine,
  private[this] val bindings: util.Map[String,AnyRef]
) {

  def this(engine: NashornScriptEngine, bindings: Map[String,AnyRef] = Map())
    = this(engine, bindings.asJava)

  def this(engine: NashornScriptEngine)
    = this(engine, new util.HashMap[String,AnyRef]())

  type Value = Option[AnyRef]
  type Result = Try[(ScriptMonad, Value)]
  type JMap = util.Map[String,AnyRef]

  private[this] val ctx: ScriptContext
    = engine.getContext

  private[this] val _bindings: ScriptObjectMirror
    = ctx.getBindings(ScriptContext.ENGINE_SCOPE)
         .asInstanceOf[ScriptObjectMirror] // I hate that I have to cast this.

  _bindings.asInstanceOf[JMap]
           .putAll(bindings)

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
    = ???

  /**
   * Evaluate a script from a String.
   * @param script the script to evaluate
   * @return a [[scala.util.Try Try]] containing the return value from the
   *         script or a [[javax.script.ScriptException ScriptException]]
   *         if the script could not be evaluated.
   */
  def apply(script: String): Result
    = apply( compile(script) )

  def apply(script: CompiledScript): Result
    = Try {
        script eval
      } map { result =>
        val b_prime = Map[String,AnyRef]() ++ _bindings.asScala

        _bindings.asInstanceOf[JMap]
                 .keySet
                 .asScala
                 // select only bindings that didn't exist previously
                 .filterNot { bindings containsKey _ }
                 // and delete them
                 .foreach   { _bindings removeMember _ }

        // reset to original bindings
        _bindings.asInstanceOf[JMap]
                 .putAll(bindings)

        (ScriptMonad(b_prime), Option(result))
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
    = compile(file) flatMap apply _

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
        Option( _bindings put (name, value) )
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
    = Try{ Option( _bindings get name ) }

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
        Option( _bindings remove name )
      } map { result =>
        // mirror the changes in our map transactionally
        bindings remove name
        result
      }

  def compile(script: String): CompiledScript
    = engine compile script

  def compile(file: FileHandle): Try[CompiledScript]
    = file.read map { stream =>
        val reader = new BufferedReader(new InputStreamReader(stream))
        engine compile reader
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
    = { val engine = factory.getScriptEngine
                            .asInstanceOf[NashornScriptEngine]
        new ScriptMonad(engine, bindings)
      }
}
