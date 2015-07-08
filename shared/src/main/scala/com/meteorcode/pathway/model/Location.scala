package com.meteorcode.pathway.model

/**
 * Created by hawk on 6/28/15.
 */
trait Location {
  protected var coords: GridCoordinates
  protected var grid: Grid

  def setCoordinates(newCoords: GridCoordinates): Unit = this.coords = newCoords
  def getCoordinates: GridCoordinates = this.coords
  def tile: Tile = grid.getTileAt(getCoordinates)
  def x: Int = coords.x
  def y: Int = coords.y

  def locationString: String = s"at $coords on ${grid.getName}"
}
