package com.meteorcode.pathway.sparkstest;

import java.util.Random;

import com.meteorcode.pathway.model.GameObject;

public class Ball extends GameObject {
	//x, y, velocity
	private float x, y, vx, vy, radius;
	
	public Ball(int width, int height) {
		Random r = new Random();
		this.radius = r.nextInt(4) + 2;
		this.x = r.nextInt((int)(width-radius*2-1)) + radius + 1;
		this.y = r.nextInt((int)(height-radius*2-1)) + radius + 1;
		this.vx = r.nextFloat() - 0.5f;
		this.vy = r.nextFloat() - 0.5f;
	}
	
	public Ball(int x, int y, int radius, float vx, float vy) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.radius = radius;
	}
	
	public int getX() {
		return (int)x;
	}
	
	public int getY() {
		return (int)y;
	}
	
	public int getRadius() {
		return (int)radius;
	}
	
	public void bounce() {
	    this.vx = -this.vx;
	    this.vy = -this.vy;
	}
	
	public void go() {
	    //NOTE that the go method does NOT bounce the ball! woo!
	    this.x += vx;
	    this.y += vy;
	}
	
	public float gradient(float x, float y) {
	    float dist = distance(x,y);
	    if(dist > radius) return 0;
	    return (dist/radius);
	}
	
	public float distance(float x, float y) {
	    float ox = (x - this.x)*(x-this.x);
	    float oy = (y - this.y)*(y-this.y);
	    return (float)Math.sqrt(Math.abs(ox+oy));
	}
	
	public boolean intersects(Ball other) {
	    float dist = distance(other.x, other.y);
	    return (dist >= (this.radius + other.radius));
	}
	
	//out of bounds
	public boolean oob(int maxwidth, int maxheight) {
	    if(this.x <= this.radius || maxwidth - this.x <= this.radius) return true;
	    if(this.y <= this.radius || maxheight - this.y <= this.radius) return true;
	    return false;
	}
	
	public String toString() {
		return "p(" + (int)x + "," + (int)y + ")"; 
	}
}
