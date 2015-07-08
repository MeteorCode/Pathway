package com.meteorcode.pathway.model
import scala.language.implicitConversions

/**
 * Bundles together an x- and y-coordinate on a Cartesian plane, such as a dungeon grid.
 *
 * @deprecated This seems more like a job for a [[scala.Tuple2]] (Integer, Integer)
 * @param x the x-value
 * @param y the y-value
 */
case class GridCoordinates(x: Int, y: Int) {

  def getX: Int = x

  def getY: Int = y

  def setX(newX: Int): GridCoordinates = this.copy(x = newX)

  def setY(newY: Int): GridCoordinates = this.copy(y = newY)

  def + (other: GridCoordinates) = this.copy(
    x = x + other.x,
    y = y + other.y)

  def - (other: GridCoordinates) = this.copy(
    x = x - other.x,
    y = y - other.y)

  override def toString: String = s"($x, $y)"

  override def equals(other: Any) = other match {
    case GridCoordinates(x2, y2) => x == x2 && y == y2
    case _ => false
  }
}

object GridCoordinatesImplicits {
  implicit def GridCoordinates2Tuple(gc: GridCoordinates): (Int, Int)
    = (gc.x, gc.y)
  implicit def Tuple2GridCoordinates(xy: (Int, Int)): GridCoordinates
    = xy match { case ((x: Int, y: Int)) => GridCoordinates(x,y) }
}
