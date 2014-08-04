package com.meteorcode.pathway.model;

import java.util.List;
import java.util.ArrayList;

/**
 * A tile on a game grid.
 * @author Hawk Weisman
 *
 */
public class Tile {
	private GridCoordinates position;

	public enum Type {
		EMPTY,
		METAL_WALL,
		ROCK_WALL,
		WATER,
		LAVA,
		FLOOR,
	}

	private Type type;
	private Entity entity;
	private List<TileProperty> engineeringProperties;
	private List<TileProperty> properties;

	/**
	 * Constructor for a Tile with a GridCoordinates
	 * @param position the GridCoordinates representing the tile's location
	 * @param type the Type of tile that this tile will be
	 */
	public Tile (GridCoordinates position, Type type) {
		this.position = position;
		this.type = type;
		this.entity = null;
	}

	/**
	 * Constructor for a Tile with integer coordinates
	 * @param x the tile's x-position
	 * @param y the tile's y-position
	 * @param type the Type of tile that this tile will be
	 */
	public Tile (int x, int y, Type type) {
		this.position = new GridCoordinates(x,y);
		this.type = type;
		this.entity = null;
	}

	/**
	 * Returns a GridCoordinates object containing this Tile's position.
	 * @return this tile's position.
	 */
	public GridCoordinates getPosition() {
		return this.position;
	}

	/**
	 * Adds a property to this tile's properties.
	 * @param newProp the new property
	 */
	public void addProperty (TileProperty newProp) {
		properties.add(newProp);
	}

	/**
	 * Removes a specified property from this tile's properties.
	 * @param removedProp the property to remove
	 */
	public void removeProperty (TileProperty removedProp) {
		properties.remove(removedProp);
	}

	/**
	 * Returns the Properties attached to this tile.
	 * @return a List containing this tile's Properties
	 */
	public List<TileProperty> getProperties () {
		List<TileProperty> returnedProperties = new ArrayList<TileProperty>();
		returnedProperties.addAll(properties);
		return returnedProperties;
	}

	/**
	 * Adds a property to this tile's EngineeringProperties.
	 * @param newProp the new property
	 */
	public void addEngineeringProperty (TileProperty newProp) {
		engineeringProperties.add(newProp);
	}

	/**
	 * Removes a specified property from this tile's EngineeringProperties.
	 * @param removedProp the property to remove
	 */
	public void removeEngineeringProperty (TileProperty removedProp) {
		engineeringProperties.remove(removedProp);
	}

	/**
	 * Returns the EngineeringProperties attached to this tile.
	 * @return a List containing this tile's EngineeringProperties
	 */
	public List<TileProperty> getEngineeringProperties () {
		List<TileProperty> returnedEngineeringProperties = new ArrayList<TileProperty>();
		returnedEngineeringProperties.addAll(engineeringProperties);
		return returnedEngineeringProperties;
	}

	/**
	 * Check to see if this tile contains an entity.
	  * @return true if this tile is occupied, false if it is unoccupied.
	 */
	public boolean occupied () {
		return !(entity == null);
	}

	/**
	 * Returns the entity occupying this this tile.
	 * Null = no entity.
	 * @return the entity occupying this tile if it is occupied; null if it is unoccopied.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Sets the entity occupying this tile. Null = no entity.
	 * @param occupier the entity to set to occupy this tile
	 */
	public void setEntity(Entity occupier) {
		this.entity = occupier;
	}

	/**
	 * Returns this tile's Type
	 * @return this tile's Type
	 */
	public Type getType() {
		return this.type;
	}
}
