package com.meteorcode.pathway.model

/**
 * Created by hawk on 6/28/15.
 */
trait Location {
  protected var coords: GridCoordinates
  protected var grid: Grid

  def coordinates_=(newCoords: GridCoordinates): Unit
    = this.coords = newCoords

  def coordinates: GridCoordinates
    = this.coords

  def tile: Tile = grid.getTileAt(getCoordinates)
  def x: Int = coords.x
  def y: Int = coords.y
  def x_=(x2: Int): Unit
    = coords.x = x2
  def y_=(y2: Int): Unit
    = coords.y = y2

  def locationString: String = s"at $coords on ${grid.getName}"
}
