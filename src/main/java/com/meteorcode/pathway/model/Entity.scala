package com.meteorcode.pathway.model

import GridCoordinatesImplicits._

class Entity(
              protected var gameID: Option[Long],
              protected var coordinates: Option[(Int,Int)],
              protected var name: Option[String],
              protected var grid: Option[Grid]
                   ) extends GameObject(gameID) {

  def this() = this(None, None, None, None)
  def this(name: String) = this (None, None, Some(name), None)
  def this(gameID: Long, name: String) = this(Some(gameID), None, Some(name), None)
  def this(coords: (Int, Int), grid: Grid) = this(None, Some(coords),None,Some(grid))
  def this(coords: (Int, Int), grid: Grid, name: String) = this(None, Some(coords),Some(name),Some(grid))
  def this(gameID: Long, coords: (Int, Int), grid: Grid, name: String) = this(Some(gameID), Some(coords),Some(name),Some(grid))

  if (name.isEmpty) name = Some(this.getClass.getSimpleName)

  def setLocation (newCoords: GridCoordinates) = this.coordinates = Some(newCoords)
  def getCoordinates: GridCoordinates = coordinates match {
    case Some(thing) => thing
    case None => null
  }
  def getLocation: Tile = grid match {
    case Some(g) => g.getTileAt(getCoordinates)
    case None => null
  }
  def getName = new String(name.getOrElse("NamelessEntity (this shouldn't happen)"))
  override def toString: String = s"Entity $name ${coordinates.map(c => s"at $c").getOrElse("")}"

}