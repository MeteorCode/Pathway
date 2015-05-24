package com.meteorcode.pathway.model

/**
 * Bundles together an x- and y-coordinate on a Cartesian plane, such as a dungeon grid.
 *
 * @deprecated This seems more like a job for a [[scala.Tuple2]] (Integer, Integer)
 * @param x the x-value
 * @param y the y-value
 */
case class GridCoordinates(x: Integer, y: Integer) {
  def this(xy: (Integer, Integer)) = this(xy._1, xy._2)

  def getX = x

  def getY = y

  override def toString = s"($x, $y)"

  override def equals(other: Any) = other match {
    case GridCoordinates(x2, y2) => x == x2 && y == y2
    case _ => false
  }
}

object GridCoordinatesImplicits {
  implicit def GridCoordinates2Tuple(gc: GridCoordinates): (Int, Int) = (gc.x, gc.y)
  implicit def Tuple2GridCoordinates(xy: (Int, Int)): GridCoordinates = GridCoordinates(xy._1, xy._2)
}