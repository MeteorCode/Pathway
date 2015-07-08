package com.meteorcode.pathway.model.player;

public class Experience {
	
	private int xpValue;
	private String name;

	/**
	 * @return this Experience's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets a new name.
	 * 
	 * @param name
	 *            the new name for this Experience
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Increments the XP value by the given amount.
	 * 
	 * <p><b>Note:</b> Incrementing by a negative number will currently decrease the XP
	 * value. This behaviour may be removed at a later date.</p>
	 * 
	 * @param amount
	 *            the amount of XP to add.
	 */
	public void increment(int amount) {
		this.xpValue += amount;
	}
	
	/**
	 * @return the current XP value of this counter.
	 */
	public int getXP() {
		return this.xpValue;
	}
}
