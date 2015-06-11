package com.meteorcode.common

import scala.annotation.tailrec
import scala.collection.{AbstractMap, DefaultMap, mutable}

/**
 * An associative map data structure for representing scopes.
 *
 * A `ForkTable` functions similarly to a standard associative map
 * data structure (such as a [[HashMap]]), but with the ability to
 * fork children off of each level of the map. If a key exists in any
 * of a child's parents, the child will 'pass through' that key. If a
 * new value is bound to a key in a child level, that child will overwrite
 * the previous entry with the new one, but the previous `key` -> `value`
 * mapping will remain in the level it is defined. This means that the parent
 * level will still provide the previous value for that key.
 *
 * This implementation mixes in [[scala.collection.AbstractMap AbstractMap]],
 * so any operations you're used to using on Scala standard library map types
 * are available on `ForkTable` as well.
 *
 * This implementation of the `ForkTable` is based on one originally developed
 * for a compiler for the Decaf programming language. The data structure
 * was originally described by Max Clive.
 *
 * @author Hawk Weisman
 * @author Max Clive
 * @since v1.0.3
 *
 * Created by hawk on 10/15/14.
 */

class ForkTable[K, V](
  protected var parent: Option[ForkTable[K,V]] = None,
  protected var children: Seq[ForkTable[K,V]] = Nil
  ) extends AbstractMap[K, V] with DefaultMap[K, V]{

  val whiteouts = mutable.Set[K]()
  val back = mutable.HashMap[K, V]()

  /**
   * Inserts a key-value pair from the map.
   *
   * If the key already had a value present in the map, that
   * value is returned. Otherwise, [[scala.None None]] is returned.
   *
   * If the key is currently whited out (i.e. it was defined
   * in a lower level of the map and was removed) then it will
   * be un-whited out and added at this level.
   *
   * @param key a key
   * @param value the value to associate with the key
   * @return an [[scala.Option Option]] containing the previous
   *         value associated with that key, or [[scala.None None]]
   *         if the key was undefined
   */
  def put(key: K, value: V): Option[V] = {
    if (whiteouts contains key) whiteouts -= key
    back.put(key, value)
  }

  /**
   * @return true if this is the root-level, false if it is not
   */
  def root: Boolean = parent.isEmpty
  /**
   * @return true if this is a bottom-level leaf, false if it is not
   */
  def leaf: Boolean = parent.isDefined && (children.isEmpty)
  /**
   * @return an [[scala.Option Option]] containing a reference to
   *         the parent table, or [[scala.None None]] if this is
   *         the root level of the tree
   */
  def getParent: Option[ForkTable[K,V]] = parent

  /**
   * @return a sequence of this level's child ForkTables.
   */
  def getChildren: Seq[ForkTable[K,V]] = children

  private def removeChild(other: ForkTable[K, V]): Unit = {
    this.children = children.filter({other != _})
  }

  private def addChild(other: ForkTable[K, V]): Unit = {
    this.children = this.children :+ other
  }

  /**
   * @return the number of keys defined in this level plus all previous levels
   */
  def chainSize: Int =  size + (parent map (_.chainSize) getOrElse 0) // TODO: make tail-recursive?

  /**
   * Change the parent corresponding to this scope.
   * @param nParent the new parent
   * @throws IllegalArgumentException if the specified parent was invalid
   */
  @throws[IllegalArgumentException]("if the specified parent was invalid")
  def reparent(nParent: ForkTable[K, V]): Unit = if (nParent == this) {
    throw new IllegalArgumentException ("Scope attempted to mount itself as parent!")
  } else {
    parent.foreach{ _.removeChild(this)}
    parent = Some(nParent)
    nParent.addChild (this)
  }

  /**
   * Returns the value corresponding to the given key.
   *
   * @param  key the key to look up
   * @return a [[scala.Option Option]] containing the value of the
   *         key, or [[scala.None None]] if it is undefined.
   */
  @tailrec final override def get(key: K): Option[V] = whiteouts contains key match {
    case true  => None
    case false => back get key match {
      case value: Some[V] => value
      case None           => parent match {
        case None         => None
        case Some(thing)  => thing get key
      }
    }
  }

  /**
   * Removes a binding from the map and returns the value
   * corresponding to the given key.
   *
   * If the removed value exists in a lower level of the table,
   * it will be whited out at this level. This means that the entry
   * will be 'removed' at this level and this table will not provide
   * access to it, but the mapping will still exist in the level where
   * it was defined. Note that the key will not be returned if it is
   * defined in a lower level of the table.
   *
   * @param  key the key to remove.
   * @return a [[scala.Option Option]] containing the value of the
   *         key, or [[scala.None None]] if it is undefined.
   */
  def remove(key: K): Option[V] = if (back contains key) {
    back remove key
  } else {
    parent flatMap (_ get key) map {(v) => whiteouts += key; v}
  }

  def freeze: Unit = ???

  /** @return the number of entries in this level over the table.
   */
  override def size: Int = back.size

  /** @return an Iterator over all of the (key, value) pairs in the tree.
   */
  override def iterator: Iterator[(K,V)] = parent match {
    case None        => back.iterator // TODO: make tail-recursive?
    case Some(thing) => back.iterator ++ thing.iterator
  }

  /**
   * Returns true if this contains the selected key OR if any of its' parents contains the key
   * @param key the key to search for
   * @return true if this or any of its' parents contains the selected key.
   */
  @tailrec final def chainContains(key: K): Boolean = back contains key match {
    // Dear maintainer (or, more likely, future-me),
    //
    // I know this weird pattern matching thing seems needlessly complex,
    // but it's like this for tail-recursion reasons. If you think you
    // can express `chainContains()` more elegantly using `flatMap()` or
    // `orElse()`, yeah, you can, but the recursive call won't be in the
    // tail position any more.
    //
    // Trust me on this. I know LISP.
    //  ~ Hawk, 6/9/2015
    case true                            => true
    case false if whiteouts contains key => false
    case false                           => parent match {
      case None        => false
      case Some(thing) => thing chainContains key
    }
  }

  /**
   * @param  key the key to look up
   * @return true if this level contains a binding for the given key, false otherwise
   */
  override def contains(key: K): Boolean = back contains key

  /**
   * Search this level for a (key, value) pair matching a predicate.
   *
   * @param  p the predicate to search for
   * @return true if there exists a pair defined at this level for
   *         which the predicate holds, false otherwise.
   */
  override def exists(p: ((K, V)) => Boolean): Boolean = back exists p

  /**
   * Search the whole chain down from this level
   * for a (key, value) pair matching a predicate.
   *
   * @param  p the predicate to search for
   * @return true if there exists a pair for which the
   *         predicate holds, false otherwise.
   */
  @tailrec final def chainExists(p: ((K, V)) => Boolean): Boolean = back exists p match {
    case true  => true // this method could look much simpler were it not for `tailrec`
    case false => parent match {
      case None        => false
      case Some(thing) => thing chainExists p
    }
  }

  /**
   * Look up the given key
   * @param  key the key to look up
   * @return the value bound to that key.
   */
  override def apply(key: K): V = back(key)

  /**
   * Forks this table, returning a new `ForkTable[K,V]`.
   *
   * This level of the table will be set as the child's
   * parent. The child will be created with an empty backing
   * [[scala.collection.HashMap HashMap]] and no keys whited out.
   *
   * @return a new child of this scope
   */
  def fork(): ForkTable[K, V] = {
    val c = new ForkTable[K, V](parent=Some(this))
    children = children :+ c
    c
  }

 /**
  * @return a String representation of this ForkTable
  */
  override def toString(): String = this.prettyPrint(0)

  /**
   * Helper method for printing indented levels of a ForkTable
   *
   * @param indentLevel the level to indent to
   * @return a String representing this table indented at the specified level
   */
  def prettyPrint(indentLevel: Int): String = (" "*indentLevel) + this.keys.foldLeft(""){
    (acc, key) =>     //TODO: make tail-recursive?
      acc + "\n" + (" " * indentLevel) + s"$key ==> ${this.get(key).getOrElse("")}"
    }
}
