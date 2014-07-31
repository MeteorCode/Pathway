/**
 * 
 */
package com.meteorcode.pathway.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.meteorcode.pathway.logging.ConcurrentCache;

/**
 * @author Hawk Weisman
 *
 */
public class ConcurrentCachingTest {

	/**
	 * Test method for {@link com.meteorcode.pathway.logging.ConcurrentCache#ConcurrentCache()}.
	 */
	@Test
	public void testConcurrentCacheInit() {
		assertNotNull(new ConcurrentCache<String>());
		
		try {
			new ConcurrentCache<String>(-1);
			fail("Didn't throw expected exception.");
		} catch (IllegalArgumentException e) {
			assertEquals("Buffer size must be greater than 1.", e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.logging.ConcurrentCache#insert(java.lang.Object)}.
	 */
	@Test
	public void testInsertAndUnwind() {
		ConcurrentCache<Integer> cc = new ConcurrentCache<Integer>(10);
		
		for (int i = 0; i < 15; i++) {
			cc.insert(i);
		}
		
		Object[] cache = cc.unwind().toArray();
		System.out.println(cc.unwind());
		assertEquals(10, cache.length);
		
		for (int i = 0; i < cache.length; i++ ) {
			assertEquals(14-i, cache[i]);
			//this expected result should be
			//[14, 13, 12, 11, 10, 9, 8, 7, 6, 5]
			//shouldn't it?
			//
			//Max has corrected your test to display this.
		}
		
	}

}
