/*
package com.meteorcode.pathway.sparkstest;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.model.GameObject;

//import processing.core.PApplet;

public class ProcellingView  extends PApplet {
	Context model;
	Event base;
	
	public void setup() {
		size(200,200);
		model = new Context("ProcellingContext");
		base = new BaseEvent("Base Event", model);
		model.fireEvent(base);
		for(int i = 0; i < 20; i++) {
			model.addGameObject(new Ball(200,200));
		}
		model.subscribe(new BounceProperty(model, 200,200));
	}
	
	public void draw() {
		background(255);
		
		try {
			while(model.viewEventStack().getLast() != base) {
				System.out.println(model.viewEventStack().getLast());
				model.pump();
			}
			model.pump();
		} catch (Throwable t) {
			t.printStackTrace();
			exit();
		}
		
		for(GameObject o : model.getGameObjects()) {
			if(o instanceof Ball) {
				Ball b = (Ball)o;
				fill(255);
				stroke(0);
				ellipseMode(CENTER);
				ellipse(b.getX(), b.getY(), b.getRadius(), b.getRadius());
			}
		}
	}

	/**
	 * Default SerialVersionUID so Eclipse will stop whining.
	 *//*
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		PApplet.main(new String[] {"com.meteorcode.spaceshipgame.sparkstest.ProcellingView"});
	}
}
*/