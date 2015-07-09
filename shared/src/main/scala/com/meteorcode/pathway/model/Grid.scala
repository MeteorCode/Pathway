package com.meteorcode.pathway.model

class Grid( val size: Int,
            initName: Option[String] = None,
            initContext: Option[Context] = None
){
  val name: String
    = initName.getOrElse(this.getClass.getSimpleName())

  private val context: Context
    = initContext.getOrElse(new Context(name))

  val grid: Array[Array[Tile]]
    = Array.ofDim[Tile](size,size)

	/**
	 * Returns the tile at the specified position.
	 * @param 	x	the x-position of the target tile
	 * @param 	y	the y-position of the target tile
	 * @return the tile at the specified position
	 */
  def tileAt(x: Int, y: Int): Tile
    = grid(x)(y)

  def tileAt(xy: GridCoordinates): Tile
    = grid(xy.x)(xy.y)

  def setTileAt(x: Int,y: Int, Tile): Unit
    = grid(x)(y) = tile; tile.grid = Some(this)

  def addProperty(p: Property): Unit
    = context addProperty p

  def removeProperty(p: Property): Unit
    = context removeProperty p

}
