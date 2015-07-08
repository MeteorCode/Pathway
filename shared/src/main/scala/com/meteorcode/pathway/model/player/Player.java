package com.meteorcode.pathway.model.player;

import java.util.ArrayList;
import java.util.List;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Entity;
import com.meteorcode.pathway.model.Property;

/**
 *
 * @author Hawk Weisman
 *
 */
public class Player extends Entity {

	private String name;
	private List<Property> properties;
	private List<Experience> skills;
	private Context playerContext;

	/**
	 * Constructor for a Player object with a name
	 * @param name the player's name.
	 */
	public Player(String name, Long gameID) {
		super(gameID, name);
		this.setName(name);
		playerContext = new Context (this.name + " Context");
	}

	/**
	 * @return the Player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the Player's name.
	 * @param name the new name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a shallow copy of the Player's skill list.
	 * @return a List containing this Player's Experience objects
	 */
	public List<Experience> getSkills () {
		List<Experience> returnedSkills = new ArrayList<Experience>();
		returnedSkills.addAll(skills);
		return returnedSkills;
	}

	/**
	 * Returns a shallow copy of the Player's property list.
	 * @return a List containing this Player's Property objects
	 */
	public List<Property> getProperties () {
		List<Property> returnedProps = new ArrayList<Property>();
		returnedProps.addAll(properties);
		return returnedProps;
	}

	public void attachSkill (Experience skill) {
		skills.add(skill);
	}

	public void attachProperty (Property prop) {
		properties.add(prop);
		prop.changeContext(playerContext);
	}
}
