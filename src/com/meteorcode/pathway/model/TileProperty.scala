
package com.meteorcode.pathway.model

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
abstract class TileProperty(
                             location: Tile,
                             initDrawID: Integer,
                             context: Context) extends Property(initDrawID, context) {

	val payload: Payload = new Payload(location)
	def x = payload.x
	def y = payload.y

	/**
	 * Constructor for a TileProperty with an unknown drawID (a new TileProperty).
	 *
	 * The drawID should be assigned by the Grand Source of All DrawIDs.
	 *
	 *
	 * @param location The [[com.meteorcode.pathway.model.Tile]] this property is attached to.
	 * @param context the [[com.meteorcode.pathway.model.Context]] within which this Property may
   *                [[com.meteorcode.pathway.model.Property.eval]] scripts. This should be the script context of
   *                whomever owns the property.
	 */
	def this(location: Tile, context: Context) = this(location, null, context)
}
