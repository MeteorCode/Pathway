package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.model.GridCoordinates;
import com.meteorcode.pathway.model.Property;
import com.meteorcode.pathway.model.Tile;
import com.meteorcode.pathway.model.TileProperty;
import com.meteorcode.pathway.script.ScriptException;

/**
 * Tests on the eventing system which do not involve Beanshell behaviour or use
 * mock Beanshell interpreters.
 * 
 * @author Hawk Weisman
 * 
 */
public class EventStackTest {

	private Context c, c2, mockContext;
	private Event mockEvent; // used when behaviour from Event is not being tested

	@Before
	public void setUp() throws Exception {
		c = new Context("C_1");
		c2 = new Context("C_2");
		mockEvent = mock(Event.class);
		when(mockEvent.isValid()).thenReturn(true);
		mockContext = mock(Context.class);
	}

	/**
	 * This is test 2 and test 3 from the large test fixture spec.
	 * 
	 * @throws ScriptException
	 */
	@Test
	public void basicStackOpsTest() throws ScriptException {
		c.fireEvent(mockEvent);
		assertEquals("FAIL: newly-fired event was not on top of EventStack",
				mockEvent, c.viewEventStack().peek());
		c.pump();
		assertTrue("FAIL: EventStack was not empty after pumping one event.", c
				.viewEventStack().isEmpty());
	}

