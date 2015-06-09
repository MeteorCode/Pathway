package com.meteorcode.pathway.model
import scala.collection.mutable
import scala.collection.JavaConverters._

class Payload(initialMap: scala.collection.Map[String, Object], tile: Tile) {
  val map = mutable.Map() ++ initialMap
  val stamps = mutable.Set.empty[Property]
  val location = tile

  def this() = this(scala.collection.immutable.Map.empty[String, Object], null)
  def this(tile: Tile) = this(scala.collection.immutable.Map.empty[String, Object], tile)
  def this(initialMap: scala.collection.immutable.Map[String, Object]) = this(initialMap, null)
  def this(initialMap: java.util.Map[String, Object]) = this(initialMap.asScala, null)
  def this(initialMap: java.util.Map[String, Object], tile: Tile) = this(initialMap.asScala, tile: Tile)

  /**
   *
   * Stamps this Payload with a [[com.meteorcode.pathway.model.Property]], marking that that Property has "seen"
   * this Payload.
   *
   * A Property may only stamp a Payload once; attempting to stamp the same
   * Payload multiple times will do nothing past the initial stamping.
   *
   * @param stampedBy
   *            the [[com.meteorcode.pathway.model.Property]] stamping this event.
   */
  def stamp(stampedBy: Property): Unit = stamps += stampedBy
  def unstamp(stampedBy: Property): Unit = stamps -= stampedBy

  /**
   * Attempts to stamp this Payload with a [[com.meteorcode.pathway.model.Property]] if this Payload has not already been
   * stamped by that Property, and returns false if the Payload has already been stamped.
   * @param stampedBy the [[com.meteorcode.pathway.model.Property]] stamping this event.
   * @return false if this Payload was already stamped by that Property, true if not
   */
  def tryToStamp (stampedBy: Property): Boolean = if (stampExists(stampedBy)) {
      false
    } else {
      stamp(stampedBy)
      true
    }
  /**
   * Checks to see if this Payload has been stamped by a [[com.meteorcode.pathway.model.Property]], marking that
   * that Property has "seen" this Payload
   *
   * @param stampedBy
   *            the [[com.meteorcode.pathway.model.Property]] who's stamp is being searched for
   * @return true if that Property's stamp is present.
   */
  def stampExists(stampedBy: Property): Boolean = stamps.contains(stampedBy)

  def where: Tile = location
  def x: Integer = { if (location == null) { null } else location.getPosition.getX }
  def y: Integer = { if (location == null) { null } else location.getPosition.getY }

  def +(kv: (String, Object)): Unit = map += kv
  def ++(addition: scala.collection.immutable.Map[String, Object]): Unit = map ++= addition
  def ++(addition: java.util.Map[String, Object]): Unit = map ++= addition.asScala
  def patch(kv: (String, Object)): Unit = map += kv
  def patch(key: String, value: Object): Unit = map += (key -> value)
  def patch(addition: scala.collection.immutable.Map[String, Object]): Unit = map ++= addition
  def patch(addition: java.util.Map[String, Object]): Unit = map ++= addition.asScala
  def -(key: String): Unit = map - key
  def remove(key: String): Unit = map - key
  def contains(key: String): Boolean = map.contains(key)
  def get(key: String): Object = { map getOrElse (key, None) }
  def toMap: java.util.Map[String,Object] = map.asJava
}
