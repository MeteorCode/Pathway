package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;

public class BounceProperty extends com.meteorcode.pathway.model.Property {

	private int maxWidth, maxHeight;
	
	public BounceProperty(Context c, int maxWidth, int maxHeight) {
		super(c);
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}
	
	@Override
	public boolean onEvent(Event e, Context c) {
		if(e.stampExists(this)) {
			return true;
		}
		e.stamp(this);
		
		if(e instanceof MoveEvent) {
			MoveEvent m = (MoveEvent)e;
			Ball target = m.getTarget();
			if(target.oob(maxWidth, maxHeight)) {
				m.invalidate();
				MoveEvent unstick = new MoveEvent("Ball-bounce-move-unstick-" + target.toString(), this.parent(), target);
				unstick.stamp(this);
				this.parent().fireEvent(unstick);
				this.parent().fireEvent(new BounceEvent("Ball-bounce-" + target.toString(), this.parent(), target));
				return false;
			}
			return true;
		}
		return true;
	}

}
