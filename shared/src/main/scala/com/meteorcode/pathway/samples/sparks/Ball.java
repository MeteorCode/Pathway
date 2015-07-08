package com.meteorcode.pathway.samples.sparks;

import java.util.Random;

import com.meteorcode.pathway.model.GameObject;

/**
 * This is a simple ball class, implementing a very primitive sphere shape.
 */
public class Ball extends GameObject {
	//x, y, velocity
	private float x, y, vx, vy, radius;

    /**
     * Construct a random ball inside the given width/height rectangle of space.
     * The method ensures that the ball will be inside the given rectangle of space,
     * and all of the other ball parameters are generated randomly, such as velocity
     * and radius.
     * @param width The maximum width of the area that the ball may be inside. Note that width must be &gt; 0
     * @param height The maximum height of the area that the ball may be inside. Note that the height must &gt; than 0
     */
	public Ball(int width, int height) {
		Random r = new Random();
		this.radius = r.nextInt(4) + 3;
		this.x = r.nextInt((int)(width-radius*2-1)) + radius + 1;
		this.y = r.nextInt((int)(height-radius*2-1)) + radius + 1;
		this.vx = r.nextFloat() - 0.5f;
		this.vy = r.nextFloat() - 0.5f;
	}

    /**
     * Construct a ball with the given parameters exactly.
     * @param x The X position of the ball
     * @param y The Y position of the ball
     * @param radius The radius of the ball.
     * @param vx The velocity of the ball in the X direction
     * @param vy The velocity of the ball in the Y direction
     */
	public Ball(int x, int y, int radius, float vx, float vy) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.radius = radius;
	}

    /**
     * Returns the X position of the ball as an integer.
     * @return The X position of the ball
     */
	public int getX() {
		return (int)x;
	}

    /**
     * Returns the Y position of the ball as an integer
     * @return The Y position of the ball
     */
	public int getY() {
		return (int)y;
	}

    /**
     * Returns the radius of the ball, as an integer
     * @return The radius of the ball
     */
	public int getRadius() {
		return (int)radius;
	}

    /**
     * Bounces the ball in both the X and the Y directions
     * by reversing the ball's velocity.
     */
	public void bounce() {
	    this.vx = -this.vx;
	    this.vy = -this.vy;
	}

    /**
     * Steps the ball one step into the future, by adding the ball's
     * velocity to its existing X and Y positions.
     */
	public void go() {
	    this.x += vx;
	    this.y += vy;
	}

    /**
     * Returns a gradient percentage of how far the given point is from the center
     * of the ball. If outside the ball, this percentage is 0, otherwise the percentage
     * is the fraction representing the distance the given point is between the center
     * of the ball and its radius.
     * @param x The X coordinate of the point to test
     * @param y The Y coordinate of the point to test
     * @return The percent gradient of this ball at the tested point, as a float whose value is between 0 and 1
     */
	public float gradient(float x, float y) {
	    float dist = distance(x,y);
	    if(dist > radius) return 0;
	    return 1-(dist/radius);
	}

    /**
     * Returns the absolute distance between a given point and the center of this ball.
     * @param x The X coordinate of the point to test
     * @param y The Y coordinate of the point to test
     * @return The distance between the tested point and the center of this ball.
     */
	public float distance(float x, float y) {
	    float ox = (x - this.x)*(x-this.x);
	    float oy = (y - this.y)*(y-this.y);
	    return (float)Math.sqrt(Math.abs(ox+oy));
	}

    /**
     * Tests whether or not two balls intersect with one another.
     * @param other The other ball to test with.
     * @return True if this ball and the tested ball intersect, false otherwise.
     */
	public boolean intersects(Ball other) {
	    float dist = distance(other.x, other.y);
	    return (dist >= (this.radius + other.radius));
	}

    /**
     * Checks whether a ball would be out of bounds of the given rectangular space.
     * @param maxWidth The width of the rectangular space to test
     * @param maxHeight The height of the rectangular space to test
     * @return True if the ball would fall outside the given rectangle, false otherwise.
     */
	public boolean oob(int maxWidth, int maxHeight) {
	    if(this.x <= this.radius || maxWidth - this.x <= this.radius) return true;
	    if(this.y <= this.radius || maxHeight - this.y <= this.radius) return true;
	    return false;
	}
	
	public String toString() {
		return "p(" + (int)x + "," + (int)y + ")"; 
	}
}
