
package com.meteorcode.pathway.model

	/**
	 * Constructor for a TileProperty with a known drawID.
	 *
	 * @param drawID
	 *            the Property's drawID
	 * @param context
	 *            the script context within which this Property may eval()
	 *            scripts. This should be the script context of whomever owns
	 *            the property.
	 */
abstract class TileProperty(location:Tile, initDrawID: Integer, initParent: Context) extends Property(initDrawID, initParent) {
	var payload = new Payload(location)
	def x = payload.x
	def y = payload.y

	/**
	 * Constructor for a TileProperty with an unknown drawID (a new TileProperty).
	 *
	 * <p>
	 * The drawID should be assigned by the Grand Source of All DrawIDs.
	 * </p>
	 *
	 * @param drawID
	 *            the Property's drawID
	 * @param context
	 *            the script context within which this Property may eval()
	 *            scripts. This should be the script context of whomever owns
	 *            the property.
	 */
	def this(location: Tile, context: Context) = this(location, null, context)
}
