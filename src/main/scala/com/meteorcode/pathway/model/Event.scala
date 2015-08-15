package com.meteorcode.pathway
package model

import javax.script.CompiledScript

import com.meteorcode.pathway.io.FileHandle
import com.meteorcode.pathway.script.ScriptMonad

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import sun.plugin.dom.exception.InvalidStateException

import scala.collection.mutable
import scala.util.{Success, Failure, Try}

/**
 * Created by hawk on 8/10/15.
 */
class Event (
  val name: String,
  val script: String,
  private[this] val _children: mutable.Set[Event] = mutable.Set()
) {


  private[this] var compiled: Option[CompiledScript]
    = None

  private[this] var valid: Boolean
    = true

  def children: Set[Event]
    = _children.toSet

  def isValid: Boolean
    = valid

  final def invalidate(): Unit
    = this.valid = false; _children foreach { _.invalidate() }

  /**
   * Evaluate the event against a [[ScriptMonad]], returning
   * the result of evaluating the event's script.
   * @return a [[scala.util.Try Try]] containing the result of evaluating
   *         the event's script, or a [[javax.script.ScriptException]] if
   *         the script could not be evaluated.
   */
  final def evalEvent(context: ScriptMonad): Try[AnyRef]
    = if (compiled.isEmpty) {
        context.compile(script) match {
          case Some(cs) =>
            compiled = Some(cs)
            context(cs)
          case None => context(script)
        }
      } else context(script)

  /**
   * Spawns a new Event as a child of this event
   * @param childScript a [[CompiledScript]] containing the child event's
   *                    scripted behaviour
   * @return a new Event that is a child of this event
   */
  def child(childName: String, childScript: String): Event
    = { val c = new Event(childName, childScript)
        _children += c
        c
      }
}
object Event {
  def unapply(e: Event): Option[(String,String,Set[Event])]
    = Some((e.name, e.script, e.children))

}

object EventSerializer
extends CustomSerializer[Event](implicit format => ({
  case json: JValue => ???
}, {
  case Event(name,script,children) =>
    ("name" -> name) ~
    ("onEvent" -> script) ~
    ("children" -> children)
  })
)
