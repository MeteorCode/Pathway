package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.model.GridCoordinates;
import com.meteorcode.pathway.model.Payload;
import com.meteorcode.pathway.model.Tile;
import com.meteorcode.pathway.script.ScriptException;

public class GridTests {
	
	private Tile mockTile;
	private Map<String,Object> testMap;

	@Before
	public void setUp() throws Exception {
		mockTile = mock(Tile.class);
		when(mockTile.getPosition()).thenReturn(new GridCoordinates(0,1));
		testMap = new HashMap<String,Object>();
	}

	@Test
	public void testEventWithLocation() {
		Event target = new Event(null, null, null, mockTile){

			@Override
			public void evalEvent() throws ScriptException {
				// TODO Auto-generated method stub
				
			}};
			
		assertEquals(target.getPayload().where(), mockTile);
		
		target = new Event(null, null, mockTile){

			@Override
			public void evalEvent() throws ScriptException {
				// TODO Auto-generated method stub
				
			}};
			
		assertEquals(target.getPayload().where(), mockTile);
	}
	
	@Test
	public void testPayloadWithLocation() {
		Payload target = new Payload(mockTile);
		assertEquals(mockTile, target.location());
		assertEquals(mockTile, target.where());
		assertEquals(new Integer(0), target.x());
		assertEquals(new Integer(1), target.y());
		
		target = new Payload(testMap,mockTile);
		assertEquals(mockTile, target.location());
		assertEquals(mockTile, target.where());
		assertEquals(new Integer(0), target.x());
		assertEquals(new Integer(1), target.y());
	}
	
	@Test
	public void TestTile() {
		Tile target = new Tile(new GridCoordinates(5,9), Tile.Type.METAL_WALL);
		assertEquals(new GridCoordinates(5,9), target.getPosition());
		assertEquals(Tile.Type.METAL_WALL, target.getType());
		
		target = new Tile(new GridCoordinates(2,7), Tile.Type.EMPTY);
		assertEquals(new GridCoordinates(2,7),target.getPosition());
		assertEquals(Tile.Type.EMPTY, target.getType());
	}

	@Test
	public void miscGridCoordsTests() {
		GridCoordinates target = new GridCoordinates(256, 512);
		assertEquals(new Integer(256), target.x());
		assertEquals(new Integer(512), target.y());
		assertEquals("(256, 512)", target.toString());
		assertFalse(target == new GridCoordinates(3,8));
		assertFalse(target == new Object());
	}
}
