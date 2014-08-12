package com.meteorcode.pathway.model

import GridCoordinatesImplicits._

class Entity(gameID: Long,
             protected var coordinates: GridCoordinates,
             protected var grid: Grid,
             protected var name: String) extends GameObject(gameID) {

  if (this.name == null) this.name = this.getClass.getSimpleName

  def this() = this(null, null, null, null)
  def this(gameID: Long) = this (gameID, null, null, null)
  def this(position: (Int, Int), grid: Grid) = this (null, position, grid, null)
  def this(name: String) = this(null, null, null, name)
  def this(gameID: Long, name:String) = this (gameID, null, null, name)
  def this(position: (Int, Int), grid: Grid, name: String) = this (null, position, location, name)

  def setLocation (xy: GridCoordinates) = this.coordinates = (xy.x, xy.y)
  def getCoordinates: GridCoordinates = new GridCoordinates(coordinates)
  def getLocation: Tile = grid.getTileAt(coordinates)

}