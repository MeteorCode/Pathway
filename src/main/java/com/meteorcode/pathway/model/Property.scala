package com.meteorcode.pathway.model

import com.meteorcode.pathway.logging.LoggerFactory

import scala.collection.mutable.Subscriber

abstract class Property (initDrawID: Integer, initParent: Context){
  var parent = initParent
  var drawID = initDrawID
  protected val logger = LoggerFactory.getLogger
  if (this.parent != null) { parent.subscribe(this) }

  def this(initParent: Context) = this(null, initParent) //TODO: eventually, this will get a DrawID from the Grand Source of All DrawIDs
  def this() = this(null, null) //TODO: eventually, this will get a DrawID from the Grand Source of All DrawIDs
  def this(initDrawID: Integer) = this (initDrawID, null)

  def getDrawID = drawID
  def setDrawID(newID: Integer) { drawID = newID }

  def eval(script: String) = parent.eval(script)

 /**
  * Move the Property from the currently-occupied [[com.meteorcode.pathway.model.Context]] to a new Context.
  *
  * Note that this assumes a Property may only occupy one Context at a
  * time. I'm assuming that this is correct behaviour, let me know if it
  * needs to be modified.
  *
  * @param newContext
  *            the [[com.meteorcode.pathway.model.Context]] this Property is entering
  */
  def changeContext(newContext: Context) {
    if (parent != null)
      parent.unsubscribe(this)
    parent = newContext
    parent.subscribe(this)
  }

  /**
   * This method is triggered when the property is evented.
   * This should be filled with the correct behaviour by scripts that create Properties.
   * @param publishedBy the [[com.meteorcode.pathway.model.Context]] on which the triggering event was pumped
   * @param event the [[com.meteorcode.pathway.model.Event]] eventing this Property.
   */
  def onEvent(event: Event, publishedBy: Context): Boolean
  // This method is triggered when the Property is evented.
  // This will be filled in by the script that creates the Property.
}