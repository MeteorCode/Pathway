package com.meteorcode.pathway.model

import java.util
import com.meteorcode.pathway.logging.Logging
import com.meteorcode.pathway.script.{
  ScriptContainer,
  ScriptContainerFactory,
  ScriptException
}
import com.meteorcode.pathway.io.scala_api.FileHandle

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.language.postfixOps

/**
 * A Context in which Event stacks are evaluated.
 *
 * @author Hawk Weisman <hawk.weisman@gmail.com>
 *
 */
class Context(contextName: Option[String] = None) extends Logging {

  def this(contextName: String) = this(Some(contextName))
  def this() = this(None)

  val name: String
    = contextName.getOrElse(this.getClass.getSimpleName)

  protected val eventStack = mutable.Stack[Event]()
  protected val gameObjects = mutable.Set[GameObject]()
  protected val properties = mutable.Set[Property]()
  private[this] val beanshell: ScriptContainer
    = (new ScriptContainerFactory).getNewInstance
  // TODO: This should really be requested from a global ScriptContainerFactory instance,
  // but since that's not available, I'm doing it like this so that the class will run and be testable.

  def injectObject(name: String, toInject: Object): Unit
    = beanshell.injectObject(name, toInject)
  def removeObject(name: String): Unit
    = beanshell.removeObject(name)

  private[this] def log(msg: String): Unit
    = logger.log(s"$name Context", msg)

  /**
   * @return A list of top-level GameObjects in this Context
   */
  def getGameObjects: util.List[GameObject] = {
    // TODO: shallow copy wouldn't be necessary if we used Scala immutable collections
    val result: util.List[GameObject] = new util.ArrayList[GameObject]
    result.addAll(gameObjects)
    result
  }
  def removeGameObject(g: GameObject): Unit = gameObjects -= g
  def addGameObject(g: GameObject): Unit = gameObjects += g
  def subscribe(p: Property): Unit = properties += p
  def unsubscribe(p: Property): Unit = properties -= p
  def getName: String = this.name

  /**
   * Evals a BeanShell expression against this Context's ScriptContainer
   * @throws ScriptException if an error occurs during script evaluation
   */
  @throws(classOf[ScriptException])
  def eval(script: String): AnyRef = {
    log(s"evaluating script:\n $script")
    beanshell.eval(script)
  }

  /**
   * Evals a file against this Context's ScriptContainer
   * @throws ScriptException if an error occurs during script evaluation
   */
  @throws(classOf[ScriptException])
  def eval(file: FileHandle): AnyRef = {
    log(s"evaluating script from file: $file")
    beanshell.eval(file)
  }

  /**
   * @return A shallow copy of the current EventStack.
   */
  def viewEventStack(): util.Deque[Event] = {
    val result: util.Deque[Event] = new util.ArrayDeque[Event]
    for (e <- eventStack)
      result.push(e)
    result
  }

  /**
   * Fires an Event onto the EventStack corresponding to this Context.
   *
   * @param e the Event fired onto the EventStack;
   */
  def fireEvent(e: Event): Unit = {
    e.setTarget(this)
    log(s"fired event $e")
    eventStack.push(e)
  }

  /**
   * Pops off the top [[com.meteorcode.pathway.model.Event]] in the stack,  notifies any subscribed Properties, and then
   * evaluates that Event.
   *
   * Each [[com.meteorcode.pathway.model.Property Property]] attached to this
   * [[com.meteorcode.pathway.model.Context Context]] s given the opportunity to
   * [[com.meteorcode.pathway.model.Event.invalidate invalidate()]] the Event when they are notified.
   * If no Property invalidates the Event, it will be evaluated once all Properties have been notified.
   *
   * @throws ScriptException if the Context's associated [[com.meteorcode.pathway.script.ScriptContainer]]
   *                         encounters an error when evaluating the Event.
   */
  @throws(classOf[ScriptException])
  def pump(): Unit = {
    if (eventStack nonEmpty) {
      val e = eventStack.top
      // publish top event to all subscribed Properties
      val eval = properties
        .takeWhile(_ => e.isValid)
        .foldRight(true){ (p, continue) =>
          if (continue) {
            log(s"publishing $e to $p")
            p.onEvent(e, this)
          } else false
        }
      // if no Property invalidated the top event, then we can evaluate it.
      if (e == eventStack.top && eval) {
        if (eventStack.top.isValid) {
          log(s"${eventStack.top} is valid, evaluating")
          eventStack.pop().evalEvent()
        } else {
          log(s"${eventStack.top} is invalid, ignoring")
          eventStack.pop()
        }
      }
    }
  }

  override def toString: String
    = s"[$name Context]$viewEventStack"
}