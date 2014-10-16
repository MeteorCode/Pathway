package com.meteorcode.pathway

import scala.collection.mutable

/**
 * Scala re-implementation of Max's ClobberTable
 * Created by hawk on 10/15/14.
 */
class ForkTable[K, V](val parent: ForkTable[K, V] = null) extends mutable.HashMap[K, V] {
  val whiteouts = mutable.Set[K]()

  override def put(key: K, value: V): Option[V] = {
    val e = findOrAddEntry(key, value)
    if (whiteouts contains key) whiteouts -= key
    if (e eq null) None
    else {
      val v = e.value
      e.value = value
      Some(v)
    }
  }

  override def get(key: K): Option[V] = {
    if (whiteouts contains key) None
    val e = findEntry(key)
    if (e eq null) {
      if (parent != null) parent.get(key)
      else None
    }
    else Some(e.value)
  }

  def fork(): ForkTable[K, V] = new ForkTable[K,V](parent = this)

}
