package com.meteorcode.pathway
package model

import javax.script.CompiledScript

import com.meteorcode.pathway.io.FileHandle
import com.meteorcode.pathway.script.ScriptMonad

import scala.collection.mutable
import scala.util.{Failure, Try}

/**
 * Created by hawk on 8/10/15.
 */
class Event (
  private[this] val context: ScriptMonad,
  private[this] val script: CompiledScript,
  private[this] val _children: mutable.Set[Event] = mutable.Set()
) {

  private[this] var valid
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
  final def evalEvent(): Try[AnyRef]
    = context(script)

  /**
   * Spawns a new Event as a child of this event
   * @param childScript a [[CompiledScript]] containing the child event's
   *                    scripted behaviour
   * @return a new Event that is a child of this event
   */
  def child(childScript: CompiledScript): Event
    = { val c = new Event(context, childScript)
        children += c
        c
      }

  /**
   * Spawns a new Event as a child of this event
   * @param childScript a [[String]] containing the child event's
   *                    scripted behaviour
   * @return a new Event that is a child of this event
   */
  def child(childScript: String): Event
    = context.compile(childScript)
        .map { child _ }
        .getOrElse(throw new IllegalStateException(
          """Attempted to compile a script, but the context was
            | not capable of compiling scripts""".stripMargin))

  /**
   * Spawns a new Event as a child of this event from a [[FileHandle]]
   *
   * Since the script for the new event's behaviour is stored on the
   * filesystem, this method is tainted by the exceptional nature of IO
   * and returns a [[scala.util.Try Try]].
   *
   * @param childScript a [[String]] containing the child event's
   *                    scripted behaviour
   * @return a new Event that is a child of this event
   */
  def child(childScript: FileHandle): Try[Event]
    = context.compile(childScript)
        .map { script => script map { child _ } }
        .getOrElse(Failure(new IllegalStateException(
          """Attempted to compile a script, but the context was
            | not capable of compiling scripts""".stripMargin)))
  }
