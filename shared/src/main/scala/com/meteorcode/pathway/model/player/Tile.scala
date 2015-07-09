package com.meteorcode.pathway.model

class Tile(x: Int, y: Int, protected[model] var grid: Grid)
  extends Location {

  protected var props: Set[TileProperty]
    = Set()
  protected var engProps: Set[TileProperty]
    = Set()
  protected var occupier: Option[TileEntity]
    = None

  protected var coords: GridCoordinates
    = new GridCoordinates(x,y)

  def occupied: Boolean = occupier.isDefined

  def addProperty(p: TileProperty): Unit
    = props = props + p; grid.addProperty(p)

}
