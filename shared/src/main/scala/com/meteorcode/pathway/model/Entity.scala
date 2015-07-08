package com.meteorcode.pathway.model

import GridCoordinatesImplicits._

class Entity(protected var gameID: Option[Long] = None,
             entityName: Option[String]         = None)
  extends GameObject(gameID) {

  val name: String = entityName.getOrElse(this.getClass.getSimpleName)

  def this(gameID: Long, name: String) = this(Some(gameID), Some(name))

  if (name.isEmpty) name = Some(this.getClass.getSimpleName)
  override def toString: String = s"Entity $name"

}

class TileEntity(
                  protected var grid: Grid,
                  protected var coords: GridCoordinates,
                  gameID: Option[Long] = None,
                  name: Option[String] = None)
 extends Entity(gameID, name)
 with Location {

  def this( grid: Grid,
            tile: Tile,
            gameID: Option[Long],
            name: Option[String]) =
      this( grid,
            tile.getPosition,
            gameID,
            name)

  override def toString: String
    = s"${super.toString} $locationString"

}
