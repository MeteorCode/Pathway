package com.meteorcode.pathway.model;

public class Item {
	private String name;
	
	public Item(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
