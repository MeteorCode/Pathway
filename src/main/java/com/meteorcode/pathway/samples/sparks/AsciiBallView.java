package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.GameObject;

/**
 * AsciiBallView implements a View for the Sparks system which displays
 * each ball in ASCII representation on the screen.
 */
public class AsciiBallView {
	Context model;
	private int width, height;
	
	public AsciiBallView(int width, int height, Context model) {
        if(height % 2 != 0) throw new IllegalArgumentException("Height must be divisible by 2!");
        if(width % 2 != 0) throw new IllegalArgumentException("Width must be divisible by 2!");
		this.width = width;
		this.height = height;
		this.model = model;
	}
	
	public float[][] getGradientGrid() {
		float[][] grid = new float[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				grid[x][y] = 0;
			}
		}
		for(GameObject o : model.getGameObjects()) {
			if(o instanceof Ball) {
				Ball b = (Ball)o;
				for(int x = 0; x < width; x++) {
					for(int y = 0; y < width; y++) {
						grid[x][y] += b.gradient(x, y);
					}
				}
			}
		}
		return grid;
	}
	
	public void render() {
		float[][] grid = getGradientGrid();
		for(int i = 0; i < 10; i++) System.out.println("");
		System.out.println("~~~~~~~~~~");
		System.out.println("");
		for(int y = 0; y < grid.length; y += 2) {
			System.out.print("~ ");
			for(int x = 0; x < grid[y].length; x++) {
                //we compress the Y by 2 so that it looks nicer in a terminal.
                float grad = (grid[y][x] + grid[y+1][x])/2;
				System.out.print(getChs(grad));
			}
			System.out.println(" ~");
		}
	}
	
	public char getChs(float grid) {
		int asc = Math.min((int)(grid * 10), 10);
		switch(asc) {
            case 1:
			case 2:
			case 3:
				return '.';
			case 4:
			case 5:
				return '+';
			case 6:
			case 7:
            case 8:
            case 9:
            case 10:
				return '=';
			case 0:
			default:
				return ' ';
		}
	}
}
