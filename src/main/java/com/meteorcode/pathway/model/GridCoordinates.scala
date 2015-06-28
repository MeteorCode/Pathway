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
  def this(xy: (Int, Int)) = this(xy._1, xy._2)

  def getX: Int = x

  def getY: Int = y

  override def toString: String = s"($x, $y)"

  override def equals(other: Any) = other match {
    case GridCoordinates(x2, y2) => x == x2 && y == y2
    case _ => false
  }
}

object GridCoordinatesImplicits {
  implicit def GridCoordinates2Tuple(gc: GridCoordinates): (Int, Int) = (gc.x, gc.y)
  implicit def Tuple2GridCoordinates(xy: (Int, Int)): GridCoordinates = GridCoordinates(xy._1, xy._2)
}