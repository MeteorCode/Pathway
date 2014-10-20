package com.meteorcode.pathway

import scala.collection.mutable

/**
 * Scala re-implementation of Max's ClobberTable
 * Created by hawk on 10/15/14.
 */
class ForkTable[K, V](val parent: ForkTable[K, V] = null) extends AbstractMap[K, V] {
  val whiteouts = mutable.Set[K]()
  val back      = mutable.HashMap[K, V]()

  override def put(key: K, value: V): Option[V] = {
    if (whiteouts contains key) whiteouts -= key
    back.put(key, value)
  }

  override def get(key: K): Option[V] = if (whiteouts contains key) {
    None
  } else if (parent != null && parent contains key ) {
    parent get key
  } else {
    back get key
  }

  override def remove(key: K): Option[V] = {
    if (back contains key) {
      back remove key
    }
    else {
      if (parent contains key) whiteouts += key
      None
    }
  }

  override def +=(kv: (K, V)): ForkTable[K, V] = { put(key, value); this}
  override def -=(key :K): ForkTable[K, V] = { remove(key); this}
  override def iterator = back.iterator
  override def contains(key: K): Boolean = back contains key || (parent != null && (parent contains key))

  def fork(): ForkTable[K, V] = new ForkTable[K,V](parent = this)
  def apply(key: K) = back(K)

}
