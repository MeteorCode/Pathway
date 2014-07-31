package com.meteorcode.pathway.model

import java.util.{
  Deque,
  ArrayDeque,
  List,
  ArrayList
}
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
 * <p>
 * A Context in which Event stacks are evaluated.
 * </p>
 *
 * <p>
 * The EventStack is backed by a {@link java.util.Deque Deque} rather than a
 * {@link java.util.Stack Stack}, primarily so that we can make shallow copies
 * for the {@link com.meteorcode.spaceshipgame.model.Context#viewEventStack()
 * viewEventStack() method}; however, Context only exposes Stack operations in
 * its' public methods.
 * </p>
 *
 * @author Hawk Weisman <hawk.weisman@gmail.com>
 *
 */
class Context(name: String) {

  protected var eventStack = Stack[Event]()
  protected var gameObjects = HashSet[GameObject]()
  protected var properties = HashSet[Property]()
  private var beanshell: ScriptContainer = new ScriptContainerFactory getNewInstance
  // TODO: This should really be requested from a global
  // ScriptContainerFactory instance, but since that's not available, I'm
  // doing it like this so that the class will run and be testable.

  def injectObject(name: String, toInject: Object) = beanshell.injectObject(name, toInject)
  def removeObject(name: String) = beanshell.removeObject(name)

  /**
   * @return A list of top-level GameObjects in this Context
   */
  def getGameObjects: java.util.List[GameObject] = {
    var result: java.util.List[GameObject] = new ArrayList[GameObject]
    result.addAll(gameObjects)
    return result
  }
  def removeGameObject(g: GameObject) = gameObjects -= g
  def addGameObject(g: GameObject) = gameObjects += g
  def subscribe(p: Property) = properties += p 
  def unsubscribe(p: Property) = properties -= p 

  /**
   * Evals a BeanShell expression against this Context's ScriptContainer
   * @throws ScriptException
   */
  @throws(classOf[ScriptException])
  def eval(script: String) = beanshell.eval(script)
  
  /**
   * Evals a file against this Context's ScriptContainer
   * @throws ScriptException
   */
  @throws(classOf[ScriptException])
  def eval(file: FileHandle) = beanshell.eval(file)

  /**
   * @return A shallow copy of the current EventStack.
   */
  def viewEventStack(): Deque[Event] = {
    var result: Deque[Event] = new ArrayDeque[Event]
    for (e <- eventStack)
      result.push(e)
    return result
  }

  /**
   * Fires an Event onto the EventStack corresponding to this Context.
   *
   * @param e the Event fired onto the EventStack;
   */
  def fireEvent(e: Event) {
    e.setTarget(this)
    eventStack.push(e)
  }

  /**
   * <p>Pops off the top {@link com.meteorcode.spaceshipgame.model.Event} in the stack, 
   * notifies any subscribed Properties, and then evaluates that Event.</p>
   * <p>Each Property is given the opportunity to {@link com.meteorcode.spaceshipgame.model.Event#invalidate()} 
   * the Event when they are notified. If no Property invalidates the Event, it will be evaluated immediately 
   * after all Properties are notified.</p>
   * @throws ScriptException if the Context's associated ScriptContainer encounters an error when evaluating the Event.
   */
  @throws(classOf[ScriptException])
  def pump {
    if (!eventStack.isEmpty()) {
      val e = eventStack.top
      // publish top event to all subscribed Properties
      for (p <- properties if e isValid)
        if(!p.onEvent(e, this)) return
      // if no Property invalidated the top event, then we can evaluate it.
      if (e == eventStack.top) {
        if (eventStack.top isValid) {
          eventStack.pop evalEvent
        } else {
          eventStack.pop
        }
      }
    }
  }

  override def toString(): String = name;
}
