package com.meteorcode.pathway

/**
 *
 * A wish is much like a [[scala.concurrent.Future Future]]: In a sense, it represents a thing that we want to happen
 * that hasn't yet. It is, however, quite unlike a Future in that it does not represent a value which is being computed,
 * but instead an input that we wish to receive within some unspecified "window of opportunity".
 *
 * In essence, the Wish is a callback which can keep track of three things:
 * 1. Whether the wish is open to receiving a success
 * 2. A callback to call in the event of the wish's success
 * 3. Whether or not the wish succeeded (so that you can query it later in the event system)
 *
 * @author Hawk Weisman
 * Created by hawk on 10/7/14.
 */
class Wish[T] {
  def succeeded: Boolean = ??? //TODO: NYI
  def failed: Boolean = ??? //TODO: NYI
  def active: Boolean = ??? //TODO: NYI

  /** Activates the Wish IFF it has not been completed yet */
  def activate: Unit = ??? //TODO: NYI

  /** Deactivates the Wish temporarily, IFF it is active AND has not completed */
  def deactivate: Unit  = ??? //TODO: NYI

  /** Completes the Wish, signaling success IFF the Wish is currently active and has not completed */
  def succeed: Unit = ??? //TODO: NYI

  /** Completes the Wish, signaling failure ALWAYS, but only IFF the wish has not yet completed. */
  def fail: Unit = ??? //TODO: NYI

  /** Scala-style success callback using higher-order functions */
  def setOnSuccessCallback(callback: => Any) = ??? //TODO: NYI

  /** Scala-style failure callback using higher-order functions */
  def setOnFailureCallback(callback: => Any) = ??? //TODO: NYI

  /** Java-style success callback using [[com.meteorcode.pathway.Action Action]] interface */
  def setOnSuccessCallback(callback: Action) = ??? //TODO: NYI

  /** Java-style failure callback using [[com.meteorcode.pathway.Action Action]] interface */
  def setOnFailureCallback(callback: Action) = ??? //TODO: NYI
}

abstract class Action {
  //TODO: NYI
}