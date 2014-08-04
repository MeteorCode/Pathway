package com.meteorcode.pathway.model

import scala.collection.mutable.Subscriber

abstract class Property (initDrawID: Integer, initParent: Context){
  var parent = initParent
  var drawID = initDrawID
  if (this.parent != null) { parent.subscribe(this) }

  def this(initParent: Context) = this(null, initParent) //TODO: eventually, this will get a DrawID from the Grand Source of All DrawIDs
  def this() = this(null, null) //TODO: eventually, this will get a DrawID from the Grand Source of All DrawIDs
  def this(initDrawID: Integer) = this (initDrawID, null)

  def getDrawID = drawID
  def setDrawID(newID: Integer) { drawID = newID }

  def eval(script: String) = parent.eval(script)

 /**
  * <p>Move the Property from the currently-occupied Context to a new Context.</p>
  * <p>Note that this assumes a Property may only occupy one Context at a
  * time. I'm assuming that this is correct behaviour, let me know if it
  * needs to be modified.</p>
  *
  * @param newContext
  *            the Context this Property is entering
  */
  def changeContext(newContext: Context) {
    if (parent != null)
      parent.unsubscribe(this)
    parent = newContext
    parent.subscribe(this)
  }

  /**
   * This method is triggered when the property is evented. This should be filled with the correct behaviour by scripts that create Properties.
   * @param publishedBy the Context on which the triggering event was pumped
   * @param event the Event object
   */
  def onEvent(event: Event, publishedBy: Context): Boolean
  // This method is triggered when the Property is evented.
  // This will be filled in by the script that creates the Property.
}