package com.meteorcode.pathway.model;

import com.meteorcode.pathway.logging.LoggerFactory;
import com.meteorcode.pathway.logging.LogDestination;
import com.meteorcode.pathway.model.Context;
import scala.None;
import scala.Option;

/**
 * An object within the game.
 *
 * @author Hawk Weisman <hawk.weisman@gmail.com>
 *
 */
public abstract class GameObject {
	private Long gameID;
	protected Context parent;
    protected LogDestination logger = LoggerFactory.getLogger();

	/**
	 * Constructor for a GameObject with a specified gameID.
	 *
	 * @param gameID
	 *            a gameID for this Object.
	 */
	public GameObject(Long gameID) {
        if (gameID==null) {
            // TODO: Get GameIDs from the GameInstance/GameEngine
            // which does not  currently exist (presumably in Controller?)
            // so that we can get themfrom the server in SMP
        } else {
            this.gameID = gameID;
        }
		this.parent = null;
	}

	/**
	 * Constructor for a GameObject with a gameID acquired from the
	 * GameInstance/GameEngine (NYI)
	 */
	public GameObject() {
        // TODO: Get GameIDs from the GameInstance/GameEngine
        // which does not  currently exist (presumably in Controller?)
        // so that we can get themfrom the server in SMP
        this.parent = null;
    }

    public GameObject(Option<scala.Long> optionalID) {
        // stupid Scala/Java interop cruft
        Long gameID = optionalID.getOrElse(null);
        if (gameID == null) {
            // TODO: Get GameIDs from the GameInstance/GameEngine
            // which does not  currently exist (presumably in Controller?)
            // so that we can get themfrom the server in SMP
        } else {
            this.gameID = gameID;
        }
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
	 * <p>Move the GameObject from the currently-occupied Context to a new Context.</p>
	 * <p>Note that this assumes a GameObject may only occupy one Context at a
	 * time. I'm assuming that this is correct behaviour, let me know if it
	 * needs to be modified.</p>
	 *
	 * @param newContext
	 *            the Context this GameObject is entering
	 */
	public void changeContext(Context newContext) {
        logger.log(this.toString(), "moving to " + newContext.name());
		if (parent != null) {
			parent.removeGameObject(this);
		}
		parent = newContext;
		parent.addGameObject(this);

	}
}
