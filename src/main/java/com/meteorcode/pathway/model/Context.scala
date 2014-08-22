package com.meteorcode.pathway.model

import java.util.{
  Deque,
  ArrayDeque,
  List,
  ArrayList
}
import com.meteorcode.pathway.logging.LoggerFactory
import com.meteorcode.pathway.script.{
  ScriptContainer,
  ScriptContainerFactory,
  ScriptException
}
import com.meteorcode.pathway.io.FileHandle

import scala.collection.JavaConversions._
import scala.collection.mutable.Stack
import scala.collection.immutable.{
  HashSet,
  List
}
/**
 * A Context in which Event stacks are evaluated.
 *
 * @author Hawk Weisman <hawk.weisman@gmail.com>
 *
 */
class Context(protected var name: String) {

  def this() = this(null)
  if (name == null) name = this.getClass.getSimpleName

  protected var eventStack = Stack[Event]()
  protected var gameObjects = HashSet[GameObject]()
  protected var properties = HashSet[Property]()
  private val logger = LoggerFactory.getLogger
  private var beanshell: ScriptContainer = (new ScriptContainerFactory).getNewInstance()
  // TODO: This should really be requested from a global ScriptContainerFactory instance,
  // but since that's not available, I'm doing it like this so that the class will run and be testable.

  def injectObject(name: String, toInject: Object) = beanshell.injectObject(name, toInject)
  def removeObject(name: String) = beanshell.removeObject(name)

  /**
   * @return A list of top-level GameObjects in this Context
   */
  def getGameObjects: java.util.List[GameObject] = {
    var result: java.util.List[GameObject] = new ArrayList[GameObject]
    result.addAll(gameObjects)
    result
  }
  def removeGameObject(g: GameObject) = gameObjects -= g
  def addGameObject(g: GameObject) = gameObjects += g
  def subscribe(p: Property) = properties += p
  def unsubscribe(p: Property) = properties -= p
  def getName = this.name

  /**
   * Evals a BeanShell expression against this Context's ScriptContainer
   * @throws ScriptException
   */
  @throws(classOf[ScriptException])
  def eval(script: String) = {
    logger.log(this.name + "Context", "evaluating script:\n" + script)
    beanshell.eval(script)
  }

  /**
   * Evals a file against this Context's ScriptContainer
   * @throws ScriptException
   */
  @throws(classOf[ScriptException])
  def eval(file: FileHandle) = {
    logger.log(this.name + "Context", "evaluating script from file: " + file)
    beanshell.eval(file)
  }

  /**
   * @return A shallow copy of the current EventStack.
   */
  def viewEventStack(): Deque[Event] = {
    var result: Deque[Event] = new ArrayDeque[Event]
    for (e <- eventStack)
      result.push(e)
    result
  }

  /**
   * Fires an Event onto the EventStack corresponding to this Context.
   *
   * @param e the Event fired onto the EventStack;
   */
  def fireEvent(e: Event) {
    e.setTarget(this)
    logger.log(this.name + " Context", "fired event " + e)
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
  def pump {
    if (!eventStack.isEmpty()) {
      val e = eventStack.top
      // publish top event to all subscribed Properties
      for (p <- properties if e.isValid) {
        logger.log(this.name + " Context", "publishing" + e + " to " + p)
        if (!p.onEvent(e, this)) return
      }
      // if no Property invalidated the top event, then we can evaluate it.
      if (e == eventStack.top) {
        if (eventStack.top.isValid) {
          logger.log(this.name + " Context", eventStack.top + " is valid, evaluating")
          eventStack.pop.evalEvent
        } else {
          logger.log(this.name + " Context", eventStack.top + " is invalid, ignoring")
          eventStack.pop
        }
      }
    }
  }

  override def toString(): String = "[" + name + " Context" + "]" + viewEventStack()
}
