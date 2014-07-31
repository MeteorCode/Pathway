package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import com.meteorcode.pathway.script.ScriptContainer;
import com.meteorcode.pathway.script.ScriptEnvironment;
import com.meteorcode.pathway.script.ScriptException;

import org.junit.Test;

/**
 * Tests for {@link com.meteorcode.pathway.script.ScriptEnvironment}.
 * 
 * @author Hawk Weisman
 * @TODO: replace hard-coded test values with randomly-generated ones
 * @TODO: make tests iterate, testing the targeted behaviour multiple times
 */
public class ScriptEnvironmentTest {
	ScriptContainer fakeContainer;
	Map<String, Object> sampleMapOne;
	Map<String, Object> sampleMapTwo;
	Map<String, Object> sampleMapThree;

	@Before
	public void setUp() {
		fakeContainer = mock(ScriptContainer.class);

		sampleMapOne = new HashMap<String, Object>();
		sampleMapTwo = new HashMap<String, Object>();
		sampleMapThree = new HashMap<String, Object>();

		sampleMapOne.put("one", 1);
		sampleMapOne.put("two", "two");
		sampleMapOne.put("three", 3.0);

		sampleMapTwo.put("a", new Integer(1));
		sampleMapTwo.put("b", new Double(2.34));
		
		sampleMapThree.put("alpha", 943d);
		sampleMapThree.put("beta", 943l);

	}

	@Test
	public void testScriptEnvironment() {
		ScriptEnvironment se = new ScriptEnvironment();
		assertNotNull("FAIL: New ScriptEnvironment failed to initialize.", se);
		assertTrue(
				"FAIL: New empty ScriptEnvironment was initialized with bindings.",
				se.getBindings().isEmpty());
	}

	@Test
	public void testScriptEnvironmentMapOfStringObject() {
		ScriptEnvironment se = new ScriptEnvironment(sampleMapOne);
		assertNotNull(
				"FAIL: New ScriptEnvironment failed to initialize.", se);
		assertTrue(
				"FAIL: Bindings in new ScriptEnvironment did not match passed argument.",
				se.getBindings().equals(sampleMapOne));
	}

	@Test
	public void testAddBinding() throws ScriptException {
		ScriptEnvironment se = new ScriptEnvironment();
		se.link(fakeContainer);
		se.addBinding("test binding", 5);
		
		assertTrue("FAIL: ScriptEnvironment did not bind key.", se
				.getBindings().containsKey("test binding"));
		assertTrue("FAIL: ScriptEnvironment did not bind value.", se
				.getBindings().containsValue(5));
		
		verify(fakeContainer).injectObject("test binding", 5);
		
		se.unlink(fakeContainer);
		se.addBinding("another test binding", 57);
		
		verify(fakeContainer, times(1)).injectObject("test binding", 5);
		verify(fakeContainer, times(1)).injectObject("test binding", 5);
	}

	@Test
	public void testAddBindings() throws ScriptException {
		ScriptEnvironment se = new ScriptEnvironment();
		se.link(fakeContainer);
		se.addBindings(sampleMapOne);
		
		assertTrue("FAIL: ScriptEnvironment did not bind map.", se.getBindings().equals(sampleMapOne));
		
		verify(fakeContainer).injectObject("one", 1);
		verify(fakeContainer).injectObject("two", "two");
		verify(fakeContainer).injectObject("three", 3.0);
		
		se.addBindings(sampleMapTwo);
		
		verify(fakeContainer).injectObject("a", new Integer(1));
		verify(fakeContainer).injectObject("b", new Double(2.34));
		
		se.unlink(fakeContainer);
		
		se.addBindings(sampleMapThree);
		
		verify(fakeContainer, times(1)).injectObject("one", 1);
		verify(fakeContainer, times(1)).injectObject("two", "two");
		verify(fakeContainer, times(1)).injectObject("three", 3.0);
		verify(fakeContainer, times(1)).injectObject("a", new Integer(1));
		verify(fakeContainer, times(1)).injectObject("b", new Double(2.34));
		
		verify(fakeContainer, never()).injectObject("alpha", 943d);
		verify(fakeContainer, never()).injectObject("beta", 943l);
	}

	@Test
	public void testLink() throws ScriptException {
		ScriptEnvironment se = new ScriptEnvironment(sampleMapOne);
		se.link(fakeContainer);
		verify(fakeContainer).injectObject("one", 1);
		verify(fakeContainer).injectObject("two", "two");
		verify(fakeContainer).injectObject("three", 3.0);
		
		se.link(fakeContainer);
		verify(fakeContainer, times(1)).injectObject("one", 1);
		verify(fakeContainer, times(1)).injectObject("two", "two");
		verify(fakeContainer, times(1)).injectObject("three", 3.0);
	}

	@Test
	public void testUnlink() throws ScriptException {
		ScriptEnvironment se = new ScriptEnvironment(sampleMapOne);
		se.link(fakeContainer);
		se.unlink(fakeContainer);
		verify(fakeContainer).removeObject("one");
		verify(fakeContainer).removeObject("two");
		verify(fakeContainer).removeObject("three");
		
		se.unlink(fakeContainer);
		verify(fakeContainer, times(1)).removeObject("one");
		verify(fakeContainer, times(1)).removeObject("two");
		verify(fakeContainer, times(1)).removeObject("three");
	}

}
