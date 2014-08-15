/**
 * 
 */
package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.InterpreterError;

import com.meteorcode.pathway.io.FileHandle;
import com.meteorcode.pathway.script.ScriptContainer;
import com.meteorcode.pathway.script.ScriptContainerFactory;
import com.meteorcode.pathway.script.ScriptEnvironment;
import com.meteorcode.pathway.script.ScriptException;

/**
 * Tests for {@link com.meteorcode.pathway.script.ScriptContainerFactory}
 * and its' inclosed BeanshellScriptContainer. All actual Beanshell
 * functionality is stubbed; as these are tests for the SpaceshipGame
 * BeanshellScriptContainer class rather than Beanshell itself.
 * 
 * @author Hawk Weisman
 * 
 * @TODO: replace hard-coded test values with randomly-generated ones
 * @TODO: make tests iterate, testing the targeted behaviour multiple times
 */

public class ScriptContainerTest {

	private Object testObj;
	private ScriptContainer testBSHContainer;
	private ScriptContainerFactory testFactory;
	private ScriptContainerFactory realFactory;

	private ScriptEnvironment fakeEnvironment;
	private Interpreter fakeInterpreter;
	private FileHandle fakeFileHandle;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		testObj = new Object();
		fakeInterpreter = mock(Interpreter.class);
		fakeEnvironment = mock(ScriptEnvironment.class);
		fakeFileHandle = mock(FileHandle.class);

		testFactory = new ScriptContainerFactory(fakeInterpreter);
		realFactory = new ScriptContainerFactory();

