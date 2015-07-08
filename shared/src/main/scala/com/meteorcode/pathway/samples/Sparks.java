package com.meteorcode.pathway.samples;

import com.meteorcode.pathway.logging.LoggerFactory;
import com.meteorcode.pathway.logging.NullLogger;
import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.samples.sparks.AsciiViewEvent;
import com.meteorcode.pathway.samples.sparks.Ball;
import com.meteorcode.pathway.samples.sparks.BaseEvent;
import com.meteorcode.pathway.samples.sparks.BounceProperty;
import com.meteorcode.pathway.script.ScriptException;

/**
 * Sparks is a sample project built using the Pathway engine, to demonstrate the
 * capabilities of the event system as a model for running a game.
 * 
 * The system set up here runs perpetually due to the BaseEvent event,
 * which is set up to make sure that the "game" continues to update
 * all by itself. In a real game, it is quite probable that you would
 * want to have something else to use as the "heartbeat" for the event stack.
 */
public class Sparks {

	public static void main (String[] argv) throws ScriptException {
	        //Silence the logs, since they will be noisy and our
	        //View prints to the terminal.
	        LoggerFactory.setLogger(new NullLogger());
	        //the very first thing we need is a game context.
	        Context c = new Context();
	        //set up width, height, because we'll be referencing them a lot
	        int width = 30;
	        int height = 30;
	        //Next we need a ball
	        Ball test = new Ball(width, height);
	        //add the ball to the game
	        c.addGameObject(test);
	        //prepare the event stack for running the game
	        c.fireEvent(new BaseEvent("Base Event", c, new AsciiViewEvent("View Event", c, width, height)));
	        //enable ball bouncing (so that balls won't drift off screen)
	        c.subscribe(new BounceProperty(c, width, height));
	        //Allow the game to run.
	        while(true) c.pump();
	}
}
