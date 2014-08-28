package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.GameObject;

public class AsciiBallView {
	Context model;
	private int width, height;
	
	public AsciiBallView(int width, int height, Context model) {
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
		System.out.println("");
		System.out.println("~~~~~~~~~~");
		System.out.println("");
		for(int x = 0; x < grid.length; x++) {
			System.out.print("~ ");
			for(int y = 0; y < grid[x].length; y++) {
				System.out.print(getChs(grid[x][y]));
			}
			System.out.println(" ~");
		}
	}
	
	public char getChs(float grid) {
		int asc = (int)(grid * 10); //should be a number 0-10
		switch(asc) {
			case 2:
			case 3:
				return '.';
			case 4:
			case 5:
				return '+';
			case 6:
			case 7:
				return '=';
			case 8:
			case 9:
			case 10:
				return '#';
			case 0:
			case 1:
			default:
				return ' ';
		}
	}
}
