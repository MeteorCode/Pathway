package com.meteorcode.pathway.model

import GridCoordinatesImplicits._

class Entity(gameID: Option[Long]) extends GameObject(gameID) {
  protected var coordinates: (Int, Int) = _
  protected var grid: Grid = _
  protected var name: String = _

  def this() = {
    this(None)
    this.name = this.getClass.getSimpleName
  }
  def this(gameID: Long) = {
    this(Some(gameID))
    this.name = this.getClass.getSimpleName
  }
  def this(name: String) = {
    this(None)
    this.name = name
  }
  def this(gameID: Long, name:String) = {
    this(Some(gameID))
    this.name = name
  }
  def this(coords: (Int, Int), grid: Grid) = {
    this(None)
    this.coordinates = coords
    this.grid = grid
    this.name = this.getClass.getSimpleName
  }
  def this(coords: (Int, Int), grid: Grid, name: String) = {
    this(None)
    this.coordinates = coords
    this.grid = grid
    this.name = name
  }
  def this(gameID: Long, coords: (Int, Int), grid: Grid, name: String) = {
    this(Some(gameID))
    this.coordinates = coordinates
    this.grid = grid
    this.name = name
  }

  def setLocation (newCoords: GridCoordinates) = this.coordinates = newCoords
  def getCoordinates: GridCoordinates = coordinates
  def getLocation: Tile = grid.getTileAt(coordinates)
  def getName = new String(name)
  override def toString = "Entity " + getName + " at " + getCoordinates

}