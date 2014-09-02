package com.meteorcode.pathway.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScriptEnvironment is an abstraction of mappings that scripts will have to use
 * in order to have a consistent API. I.e. you would map the script variable
 * "game" to perhaps an instance of "Game", and if the ScriptEnvironment is
 * working correctly, you should be able to pass it to a ScriptContainer and all
 * scripts that use the word "game" are referring to your in-java Game.
 * 
 * @author xyzzy
 * @author Hawk Weisman
 * 
 */
public class ScriptEnvironment {
	private Map<String, Object> bindings;
	private List<ScriptContainer> containers;
    private String onLink = null;

	/**
	 * Constructor for a blank ScriptEnvironment.
	 */
	public ScriptEnvironment() {
		this.bindings = new HashMap<String, Object>();
		this.containers = new ArrayList<ScriptContainer>();
	}

	/**
	 * Constructor for a ScriptEnvironment with initial bindings.
	 * 
	 * @param initialBindings
	 *            A mapping of variable names to objects that will be used as
	 *            the initial bindings for the ScriptEnvironment.
	 */
	public ScriptEnvironment(Map<String, Object> initialBindings) {
		this.bindings = new HashMap<String, Object>();
		this.bindings.putAll(initialBindings);
		this.containers = new ArrayList<ScriptContainer>();
	}

    /**
     * Constructor for a ScriptEnvironment with no initial bindings and an onLink script.
     * @param onLink
     *            A String containing a script to be executed when the ScriptEnvironment
     *            is linked to a ScriptContainer.
     */
    public ScriptEnvironment(String onLink) {
        this.bindings = new HashMap<String, Object>();
        this.containers = new ArrayList<ScriptContainer>();
        this.onLink = onLink;
    }

    /**
     * Constructor for a ScriptEnvironment with initial bindings and an onLink script.
     *
     * @param initialBindings
     *            A mapping of variable names to objects that will be used as
     *            the initial bindings for the ScriptEnvironment.
     * @param onLink
     *            A String containing a script to be executed when the ScriptEnvironment
     *            is linked to a ScriptContainer.
     */
    public ScriptEnvironment(Map<String, Object> initialBindings, String onLink) {
        this.bindings = new HashMap<String, Object>();
        this.bindings.putAll(initialBindings);
        this.containers = new ArrayList<ScriptContainer>();
        this.onLink = onLink;
    }

	/**
	 * Adds a new binding to the ScriptEnvironment. If the ScriptEnvironment is
	 * linked to any ScriptContainers, this will automatically inject the new
	 * binding into that ScriptContainer.
	 * 
	 * @param name
	 *            the name to bind to the variable
	 * @param variable
	 *            the value of the new variable
	 * @throws ScriptException
	 *             in the event of an injection failure
	 */
	public void addBinding(String name, Object variable) throws ScriptException {
		bindings.put(name, variable);
		if (!containers.isEmpty()) {
			for (ScriptContainer c : containers)
				c.injectObject(name, variable);
		}
	}

	/**
	 * Adds new bindings to the ScriptEnvironment. If the ScriptEnvironment is
	 * linked to any ScriptContainers, this will automatically inject the new
	 * binding into that ScriptContainer.
	 * 
	 * @param newBindings
	 *            a mapping of Strings to Objects to use as new bindings.
	 * @throws ScriptException
	 *             in the event of an injection failure
	 */
	public void addBindings(Map<String, Object> newBindings)
			throws ScriptException {
		bindings.putAll(newBindings);
		if (!containers.isEmpty()) {
			for (ScriptContainer c : containers) {
				for (Map.Entry<String, Object> i : newBindings.entrySet())
					c.injectObject(i.getKey(), i.getValue());
			}
		}
	}

	public Map<String, Object> getBindings() {
		return bindings;
	}

	/**
	 * Links this environment with a ScriptContainer, injecting this
	 * environment's bindings into the container. The onLink script will be executed
     * if it is present.
	 * <p>
	 * If the target ScriptContainer is already linked to this environment, this
	 * method silently does nothing.
	 * </p>
	 * 
	 * @param container
	 *            the target ScriptContainer to link with.
	 * @throws ScriptException
	 *             if an error occurs during binding injection
	 */
	public void link(ScriptContainer container) throws ScriptException {
		if (!containers.contains(container)) {
			containers.add(container);
			for (Map.Entry<String, Object> i : bindings.entrySet())
				container.injectObject(i.getKey(), i.getValue());
		}
        if (onLink != null)
            container.eval(onLink);
	}

	/**
	 * Unlinks this environment from a linked ScriptContainer, removing the
	 * bindings from the container's execution environment.
	 * <p>
	 * If the target ScriptContainer is not linked to this environment, this
	 * method silently does nothing.
	 * </p>
	 * 
	 * @param container
	 *            the target ScriptContainer to link with
	 * @throws ScriptException
	 *             if an error occurs during binding removal
	 */
	public void unlink(ScriptContainer container) throws ScriptException {
		if (containers.contains(container)) {
			for (String key : bindings.keySet())
				container.removeObject(key);
			containers.remove(container);
		}
	}

}
