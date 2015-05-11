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

  def getX = x

  def getY = y

  override def toString = s"($x, $y)"

  override def equals(other: Any) = other match {
    case GridCoordinates(x_2, y_2) => x_2 == x && y_2 == y
    case _ => false
  }
}

object GridCoordinatesImplicits {
  implicit def GridCoordinates2Tuple(gc: GridCoordinates): (Int, Int) = (gc.x, gc.y)
  implicit def Tuple2GridCoordinates(xy: (Int, Int)): GridCoordinates = new GridCoordinates(xy._1, xy._2)
}