	@Test
	public void basicPropertyTest() {
		assertNotNull(new Property(){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}});
		assertNotNull(new Property(1234567){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}});
		assertNotNull(new TileProperty(new Tile(new GridCoordinates(0, 0), Tile.Type.FLOOR), mockContext) {

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}
		});

		assertEquals(new Integer(1234567), new Property(1234567){

			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				// TODO Auto-generated method stub
				return false;
			}}.getDrawID());
	}

	@Test
	public void propertySubscriptionTest() throws ScriptException {
		// let's acknowledge right now that this is a terrible kludge and I
		// should be shot for it; this exists only because of Java's massive
		// failure to understand Scala's type system, making it impossible for
		// me to test this behaviour in any of the many sane ways I attempted
		// before doing it this way.
		
		Event getsSet = new Event("I should get a flag", c) {

			@Override
			public void evalEvent() throws ScriptException {
				assertTrue(payload.contains("TestFlag"));
				assertTrue((Boolean)payload.get("TestFlag"));
			}
		};

		Event staysUnset = new Event("I should not get a flag", c) {
			
			@Override
			public void evalEvent() throws ScriptException {
				assertFalse(payload.contains("TestFlag"));
			}
		
		};

		c.fireEvent(staysUnset);
		c.pump();
		
		Property testProp = new Property(c) {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				event.patchPayload("TestFlag", true);
				return true;
			}
		};

		c.fireEvent(getsSet);
		c.pump();

		testProp.changeContext(c2);
		c2.fireEvent(getsSet);
		c2.pump();

		c.fireEvent(staysUnset);
		c.pump();
	}
	
	@Test
	public void propertyEventInvalidationTest() throws ScriptException {
		Event getsInvalidated = new Event("I should be invalidated", c) {
			@Override
			public void evalEvent() throws ScriptException {
				assertFalse("FAIL: Event " + this + " in context "
						+ this.target + " was incorrect validity state",
						this.isValid());
			}
		};

		Event staysValid = new Event("I should stay valid", c) {
			@Override
			public void evalEvent() throws ScriptException {
				assertTrue(this.isValid());
			}
		};
		
		Property testProp = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				event.invalidate();
				return false;
			}
		};
		
		c.fireEvent(staysValid);
		c.pump();
		
		testProp.changeContext(c);
		
		c.fireEvent(getsInvalidated);
		c.pump();
	}
	
	@Test
	public void propertyReturnValueTest() throws ScriptException {
		Event staysValid = new Event("I should stay valid", c) {
			@Override
			public void evalEvent() throws ScriptException {
				assertTrue(this.isValid());
			}
		};
		
		Property testProp1 = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				return false;
			}
		};
		
		Property testProp2 = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				return true;
			}
		};
		
		Property testProp3 = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				event.invalidate();
				return false;
			}
		};
		
		c.fireEvent(staysValid);
		c.pump();
		
		testProp1.changeContext(c);
		testProp2.changeContext(c);
		testProp3.changeContext(c);
		
		c.fireEvent(staysValid);
		c.pump();
	}
	
	@Test
	public void stampingTest() throws ScriptException {
		Event staysValid = new Event("I should stay valid", c) {
			@Override
			public void evalEvent() throws ScriptException {
				assertTrue(this.isValid());
			}
		};
		
		Property testProp1 = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				event.stamp(this);
				return true;
			}
		};
		
		Property testProp2 = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				event.stamp(this);
				return true;
			}
		};
		
		c.fireEvent(staysValid);
		c.pump();
		
		assertFalse(staysValid.stampExists(testProp1));
		assertFalse(staysValid.stampExists(testProp2));
		
		testProp1.changeContext(c);
		c.fireEvent(staysValid);
		c.pump();
		
		assertTrue(staysValid.stampExists(testProp1));
		assertFalse(staysValid.stampExists(testProp2));
		
		testProp2.changeContext(c);
		
		c.fireEvent(staysValid);
		c.pump();
		
		assertTrue(staysValid.stampExists(testProp1));
		assertTrue(staysValid.stampExists(testProp2));	
		
		Property testProp3 = new Property() {
			@Override
			public boolean onEvent(Event event, Context publishedBy) {
				event.unstamp(this);
				return true;
			}
		};
		
		testProp3.changeContext(c);
		c.fireEvent(staysValid);
		c.pump();
		
		assertTrue(staysValid.stampExists(testProp1));
		assertTrue(staysValid.stampExists(testProp2));
		assertFalse(staysValid.stampExists(testProp3));	
	}
	
	@Test
	public void basicPayloadAndNameTest() throws ScriptException {
		Map<String, Object> payloadOne = new HashMap<String, Object>();
		payloadOne.put("testPayload1", 1);
		payloadOne.put("testPayload2", "I am also a payload.");
		Event testEvent = new Event("I have a payload!", payloadOne, c) {
			@Override
			public void evalEvent() throws ScriptException {
				assertEquals(1, payload.get("testPayload1"));
				assertEquals("I am also a payload.", payload.get("testPayload2"));
			}
		};
		
		c.fireEvent(testEvent);
		c.pump();
		
		assertEquals(payloadOne, testEvent.getPayload().toMap());
		assertEquals("I have a payload!", testEvent.getName());
		
		testEvent = new Event("My payload is added later!!", c) {
			@Override
			public void evalEvent() throws ScriptException {
				assertEquals(1, payload.get("testPayload1"));
				assertEquals("I am also a payload.", payload.get("testPayload2"));
			}
		};
		
		testEvent.patchPayload(payloadOne);
		c.fireEvent(testEvent);
		c.pump();
		
		assertEquals(payloadOne, testEvent.getPayload().toMap());
		assertEquals("My payload is added later!!", testEvent.getName());
	}

	@Test
	public void childInvalidationTest() throws ScriptException {
		class EventShouldBeInvalidated extends Event {
			public EventShouldBeInvalidated(String name, Context origin) {
				super(name, origin);
				// TODO Auto-generated constructor stub
			}

			@Override
			public void evalEvent() throws ScriptException {
				fail();
			}
		};
		
		Event e1 = new EventShouldBeInvalidated("I should be invalidated", c);
		Event e2 = new EventShouldBeInvalidated("I should also be invalidated", c);
		Event e3 = new EventShouldBeInvalidated("I should also be invalidated", c);
		
		c.fireEvent(e1);
		e1.fireEventChild(e2);
		e2.fireEventChild(e3);
		
		e1.invalidate();
		c.pump();
		
		assertFalse(e1.isValid());
		assertFalse(e2.isValid());
		assertFalse(e3.isValid());
		
	}
}
