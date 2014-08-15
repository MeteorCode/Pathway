/**
 * 
 */
package com.meteorcode.pathway.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.meteorcode.pathway.script.ScriptException;

/**
 * Okay, let's be honest: this is just to get 100% coverage.
 * @author Hawk Weisman
 *
 */
public class ScriptExceptionTest {

	/**
	 * Test method for {@link com.meteorcode.pathway.script.ScriptException#ScriptException()}.
	 */
	@Test
	public void testScriptException() {
		assertNotNull(new ScriptException());
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.script.ScriptException#ScriptException(java.lang.String)}.
	 */
	@Test
	public void testScriptExceptionString() {
		assertNotNull(new ScriptException("fake message"));
		assertEquals("fake message", new ScriptException("fake message").getMessage());
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.script.ScriptException#ScriptException(java.lang.Throwable)}.
	 */
	@Test
	public void testScriptExceptionThrowable() {
		Exception testException = new Exception();
		assertNotNull(new ScriptException(testException));
		assertEquals(new ScriptException(testException).getCause(), testException);
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.script.ScriptException#ScriptException(java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public void testScriptExceptionStringThrowable() {
		Exception testException = new Exception();
		assertNotNull(new ScriptException("Fake message", testException));
		assertEquals(new ScriptException("Fake message", testException).getCause(), testException);
		assertEquals(new ScriptException("Fake message", testException).getMessage(), "Fake message");
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.script.ScriptException#ScriptException(java.lang.String, java.lang.Throwable, boolean, boolean)}.
	 */
	@Test
	public void testScriptExceptionStringThrowableBooleanBoolean() {
		Exception testException = new Exception();
		assertNotNull(new ScriptException("Fake message", testException, true, true));
	}

}
