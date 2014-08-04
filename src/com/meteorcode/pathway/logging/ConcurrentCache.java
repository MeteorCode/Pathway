package com.meteorcode.pathway.logging;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentCache<T> {
	T[] buffer;
	private Object lock;
	private int cursor;
	private int fill;

	/**
	 * Constructor for a RingBuffer of default size.
	 */
	public ConcurrentCache() {
		this(200);
	}

	/**
	 * Constructor for a RingBuffer of arbitrary size
	 *
	 * @param size
	 *            the maximum size of the RingBuffer
	 * @throws IllegalArgumentException
	 *             if size is less than 1.
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentCache(int size) throws IllegalArgumentException {
		if (size < 1)
			throw new IllegalArgumentException(
					"Buffer size must be greater than 1.");
		buffer = (T[]) new Object[size];
		cursor = 0;
		fill = 0;
		lock = new Object();
	}

	/**
	 * Insert an object into the RingBuffer at the next available position. If
	 * the buffer is full, the oldest object in the buffer will be replaced.
	 *
	 * @param object
	 *            the object to be inserted into the buffer.
	 */
	public void insert(T object) {
		int myI;
		synchronized(lock) {
			myI = cursor;

			//handle small fills
			if(fill < buffer.length) fill ++;

			cursor = (cursor + 1) % buffer.length;
			buffer[myI] = object;
		}
	}

	/**
	 * Unwinds the cache, returning a copy of it's contents.
	 * @return a new List<T> containing the state of the cache's contents
	 */
	public List<T> unwind() {
		List<T> result = new ArrayList<T>();
		//freeze reads
		synchronized(lock) {
			if(fill >= buffer.length) {
				for(int i = buffer.length-1; i >= 0; i--) {
					result.add(buffer[(i+cursor) % buffer.length]);
				}
			} else {
				for(int i = fill; i >= 0; i--) {
					result.add(buffer[i]);
				}
			}
		}

		return result;
	}
}
