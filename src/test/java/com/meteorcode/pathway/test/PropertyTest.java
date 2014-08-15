package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.model.Property;
import com.meteorcode.pathway.script.ScriptException;

public class PropertyTest {
	private Property target;
	private Context mockContext, anotherMockContext;

	@Before
	public void setUp()  {
		mockContext = mock(Context.class);
		anotherMockContext = mock(Context.class);
	}

	@Test
	public void testContextLinking() {
		target = new Property(mockContext){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}};
		verify(mockContext).subscribe(target);
		
		target.changeContext(anotherMockContext);
		verify(mockContext).unsubscribe(target);
		verify(anotherMockContext).subscribe(target);
		verifyNoMoreInteractions(mockContext);
	}
	
	@Test
	public void testDrawID () {
		target = new Property(mockContext){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}};
			
		assertNull(target.getDrawID());
		target.setDrawID(123456);
		assertEquals(new Integer(123456), target.getDrawID());
		
		target = new Property(78910, mockContext){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}};
			
		assertEquals(new Integer(78910), target.getDrawID());
	}
	
	@Test
	public void testEval() throws ScriptException {		
		target = new Property(mockContext){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}};
		
		when(mockContext.eval("1+1")).thenReturn(2);
		target.eval("1+1");
		verify(mockContext, times(1)).eval("1+1");
	}
}
