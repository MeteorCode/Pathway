package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.script.ScriptException;

public class MoveEvent extends Event {
	private Ball target;
	
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
