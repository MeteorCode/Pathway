package com.meteorcode.pathway.model
import model.GameID
import model.DrawID
import GridCoordinatesImplicits._

class Entity( protected var gameID: Option[GameID] = None,
              entityName: Option[String]           = None)
  extends GameObject(gameID) {

  val name: String
    = entityName.getOrElse(this.getClass.getSimpleName)

  def this(gameID: GameID, name: String) = this(Some(gameID), Some(name))

  override def toString: String = s"Entity $name"
}

class TileEntity( protected var grid: Grid,
                  protected var coords: GridCoordinates,
                  gameID: Option[GameID] = None,
                  drawID: Option[DrawID] = None,
                  name: Option[String] = None)
extends Entity(gameID, name)
  with Drawable
  with Location {

  override protected var _drawID: DrawID
    = drawID.getOrElse(???) // todo: get from the source of drawIDs

  def this( grid: Grid,
            tile: Tile,
            drawID: Option[DrawID],
            gameID: Option[GameID],
            name: Option[String])
    = this( grid,
            tile.coordinates,
            drawID,
            gameID,
            name)

  override def toString: String
    = s"${super.toString} $locationString"

}
