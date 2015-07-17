package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;

/**
 * BounceProperty is a Property which validates MoveEvents on the event stack.
 * BounceProperty will filter MoveEvents on Balls which are out of bounds,
 * instead sending them back and changing their velocity in order to bounce them.
 */
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
				MoveEvent unstick = new MoveEvent("Ball-bounce-move-unstick-" + target.toString(), this.getParent(), target);
				unstick.stamp(this);
				this.getParent().fireEvent(unstick);
				this.getParent().fireEvent(new BounceEvent("Ball-bounce-" + target.toString(), this.getParent(), target));
				return false;
			}
			return true;
		}
		return true;
	}

}
