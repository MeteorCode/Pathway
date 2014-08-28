package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.model.GameObject;
import com.meteorcode.pathway.script.ScriptException;

public class BaseEvent extends Event {

	public BaseEvent(String name, Context origin) {
		super(name, origin);
	}

	@Override
	public void evalEvent() throws ScriptException {
		//ensure that this is run again.
		origin.fireEvent(this);
		//fire movement events for all the balls.
		for(GameObject o : origin.getGameObjects()) {
			if(o instanceof Ball) {
				Ball b = (Ball)o;
				origin.fireEvent(new MoveEvent("Ball-move-" + b.toString(), origin, b));
			}
		}
	}

}
