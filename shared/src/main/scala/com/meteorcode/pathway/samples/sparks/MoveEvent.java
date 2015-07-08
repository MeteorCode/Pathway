package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.script.ScriptException;

/**
 * MoveEvent is an Event which targets a Ball and causes that ball to move
 * forwards one tick in time if the event is evaluated.
 */
public class MoveEvent extends Event {
	private Ball target;

    /**
     * Constructs a new MoveEvent with the given Ball as the target
     * @param name The name of this MoveEvent
     * @param origin The origin Context for this MoveEvent
     * @param target The ball to target with this MoveEvent
     */
	public MoveEvent(String name, Context origin, Ball target) {
		super(name, origin);
		this.target = target;
	}

	@Override
	public void evalEvent() throws ScriptException {
		target.go();
	}
	
	public Ball getTarget() {
		return this.target;
	}

}
