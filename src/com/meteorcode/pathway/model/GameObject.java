package com.meteorcode.pathway.model;

import com.meteorcode.pathway.model.Context;

/**
 * An object within the game.
 * 
 * @author Hawk Weisman <hawk.weisman@gmail.com>
 * 
 */
public abstract class GameObject {
	private long gameID;
	protected Context parent;

	/**
	 * Constructor for a GameObject with a specified gameID.
	 * 
	 * @param gameID
	 *            a gameID for this Object.
	 */
	public GameObject(long gameID) {
		this.gameID = gameID;
		this.parent = null;
	}

	/**
	 * Constructor for a GameObject with a gameID acquired from the
	 * GameInstance/GameEngine (NYI)
	 */
	public GameObject() {
		// TODO: Get GameIDs from the GameInstance/GameEngine which does not
		// currently exist (presumably in Controller?) so that we can get them
		// from the server in SMP
		this.parent = null;
	}

	/**
	 * @return the gameID
	 */
	public long getGameID() {
		return gameID;
	}

	/**
	 * @return the Context this GameObject inhabits
	 */
	public Context getContext() {
		return parent;
	}

	/**
	 * Move the GameObject from the currently-occupied Context to a new Context.
	 * 
	 * @param newContext
	 *            the Context this GameObject is entering
	 * @note that this assumes a GameObject may only occupy one Context at a
	 *       time. I'm assuming that this is correct behaviour, let me know if
	 *       it needs to be modified.
	 */
	public void changeContext(Context newContext) {
		if (parent != null) {
			parent.removeGameObject(this);
		}
		parent = newContext;
		parent.addGameObject(this);

	}
}
