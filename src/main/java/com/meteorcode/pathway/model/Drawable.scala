package com.meteorcode.pathway.model

/**
 * Trait for a model with a corresponding view.
 *
 * @author Hawk Weisman
 *
 * Created by hawk on 6/28/15.
 */
trait Drawable {

  type DrawID = Integer //TODO: I thought these were going to be longs?

  var drawID: DrawID
  // TODO: I'm not sure if we actually want DrawIDs to be mutable. If the
  //       purpose of a DrawID is to uniquely identify a given model to the
  //       view, wouldn't that mean that the proper way to handle a change in
  //       the way we want to draw a model be to leave the draw ID the same and
  //       let the view figure it out?
  //             – Hawk, 6/28/15

  def getDrawID: DrawID = drawID
  def setDrawID(newID: DrawID) = drawID = newID

}
