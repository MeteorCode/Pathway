package com.meteorcode.pathway.model

/**
 * Bundles together an x- and y-coordinate on a Cartesian plane, such as a dungeon grid.
 *
 * @deprecated This seems more like a job for a [[scala.Tuple2]] (Integer, Integer)
 * @param x the x-value
 * @param y the y-value
 */
class GridCoordinates(val x: Integer, val y: Integer) {
  def this(xy: (Integer, Integer)) = this(xy._1, xy._2)

  def getX = x
  def getY = y

  override def toString() = "(" + x + ", " + y + ")"
  override def equals(other: Any) = {
    if (other.isInstanceOf[GridCoordinates]) {
      (this.x == other.asInstanceOf[GridCoordinates].getX && this.y == other.asInstanceOf[GridCoordinates].getY)
      } else false }
}

object GridCoordinatesImplicits {
  implicit def GridCoordinates2Tuple(gc: GridCoordinates): (Int, Int) = (gc.x, gc.y)
  implicit def Tuple2GridCoordinates(xy: (Int, Int)): GridCoordinates = new GridCoordinates(xy._1, xy._2)
}