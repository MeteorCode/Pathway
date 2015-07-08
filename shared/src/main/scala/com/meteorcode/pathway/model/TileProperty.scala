package com.meteorcode.pathway.model
import model.DrawID

/**
 * Constructor for a TileProperty with a known drawID.
 *
 * @param initDrawID
 *            the Property's initial drawID
 * @param location The [[com.meteorcode.pathway.model.Tile]] this property is attached to.
 * @param context the [[com.meteorcode.pathway.model.Context]] within which this Property may
 *                [[com.meteorcode.pathway.model.Property.eval]] scripts. This should be the script context of
 *                whomever owns the property.
 */
abstract class TileProperty(protected var coords: GridCoordinates,
                            protected var grid: Grid,
                            initDrawID: Option[DrawID] = None,
                            context: Option[Context]   = None)
  extends Property(initDrawID, context)
  with Location {

  def this( tile: Tile,
            grid: Grid,
            initDrawID: Option[DrawID],
            context: Option[Context])
    = this( tile.getPosition,
            grid,
            initDrawID,
            context)

	val payload: Payload = new Payload(tile)

}
