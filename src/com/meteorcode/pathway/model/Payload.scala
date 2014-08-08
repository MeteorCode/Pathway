package com.meteorcode.pathway.model
import java.util.Map
import scala.collection.immutable.{
  Map,
  HashMap,
  Set,
  HashSet
}
import scala.collection.JavaConverters._

class Payload(initialMap: scala.collection.Map[String, Object], tile: Tile) {
  var map = initialMap
  var stamps = Set.empty[Property]
  val location = tile

  def this() = this(scala.collection.immutable.Map.empty[String, Object], null)
  def this(tile: Tile) = this(scala.collection.immutable.Map.empty[String, Object], tile)
  def this(initialMap: scala.collection.immutable.Map[String, Object]) = this(initialMap, null)
  def this(initialMap: java.util.Map[String, Object]) = this(initialMap.asScala, null)
  def this(initialMap: java.util.Map[String, Object], tile: Tile) = this(initialMap.asScala, tile: Tile)

  /**
   *
   * Stamps this Payload with a [[com.meteorcode.model.Property]], marking that that Property has "seen"
   * this Payload.
   *
   * A Property may only stamp a Payload once; attempting to stamp the same
   * Payload multiple times will do nothing past the initial stamping.
   *
   * @param stampedBy
   *            the [[com.meteorcode.model.Property]] stamping this event.
   */
  def stamp(stampedBy: Property) = stamps += stampedBy
  def unstamp(stampedBy: Property) = stamps -= stampedBy
  /**
   * Checks to see if this Payload has been stamped by a [[com.meteorcode.model.Property]], marking that
   * that Property has "seen" this Payload
   *
   * @param stampedBy
   *            the [[com.meteorcode.model.Property]] who's stamp is being searched for
   * @return true if that Property's stamp is present.
   */
  def stampExists(stampedBy: Property) = stamps.contains(stampedBy)
  
  def where = location
  def x: Integer = { if (location == null) { null } else location.getPosition().getX }
  def y: Integer = { if (location == null) { null } else location.getPosition().getY }

  def +(kv: (String, Object)) = map += kv
  def ++(addition: scala.collection.immutable.Map[String, Object]) = map ++= addition
  def ++(addition: java.util.Map[String, Object]) = map ++= addition.asScala
  def patch(kv: (String, Object)) = map += kv
  def patch(key: String, value: Object) = map += (key -> value)
  def patch(addition: scala.collection.immutable.Map[String, Object]) = map ++= addition
  def patch(addition: java.util.Map[String, Object]) = map ++= addition.asScala
  def -(key: String) = map - key
  def remove(key: String) = map - key
  def contains(key: String): Boolean = map.contains(key)
  def get(key: String): Object = { map.get(key).getOrElse(None) }
  def toMap = map.asJava
}