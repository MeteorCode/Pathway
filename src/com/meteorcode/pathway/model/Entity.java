package com.meteorcode.pathway.model;

public class Entity extends GameObject {
	
	/**
	 * Constructor for a new Entity. The GameID will be assigned by the
	 * GameEngine (NYI)
	 */
	public Entity() {
		super();
	}

	/**
	 * Constructor for an Entity with a known gameID. Calls the known-gameID
	 * constructor in GameObject.
	 * 
	 * @param gameID
	 *            the gameID for this Entity
	 */
	public Entity(long gameID) {
		super(gameID);
	}
}