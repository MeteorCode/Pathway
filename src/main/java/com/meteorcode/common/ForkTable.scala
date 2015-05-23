package com.meteorcode.common

import scala.collection.{AbstractMap, DefaultMap, mutable}

/**
 * Scala re-implementation of Max's ClobberTable
 *
 *
 * Created by hawk on 10/15/14.
 */

class ForkTable[K, V](protected var parent: ForkTable[K,V] = null,
                      protected var children: List[ForkTable[K,V]] = Nil) extends AbstractMap[K, V] with DefaultMap[K, V]{
  val whiteouts = mutable.Set[K]()
  val back = mutable.HashMap[K, V]()

  def put(key: K, value: V): Option[V] = {
    if (whiteouts contains key) whiteouts -= key
    back.put(key, value)
  }

  /**
   * @return true if this is the root-level, false if it is not
   */
  def root: Boolean = parent == null
  /**
   * @return true if this is a bottom-level leaf, false if it is not
   */
  def leaf: Boolean = parent != null && (children isEmpty)
  def getParent = parent
  def getChildren = children

  private def removeChild(other: ForkTable[K, V]): Unit = {
    this.children = children.filter({other != _})
  }

  private def addChild(other: ForkTable[K, V]): Unit = {
    this.children = this.children :+ other
  }

  def chainSize: Int = if (root) { size } else { size + parent.chainSize }

  /**
   * Change the parent corresponding to this scope.
   * @param nParent the new parent
   * @throws IllegalArgumentException if the specified parent was invalid
   */
  @throws[IllegalArgumentException]("if the specified parent was invalid")
  def reparent(nParent: ForkTable[K, V]) = if (nParent == this) {
    throw new IllegalArgumentException ("Scope attempted to mount itself as parent!")
  } else {
    if (this.parent != null) {
      val oldParent = this.parent
      oldParent.removeChild (this)
    }
    nParent.addChild (this)
  }

  override def get(key: K): Option[V] = if (whiteouts contains key) {
    None
  } else if (this.contains(key)) {
    back get key
  } else if(parent != null && (parent chainContains key)) {
    parent get key
  } else {
    None
  }

  def remove(key: K): Option[V] = {
    if (back contains key) {
      back remove key
    } else {
      if (parent != null && (parent contains key)) {
        whiteouts += key
        parent get key
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
  def chainContains(key: K): Boolean = (back contains key) || ((!(whiteouts contains key)) && parent != null && (parent chainContains key))

  override def contains(key: K): Boolean = back contains key
  override def exists(p: ((K, V)) => Boolean) = back exists p

  override def apply(key: K) = back(key)

  /**
   * @return a new child of this scope
   */
  def fork: ForkTable[K, V] = {
    val c = new ForkTable[K, V](parent=this)
    children = children :+ c
    c
  }

  override def toString = this.prettyPrint(0)

  def prettyPrint(indentLevel: Int) = (" "*indentLevel) + this.keys.foldLeft[String](""){(acc, key) => acc + "\n" + (" " * indentLevel) + s"$key ==> ${this.get(key).getOrElse("")}"}
}