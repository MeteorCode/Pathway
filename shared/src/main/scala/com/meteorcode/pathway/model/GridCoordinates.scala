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

  // Why does this have getters and setters? What the fuck. Is this just
  // some kind of Java-placating cruft or am I just that stupid?
  def getX: Int = x

  def getY: Int = y

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
