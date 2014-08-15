/**
 * 
 */
package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.GameObject;
import com.meteorcode.pathway.script.ScriptException;

/**
 * @author Hawk Weisman
 * 
 */
public class ContextTest {

	private GameObject mockGameObjectOne;
	private GameObject mockGameObjectTwo;
	private Context target;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		mockGameObjectOne = mock(GameObject.class);
		mockGameObjectTwo = mock(GameObject.class);
		target = new Context("Target");
	}

	/**
	 * Test method for
	 * {@link com.meteorcode.pathway.model.Context#getGameObjects()}.
	 */
	@Test
	public void testAddAndGetGameObjects() {
		target.addGameObject(mockGameObjectOne);
		assertTrue(target.getGameObjects().contains(mockGameObjectOne));
		target.addGameObject(mockGameObjectTwo);
		assertTrue(target.getGameObjects().contains(mockGameObjectOne));
		assertTrue(target.getGameObjects().contains(mockGameObjectTwo));

	}

	/**
	 * Test method for
	 * {@link com.meteorcode.pathway.model.Context#removeGameObject(com.meteorcode.pathway.model.GameObject)}
	 * .
	 */
	@Test
	public void testRemoveGameObject() {
		target.addGameObject(mockGameObjectOne);
		target.addGameObject(mockGameObjectTwo);
		assertTrue(target.getGameObjects().contains(mockGameObjectOne));
		assertTrue(target.getGameObjects().contains(mockGameObjectTwo));
		target.removeGameObject(mockGameObjectTwo);
		verifyNoMoreInteractions(mockGameObjectOne);
		assertTrue(target.getGameObjects().contains(mockGameObjectOne));
		assertFalse(target.getGameObjects().contains(mockGameObjectTwo));
	}
	
	@Test
	public void beanshellInteractionTests() throws ScriptException {
		target.injectObject("testvar", 4);
		assertEquals(target.eval("testvar"), 4);
		target.removeObject("testvar");
		assertNull(target.eval("testvar"));
	}
	
	@Test
	public void toStringTest() {
		assertEquals("[Target Context][]", target.toString());
	}

}
