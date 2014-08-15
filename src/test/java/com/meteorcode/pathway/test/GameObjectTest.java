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

/**
 * @author Hawk Weisman
 *
 */
public class GameObjectTest {
	private Context mockContextOne;
	private Context mockContextTwo;
	private GameObject target;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		mockContextOne = mock(Context.class);
		mockContextTwo = mock(Context.class);
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.model.GameObject#getGameID()}.
	 */
	@Test
	public void testGetGameID() {
		target = new GameObject(1235453456l){};
		assertEquals(1235453456l,target.getGameID());
	}

	/**
	 * Test method for {@link com.meteorcode.pathway.model.GameObject#changeContext(com.meteorcode.pathway.model.Context)}.
	 */
	@Test
	public void testChangeContext() {
		target = new GameObject(){};
		assertEquals(null,target.getContext());
		
		target.changeContext(mockContextOne);
		verify(mockContextOne, times(1)).addGameObject(target);
		assertEquals(mockContextOne, target.getContext());
		
		target.changeContext(mockContextTwo);
		verify(mockContextOne, times(1)).removeGameObject(target);
		verify(mockContextTwo, times(1)).addGameObject(target);
		assertEquals(mockContextTwo, target.getContext());
	}

}
