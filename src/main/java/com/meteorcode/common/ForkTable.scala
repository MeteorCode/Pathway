package com.meteorcode.common

import scala.collection.{AbstractMap, DefaultMap, mutable}

/**
 * Scala re-implementation of Max's ClobberTable
 *
 *
 * Created by hawk on 10/15/14.
 */
class ForkTable[K, V](override var parent: Option[ForkTable[K, V]] = None) extends AbstractMap[K, V] with DefaultMap[K, V] with Scope {
  val whiteouts = mutable.Set[K]()
  val back = mutable.HashMap[K, V]()

  def put(key: K, value: V): Option[V] = {
    if (whiteouts contains key) whiteouts -= key
    back.put(key, value)
  }


  override def get(key: K): Option[V] = if (whiteouts contains key) {
    None
  } else if (this.contains(key)) {
    back get key
  } else if(parent.isDefined && (parent.get chainContains key)) {
    parent.get get key
  } else {
    None
  }

  def remove(key: K): Option[V] = {
    if (back contains key) {
      back remove key
    } else {
      if (parent.isDefined && (parent.get contains key)) {
        whiteouts += key
        parent.get get key
      } else {
        None
      }
    }
  }

  override def iterator = back.iterator

  /**
   * Returns true if this contains the selected key OR if any of its' parents contains the key
   * @param key the key to search for
   * @return true if this or any of its' parents contains the selected key.
   */
  def chainContains(key: K): Boolean = (back contains key) || ((!(whiteouts contains key)) && parent.isDefined && (parent.get chainContains key))

  override def contains(key: K): Boolean = back contains key
  override def exists(p: ((K, V)) => Boolean) = back exists p

  override def apply(key: K) = back(key)

  override def fork: ForkTable[K, V] = new ForkTable[K, V](Some(this))

  override def toString = this.prettyPrint(0)

  def prettyPrint(indentLevel: Int) = (" "*indentLevel) + this.keys.foldLeft[String](""){(acc, key) => acc + "\n" + (" " * indentLevel) + s"$key ==> ${this.get(key).getOrElse("")}"}
}