		testBSHContainer = testFactory.getNewInstance();
	}

	@Test
	public void testGetNewInstnace() {
		assertNotNull(testFactory.getNewInstance());
		assertNotNull(realFactory.getNewInstance());
	}

	@Test
	public void testGetNewInstanceWithEnvironment() throws ScriptException {
		testBSHContainer = testFactory
				.getNewInstanceWithEnvironment((fakeEnvironment));

		verify(fakeEnvironment).link(testBSHContainer);
		
		testBSHContainer = realFactory
				.getNewInstanceWithEnvironment((fakeEnvironment));

		verify(fakeEnvironment).link(testBSHContainer);
	}

	/**
	 * Test method for
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#eval(java.lang.String)}
	 * .
	 * 
	 * @throws ScriptException
	 * @throws EvalError
	 */
	@Test
	public void testEvalString() throws ScriptException, EvalError {
		when(fakeInterpreter.eval("I am a test script")).thenReturn(
				"And I am test script results.");
		assertTrue(testBSHContainer.eval("I am a test script").equals(
				"And I am test script results."));
		verify(fakeInterpreter).eval("I am a test script");
	}

	/**
	 * Test method for
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#eval(FielHandle)}
	 * .
	 * 
	 * @throws ScriptException
	 * @throws EvalError
	 * @throws IOException 
	 */
	@Test
	public void testEvalFileHandle() throws ScriptException, EvalError, IOException {
		when(fakeFileHandle.readString()).thenReturn(
				"I am a test script from a fake file");
		when(fakeInterpreter.eval("I am a test script from a fake file"))
				.thenReturn("And I am test script results.");

		assertTrue(testBSHContainer.eval(fakeFileHandle).equals(
				"And I am test script results."));
		verify(fakeInterpreter).eval("I am a test script from a fake file");
		verify(fakeFileHandle).readString();
	}

	/**
	 * Test method for error handling in
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#eval(FielHandle)}
	 * and
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#eval(java.lang.String)}
	 * .
	 * @throws IOException 
	 * */
	@Test()
	public void testEvalErrorHandling() throws EvalError, IOException {
		when(fakeFileHandle.readString()).thenReturn(
				"I am a test script from a fake file");
		when(fakeInterpreter.eval("I am a test script from a fake file"))
				.thenThrow(new EvalError(null, null, null));

		try {
			testBSHContainer.eval(fakeFileHandle);
			fail("FAIL: ScriptException not thrown by eval(FileHandle) after EvalError thrown by interpreter.");
		} catch (ScriptException e) {
			assertTrue(
					"FAIL: ScriptException did not wrap Beanshell EvalError",
					e.getCause() instanceof EvalError);
		}
		
		when(fakeFileHandle.readString()).thenReturn(
				"I am a second test script from a fake file");
		when(fakeInterpreter.eval("I am a second test script from a fake file")).thenThrow(
				new InterpreterError("I'm a String :D"));
		
		try {
			testBSHContainer.eval(fakeFileHandle);
			fail("FAIL: ScriptException not thrown by eval(String) after EvalError thrown by interpreter.");
		} catch (ScriptException e) {
			assertTrue(
					"FAIL: ScriptException did not wrap Beanshell EvalError",
					e.getCause() instanceof InterpreterError);
		}

		when(fakeInterpreter.eval("I am a test script.")).thenThrow(
				new EvalError(null, null, null));

		try {
			testBSHContainer.eval("I am a test script.");
			fail("FAIL: ScriptException not thrown by eval(String) after EvalError thrown by interpreter.");
		} catch (ScriptException e) {
			assertTrue(
					"FAIL: ScriptException did not wrap Beanshell EvalError",
					e.getCause() instanceof EvalError);
		}

		when(fakeInterpreter.eval("I am also a test script.")).thenThrow(
				new InterpreterError("I'm a String :D"));

		try {
			testBSHContainer.eval("I am also a test script.");
			fail("FAIL: ScriptException not thrown by eval(String) after EvalError thrown by interpreter.");
		} catch (ScriptException e) {
			assertTrue(
					"FAIL: ScriptException did not wrap Beanshell InterpreterError",
					e.getCause() instanceof InterpreterError);
		}

	}

	/**
	 * Test method for
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#injectObject(java.lang.String, java.lang.Object)}
	 * .
	 * 
	 * @throws ScriptException
	 * @throws EvalError
	 */
	@Test
	public void testInjectObject() throws ScriptException, EvalError {
		testBSHContainer.injectObject("TestObj", testObj);
		verify(fakeInterpreter).set("TestObj", testObj);
		
		doThrow(new EvalError(null, null, null)).when(fakeInterpreter).set("anotherTestObj",testObj);
		
		try {
			testBSHContainer.injectObject("anotherTestObj", testObj);
			fail("FAIL: BeanshellContainer did not throw ScriptException after Interpreter threw EvalError on object injection");
		} catch (ScriptException e) {
			assertTrue(
				"FAIL: ScriptException did not wrap Beanshell EvalError",
				e.getCause() instanceof EvalError);
			assertEquals(
					"FAIL: ScriptException thrown by injectObject did not have correct message.",
					e.getMessage(),
					"Error injecting anotherTestObj into Beanshell");
		}
	}

	/**
	 * Test method for
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#removeObject(java.lang.String)}
	 * .
	 * 
	 * @throws ScriptException
	 * @throws EvalError
	 */
	@Test
	public void testRemoveObject() throws ScriptException, EvalError {
		testBSHContainer.removeObject("TestObj");
		verify(fakeInterpreter).unset("TestObj");
		
		doThrow(new EvalError(null, null, null)).when(fakeInterpreter).unset("anotherTestObj");
		
		try {
			testBSHContainer.removeObject("anotherTestObj");
			fail("FAIL: BeanshellContainer did not throw ScriptException after Interpreter threw EvalError on object removal");
		} catch (ScriptException e) {
			assertTrue(
				"FAIL: ScriptException did not wrap Beanshell EvalError",
				e.getCause() instanceof EvalError);
			assertEquals(
					"FAIL: ScriptException thrown by injectObject did not have correct message.",
					e.getMessage(),
					"Error unbinding anotherTestObj from Beanshell");
		}
	}

	/**
	 * Test method for
	 * {@link com.meteorcode.spaceshipgame.script.BeanshellScriptContainer#access(java.lang.String)}
	 * .
	 * 
	 * @throws EvalError
	 * @throws ScriptException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testAccess() throws EvalError, IllegalArgumentException,
			ScriptException {
		when(fakeInterpreter.eval("testVar")).thenReturn(3);
		assertEquals(testBSHContainer.access("testVar"), 3);
		verify(fakeInterpreter).eval("testVar");
	}

	@Test
	public void testAccessErrorHandling() {

		try {
			testBSHContainer.access("finally");
			fail("FAIL: access() did not throw IllegalArgumentException when passed a Java reserved word.");
		} catch (IllegalArgumentException ie) {
			assertEquals(
					"FAIL: Access() did not attach correct message to exception",
					"Variable name cannot be a Java reserved word.",
					ie.getMessage());
		} catch (ScriptException e) {
			fail("FAIL: Access() threw wrong exception type.");
		}

		try {
			testBSHContainer.access("1variable");
			fail("FAIL: access() did not throw IllegalArgumentException when passed invalid identifier 1variable.");
		} catch (IllegalArgumentException ie) {
			assertEquals(
					"FAIL: Access() did not attach correct message to exception",
					"Variable name was not a valid Java identifier; illegal character at position 0",
					ie.getMessage());
		} catch (ScriptException e) {
			fail("FAIL: Access() threw wrong exception type.");
		}

		try {
			testBSHContainer.access("A<lessthansign");
			fail("FAIL: access() did not throw IllegalArgumentException when passed invalid identifier A<lessthansign.");
		} catch (IllegalArgumentException ie) {
			assertEquals(
					"FAIL: Access() did not attach correct message to exception",
					"Variable name was not a valid Java identifier; illegal character at position 1",
					ie.getMessage());
		} catch (ScriptException e) {
			fail("FAIL: Access() threw wrong exception type.");
		}

		try {
			testBSHContainer.access("This variable name contains spaces");
			fail("FAIL: access() did not throw IllegalArgumentException when passed an invalid identifier.");
		} catch (IllegalArgumentException ie) {
			assertEquals(
					"FAIL: Access() did not attach correct message to exception",
					"Variable name was not a valid Java identifier; illegal character at position 4",
					ie.getMessage());
		} catch (ScriptException e) {
			fail("FAIL: Access() threw wrong exception type.");
		}

	}
}
