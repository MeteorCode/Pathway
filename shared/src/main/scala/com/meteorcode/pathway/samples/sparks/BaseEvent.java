package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.model.GameObject;
import com.meteorcode.pathway.script.ScriptException;

/**
 * BaseEvent is the bottom event of the event stack for the purposes of running
 * the Sparks sample. The reason it exists is to ensure that the game always ticks,
 * by pushing the necessary game events onto the stack whenever it is evaluated.
 *
 * Because of the nature of the event stack, this ensures that Sparks can be run
 * by simply pumping events on the stack repeatedly. Whenever the stack is finished
 * evaluating all of the tasks for each game tick, this BaseEvent will then occur,
 * repeating the process. If the BaseEvent did not exist, then the stack would empty
 * and would have to be primed by the caller, or in Sparks' case, the Main method.
 */
public class BaseEvent extends Event {

    //The event designed to update the view of the game model
    private Event viewEvent;

    /**
     * Constructs a new BaseEvent with the given name, origin context,
     * and View event. The view event is necessary so that the BaseEvent
     * will trigger itself and repaint the view each time the event stack
     * empties. (This is the point of having a BaseEvent)
     * @param name The name of this BaseEvent
     * @param origin The origin context for this BaseEvent
     * @param view The event that this BaseEvent should fire in order to repaint the game's view of the model.
     */
	public BaseEvent(String name, Context origin, Event view) {
        super(name, origin);
        this.viewEvent = view;
	}

	@Override
	public void evalEvent() throws ScriptException {
		//ensure that this is run again.
		origin.fireEvent(this);
        origin.fireEvent(viewEvent);
		//fire movement events for all the balls.
		for(GameObject o : origin.getGameObjects()) {
			if(o instanceof Ball) {
				Ball b = (Ball)o;
				origin.fireEvent(new MoveEvent("Ball-move-" + b.toString(), origin, b));
			}
		}
	}

}
