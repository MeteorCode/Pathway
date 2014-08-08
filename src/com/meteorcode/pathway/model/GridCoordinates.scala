package com.meteorcode.pathway.model

/**
 * Bundles together an x- and y-coordinate on a Cartesian plane, such as a dungeon grid.
 *
 * @deprecated This seems more like a job for a [[scala.Tuple2]] (Integer, Integer)
 * @param x the x-value
 * @param y the y-value
 */
class GridCoordinates(val x: Integer, val y: Integer) {
  def getX = x
  def getY = y

  override def toString() = "(" + x + ", " + y + ")"
  override def equals(other: Any) = {
    if (other.isInstanceOf[GridCoordinates]) {
      (this.x == other.asInstanceOf[GridCoordinates].getX && this.y == other.asInstanceOf[GridCoordinates].getY)
      } else false }
}