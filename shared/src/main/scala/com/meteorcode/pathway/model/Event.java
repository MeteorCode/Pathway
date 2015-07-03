package com.meteorcode.pathway.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.meteorcode.pathway.logging.LogDestination;
import com.meteorcode.pathway.logging.LoggerFactory;
import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.script.ScriptException;

/**
 * An Event in the event loop. Do note that Events are immutable. If you want to
 * modify an Event that's on the stack, swallow that Event and fire a new Event
 * with the appropriate data changed. Don't forget to invalidate old events on
 * the stack.
 *
 * @author Hawk Weisman
 * @author Xyzzy
 */
public abstract class Event {

	private Event parent;
	private String name;
	protected Payload payload;
	private Set<Event> children;
	private boolean valid;
	protected Context origin;
	protected Context target;
    protected LogDestination logger = LoggerFactory.getLogger();

	/**
	 * Constructor for an Event with a data payload.
	 *
	 * @param name
	 *            the name of this Event.
	 * @param payload
	 *            a Map of Strings to Objects containing the data payload connected to
	 *            this Event.
	 * @param origin
	 *            The Context where this event originated.
	 */
	public Event(String name, Map<String, Object> payload, Context origin) {
		this.children = new HashSet<Event>();
		this.parent = null;
		this.name = name;
		this.payload = new Payload(payload);
		this.origin = origin;
		this.valid = true;
	}

	/**
	 * Constructor for an Event without a data payload. This creates a new Event
	 * with the payload set to empty map. Use this only when you need an Event
	 * which carries no additional data beyond its' name, as you cannot add data
	 * to the payload of empty events after initialization.
	 *
	 * @param name
	 *            the name of this Event.
	 * @param origin
	 *            The Context where this event originated.
	 */
	public Event(String name, Context origin) {
		this.children = new HashSet<Event>();
		this.parent = null;
		this.name = name;
		this.origin = origin;
		this.payload = new Payload();
		this.valid = true;
	}

    /**
     * Constructor for an event with an originating context. The name defaults to the event's class name.
     * @param origin The Context where this event originated.
     */
    public Event(Context origin) {
        this.children = new HashSet<Event>();
        this.parent = null;
        this.name = this.getClass().getSimpleName();
        this.origin = origin;
        this.payload = new Payload();
        this.valid = true;
    }

    /**
     * Constructor for an Event with a data payload.
     * The name defaults to the event's class name.
     *
     * @param payload
     *            a Map of Strings to Objects containing the data payload connected to
     *            this Event.
     * @param origin
     *            The Context where this event originated.
     */
    public Event(Map<String, Object> payload, Context origin) {
        this.children = new HashSet<Event>();
        this.parent = null;
        this.name = this.getClass().getSimpleName();
        this.payload = new Payload(payload);
        this.origin = origin;
        this.valid = true;
    }

    /**
     * Constructor for an event with a location.
     * The name defaults to the event's class name.
     * @param origin
     * @param location
     */
    public Event(Context origin, Tile location) {
        this.children = new HashSet<Event>();
        this.parent = null;
        this.name = this.getClass().getSimpleName();
        this.origin = origin;
        this.payload = new Payload(location);
        this.valid = true;
    }

    /**
     * Constructor for an event with a payload and a location.
     * The name defaults to the event's class name.
     *
     * @param location
     *             The Tile where this event occurred.
     * @param payload
     *            a Map of Strings to Objects containing the data payload connected to
     *            this Event.
     * @param origin
     *            The Context where this event originated.
     */
    public Event(Map<String, Object> payload, Context origin, Tile location) {
        this.children = new HashSet<Event>();
        this.parent = null;
        this.name = this.getClass().getSimpleName();
        this.payload = new Payload(payload, location);
        this.origin = origin;
        this.valid = true;
    }

	public Event(String name, Context origin, Tile location) {
		this.children = new HashSet<Event>();
		this.parent = null;
		this.name = name;
		this.origin = origin;
		this.payload = new Payload(location);
		this.valid = true;
	}

	public Event(String name, Map<String, Object> payload, Context origin, Tile location) {
		this.children = new HashSet<Event>();
		this.parent = null;
		this.name = name;
		this.payload = new Payload(payload, location);
		this.origin = origin;
		this.valid = true;
	}

	/**
	 * <p>The method that is called to actually perform the thing done by an Event.
	 * Should be called as part of the event loop, when an event is finally...
	 * you know, evented.</p>
	 *
	 * <p>NOTE that this occurs <i>after</i> the event has been propagated, and
	 * also NOTE that this method will *not* be called on an invalidated Event.
	 * (Event code shouldn't have to worry whether or not they're invalid)</p>
	 *
	 * @throws ScriptException
	 */
	public abstract void evalEvent() throws ScriptException;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets this Event's payload.
	 *
	 * @return A shallow copy of this Event's data payload
	 */
	public Payload getPayload() {
		return this.payload;
	}

	/**
	 * Inserts a String to Object mapping into the Event's payload
	 *
	 * @param name
	 *            the name to map to the new value
	 * @param value
	 *            the value to insert into the payload
	 */
	public void patchPayload(String name, Object value) {
        logger.log(this.name, "added " + name + "->" + value + " to payload");
		payload.patch(name,value);
	}

	/**
	 * Inserts a Map of Strings to Objects into the payload
	 *
	 * @param additions
	 *            the map to add to the payload.
	 */
	public void patchPayload(Map<String, Object> additions) {
        logger.log(this.name, "added " + additions + " to payload");
		payload.patch(additions);
	}

	public boolean stampExists(Property stampedBy) {

        return payload.stampExists(stampedBy);
	}

	public void stamp (Property stampedBy) {
		logger.log(stampedBy.toString(), "stamped " + this.name);
        payload.stamp(stampedBy);
	}

	public void unstamp(Property stampedBy) {

        logger.log(stampedBy.toString(), "unstamped " + this.name);
        payload.unstamp(stampedBy);
	}

	/**
	 * Invalidates this Event by setting the valid flag to false.
	 */
	public void invalidate() {
		this.valid = false;
        logger.log(this.name, "invalidated");
		if (!this.children.isEmpty()) {
			for (Event child : children)
				child.invalidate();
		}
	}

	/**
	 * Check the validity of this Event.
	 *
	 * @return true if this Event is valid, false if it has been invalidated.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * <p>
	 * Cause an Event to be fired - in the current context - as a child/result
	 * of another Event. If successful, the Event passed to this method inherits
	 * this Event as its parent, and is fired into this Event's Context for
	 * evaluation. Method returns true IFF this process was successful. The
	 * default implementation does this.
	 * </p>
	 *
	 * <p>
	 * In the event that firing Children from this Event is unsuccessful or
	 * disallowed, the method should return false instead, and should not modify
	 * the Event parameter, e.
	 * </p>
	 *
	 * @param e
	 *            The Event to fire
	 * @return child The new child Event.
	 */
	public boolean fireEventChild(Event e) {
        logger.log(this.name, "fired child event " + e.name);
		e.parent = this;
		this.children.add(e);
		this.origin.fireEvent(e);
		return true;
	}

    /**
     * Set the Context of this Event to the specified Context.
     * @param c the new Context to set
     */
	public void setTarget(Context c) {
		this.target = c;
	}

    /**
     * @return a pointer to the parent Event
     */
	public Event getParent() {
		return this.parent;
	}

	public String toString() {
		return "Event: " + this.getName();
	}

}
