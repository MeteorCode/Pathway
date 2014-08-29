package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.script.ScriptException;

/**
 * BounceEvent is an Event which targets a Ball and causes that ball to bounce
 * if the event is evaluated.
 */
public class BounceEvent extends Event {

	private Ball target;

    /**
     * Construct a new BounceEvent with the given ball as the target.
     * @param name The name of this BounceEvent
     * @param origin The origin Context for this BounceEvent
     * @param target The ball which is the subject of the bouncing.
     */
	public BounceEvent(String name, Context origin, Ball target) {
		super(name, origin);
		this.target = target;
	}

	@Override
	public void evalEvent() throws ScriptException {
		target.bounce();
	}
	
	public Ball getTarget() {
		return target;
	}
}
