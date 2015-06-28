package com.meteorcode.pathway.model

import GridCoordinatesImplicits._

class Entity(protected var gameID: Option[Long] = None,
             protected var name: Option[String] = None)
  extends GameObject(gameID) {

  if (name.isEmpty) name = Some(this.getClass.getSimpleName)

  def getName = new String(name.getOrElse("NamelessEntity (this shouldn't happen)"))
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
            gameID: Option[Long] = None,
            name: Option[String] = None) =
    this(   grid,
            tile.getPosition,
            gameID,
            name)

  override def toString: String = s"${super.toString} $locationString"

}