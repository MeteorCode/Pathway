package com.meteorcode.pathway.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.script.ScriptException;

/**
 * Tests on the eventing system which involve Beanshell.
 * 
 * @author Hawk
 * 
 */
public class EventBSHTest {

	private Context c;

	@Before
	public void setUp() throws Exception {
		c = new Context("Context C_1");
	}

	@Test
	public void testNullEvals() throws ScriptException {
		try {
			c.eval((String)null);
			fail("Eval(null) did not throw ScriptException as expected");
		} catch (ScriptException se) {
			assertEquals("Null is not a valid script.", se.getMessage());
			assertTrue(se.getCause() instanceof NullPointerException);
		}

		assertEquals(null, c.eval(""));
	}

	@Test
	public void testBSHMathEval() throws ScriptException {

		// test identity property
		assertEquals(3, c.eval("3"));

		// basic operators tests
		assertEquals(4, c.eval("2+2"));
		assertEquals(2.5, c.eval("1.2 + 1.3"));
		assertEquals(3, c.eval("5 - 2"));
		assertEquals(2, c.eval("10/5"));

		// test properties of equality
		assertEquals("FAILED: symmetric property of equality", c.eval("1+4"),
				c.eval("4+1"));
		assertEquals("FAILED: associative property of equality",
				c.eval("1 + (4 + 7)"), c.eval("(1 + 4) + 7"));
		assertEquals("FAILED: distributive property of equality",
				c.eval("2*(3 + 4)"), c.eval("2 * 3 + 2 * 4"));

		// test operator precedence
		assertEquals(0, c.eval("1+2^3"));
		assertEquals(2, c.eval("1+(2^3)"));
		assertEquals(0, c.eval("(1+2)^3"));
	}

	@Test
	public void testVariableSetting() throws ScriptException {
		c.eval("something = 1");
		assertEquals(1, c.eval("something"));

		Event e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("something = 2");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(2, c.eval("something"));
	}

	@Test
	public void testEventMathEval() throws ScriptException {
		Event e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("testvar = 2+2");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(4, c.eval("testvar"));

		e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("testvar = 1.2 + 1.3");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(2.5, c.eval("testvar"));

		e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("testvar = 5 - 2");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(3, c.eval("testvar"));

		e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("testvar = 10/5");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(2, c.eval("testvar"));
	}

	@Test
	public void testEventPropertiesOfEquality() throws ScriptException {
		Event e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("a = 1 + 2");
				target.eval("b = 2 + 1");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(c.eval("a"), c.eval("b"));

		e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("a = 1 + (4 + 7)");
				target.eval("b = (1 + 4) + 7");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(c.eval("a"), c.eval("b"));

		e = new Event(c) {
			@Override
			public void evalEvent() throws ScriptException {
				target.eval("a = 2*(3 + 4)");
				target.eval("b = 2 * 3 + 2 * 4");
			}
		};

		c.fireEvent(e);
		c.pump();
		assertEquals(c.eval("a"), c.eval("b"));
	}

	/**
	 * Runs basically the same battery from
	 * {@link com.meteorcode.pathway.test.EventStackTest#propertySubscriptionTest()}
	 * , but with Properties created from Beanshell
	 * 
	 * @throws ScriptException
	 */
	@Test
	public void propertyBSHInstantiationAndSubscriptionTest() throws ScriptException {
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

		// HUGE STRING EVAL TEST
		c.injectObject("selfReference", c);
		c.eval("import com.meteorcode.pathway.model.*");
		c.eval("class MyProperty extends Property { MyProperty(Context c) { super(c);} public boolean onEvent(Event event, Context publishedBy) { event.patchPayload(\"TestFlag\", true); return true; }}");
		
		c.fireEvent(staysUnset);
		c.pump();
		
		c.eval("new MyProperty(selfReference);");

		c.fireEvent(getsSet);
		c.pump();
	}
	
	/**
	 * Runs basically the same battery from
	 * {@link com.meteorcode.pathway.test.EventStackTest#propertyEventInvalidationTest()}
	 * , but with Properties created from Beanshell
	 * 
	 * @throws ScriptException
	 */
	@Test
	public void propertyBSHInstantiationAndEventInvalidationTest() throws ScriptException {
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
		// HUGE STRING EVAL TEST
		c.injectObject("selfReference", c);
		c.eval("import com.meteorcode.pathway.model.*");
		c.eval("class MyProperty extends Property { MyProperty(Context c) { super(c);} public boolean onEvent(Event event, Context publishedBy) { event.invalidate();return true; }}");
		
		c.fireEvent(staysValid);
		c.pump();
		
		c.eval("new MyProperty(selfReference);");

		c.fireEvent(getsInvalidated);
		c.pump();
	}
}