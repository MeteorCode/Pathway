package com.meteorcode.pathway.model

class Tile(x: Int, y: Int, protected[model] var grid: Grid)
  extends Location {

  private[this] var props: Set[TileProperty]
    = Set()
  private[this] var engProps: Set[TileProperty]
    = Set()
  private[this] var _entity: Option[TileEntity]
    = None
  protected var coords: GridCoordinates
    = new GridCoordinates(x,y)

  def occupied: Boolean = _entity isDefined

  def entity: Option[TileEntity] = _entity

  def entity_=(e: TileEntity): Unit
    = _entity = Some(e)

  def entity_=(o: Option[TileEntity]): Unit
    = _entity = o

  def addProperty(p: TileProperty): Unit
    = { props = props + p; grid.addProperty(p) }

}
