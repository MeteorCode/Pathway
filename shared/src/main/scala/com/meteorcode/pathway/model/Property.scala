package com.meteorcode.pathway.model

import com.meteorcode.pathway.logging.Logging

abstract class Property (initDrawID: Option[Integer] = None,
                         var parent: Option[Context] = None)
 extends Logging
 with Drawable {
  var drawID = initDrawID
    .getOrElse(???) //TODO: eventually, this will get a DrawID from the Grand Source of All DrawIDs

  parent foreach (_ subscribe this)

  def getParent: Context = parent orNull
  // this exists for Java api compatibility only
  // and I am sorry
  // ~ hawk

  def eval(script: String): AnyRef = parent map (_ eval script ) orNull // todo: ugh

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
  def changeContext(newContext: Context): Unit = {
    parent foreach( _ unsubscribe this )
    parent = Some(newContext)
    parent foreach ( _ subscribe this )
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
