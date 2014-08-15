package com.meteorcode.pathway.sparkstest;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.script.ScriptException;

public class BounceEvent extends Event {

	private Ball target;
	
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
