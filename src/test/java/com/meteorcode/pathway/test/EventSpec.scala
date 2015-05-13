package com.meteorcode.pathway.test

/**
 * Created by hawk on 5/11/15.
 */

import com.meteorcode.pathway.logging.{LoggerFactory,NullLogger}
import com.meteorcode.pathway.model.{Context, Event,Property}
import com.meteorcode.pathway.script.ScriptException

import me.hawkweisman.util._

import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers, FreeSpec}

import org.mockito.Mockito.verify

import scala.util.Random

class EventSpec extends FreeSpec with Matchers with PropertyChecks with MockitoSugar {
  // quash the obnoxious and unnecessary log messages during testing
  LoggerFactory setLogger new NullLogger

  val random = new Random()
  val nonMaxInt = Gen.choose(Integer.MIN_VALUE+1, Integer.MAX_VALUE - 1)
  val ident: Gen[String] = for {
    len  <- Gen.choose(1,500) // 500 seems reasonable
    name <- randomJavaIdent(len)(random)
  } yield name

  def target = new Context("Target")

  "A Property" - {
    "when instantiated with the 0-parameter constructor" - {
      "should not be null" in {
        new Property() { override def onEvent(event: Event, publishedBy:Context) = false} should not be (null)
      }
    }
    "when instantiated with a known DrawID" - {
      "should not be null" in {
        forAll {
          (id: Int) =>
            new Property(id) {
              def onEvent(event: Event, publishedBy: Context) = false
            } should not be (null)
        }
      }
      "should know its own DrawID" in {
        forAll {
          (id: Int) =>
            new Property(id) {
              def onEvent(event: Event, publishedBy: Context) = false
            }.getDrawID should equal(id)
        }
      }
    }
    "when stamping Events" - {
      "should insert its stamp into the Event's payload" in {
          val c = target
          val e = new Event("Gets stamped", c) { def evalEvent() = {}}
          val p = new Property() {
            def onEvent(event: Event, publishedBy:Context) = {
              event.stamp(this)
              false
            }
          }

          p changeContext c
          c fireEvent e
          c pump

          e.stampExists(p) shouldBe true
          e.getPayload.stamps should contain (p)
        }
      "should successfully stamp an Event that has been stamped by a different Property" in {
        val c = target
        val e = new Event("Gets stamped", c) { def evalEvent() = {}}
        val p1 = new Property() {
          def onEvent(event: Event, publishedBy:Context) = {
            event.stamp(this)
            true
          }
        }
        val p2 = new Property() {
          def onEvent(event: Event, publishedBy:Context) = {
            event.stamp(this)
            false
          }
        }

        p1 changeContext c
        p2 changeContext c
        c fireEvent e
        c pump

        e.stampExists(p1) shouldBe true
        e.stampExists(p2) shouldBe true
      }
      "should unstamp itself without disturbing the stamps of other Properties" in {
        val c = target
        val e = new Event("Gets stamped", c) { def evalEvent() = {}}
        val p1 = new Property() {
          def onEvent(event: Event, publishedBy:Context) = {
            event.stamp(this)
            false
          }
        }
        val p2 = new Property() {
          def onEvent(event: Event, publishedBy:Context) = {
            event.unstamp(this)
            false
          }
        }

        p1 changeContext c
        p2 changeContext c
        c fireEvent e
        c pump

        e.stampExists(p1) shouldBe true
        e.stampExists(p2) shouldBe false
      }
    }
    "when attached to a Context" - {
      "should only effect events in the Context it is attached to" in {
        // this test is ported from the property subscription test in the
        // original JUnit test suite. A more elegant method of testing this
        // is probably possible and I should try and come up with it eventually
        val c = target
        val c2 = new Context("Other Target")
        val getsSet: Event = new Event("I should get a flag", c) {
          @throws(classOf[ScriptException])
          def evalEvent {
            payload.toMap keySet() should contain ("TestFlag")
            payload.get("TestFlag") shouldEqual true
          }
        }

        val staysUnset: Event = new Event("I should not get a flag", c) {
          @throws(classOf[ScriptException])
          def evalEvent {
            payload.toMap keySet() should not contain "TestFlag"
          }
        }

        c fireEvent staysUnset
        c pump

        val testProp: Property = new Property(c) {
          def onEvent(event: Event, publishedBy: Context): Boolean = {
            event.patchPayload("TestFlag", true)
            true
          }
        }

        c fireEvent getsSet
        c pump

        testProp changeContext c2
        c2 fireEvent getsSet
        c2 pump

        c fireEvent staysUnset
        c pump
      }
    }
    "which returns true" - {
      "should interrupt event stack evaluation" in {
        val c = target
        val staysValid: Event = new Event("I should stay valid", c) {
          @throws(classOf[ScriptException])
          def evalEvent { this shouldBe 'valid }
        }

        val testProp1: Property = new Property() {
          def onEvent(event: Event, publishedBy: Context): Boolean = false
        }

        val testProp2: Property = new Property() {
          def onEvent(event: Event, publishedBy: Context): Boolean = true
        }

        val testProp3: Property = new Property() {
          def onEvent(event: Event, publishedBy: Context): Boolean = {
            event.invalidate(); false
          }
        }

        c.fireEvent(staysValid)
        c.pump

        testProp1.changeContext(c)
        testProp2.changeContext(c)
        testProp3.changeContext(c)

        c.fireEvent(staysValid)
        c.pump
      }
    }
  }
  "A Context" - {
    "when evaluating event stacks" - {
      "should place an Event onto the stack when it is fired" in {
        val mockEvent = mock[Event]
        val t = target
        t fireEvent mockEvent
        t viewEventStack() should contain only mockEvent
      }
      "should remove an Event from the stack after pump()" in {
        val mockEvent = mock[Event]
        val t = target
        t fireEvent mockEvent
        t viewEventStack() should contain only mockEvent

        t.pump
        t.viewEventStack() shouldBe 'empty
      }
    }
  }
  "An Event" - {
    "when evaluating BeanShell expressions" - {
      "should evaluate the empty string to null" in {
        target eval "" shouldBe null
      }
      "should throw a null pointer exception when evaluating null" in {
        val ex = the [ScriptException] thrownBy (target eval null.asInstanceOf[String])
        ex should have message "Null is not a valid script."
        ex getCause() shouldBe a [NullPointerException]
      }
      "should evaluate integer arithmetic expressions correctly" in {
        forAll (nonMaxInt, nonMaxInt) { (a: Int, b: Int) =>
          target eval s"$a + $b" should equal(a + b)
        }
        forAll (nonMaxInt, nonMaxInt)  { (a: Int, b: Int) =>
          target eval s"$a - $b" should equal(a - b)
        }
        forAll (nonMaxInt, nonMaxInt) { (a: Int, b: Int) =>
          target eval s"$a / $b" should equal(a / b)
        }
        forAll (nonMaxInt, nonMaxInt) { (a: Int, b: Int) =>
          target eval s"$a * $b" should equal(a * b)
        }
      }
      "should obey the arithmetic properties of equality" in {
        forAll (nonMaxInt, nonMaxInt) { (a: Int, b: Int) =>
          target.eval(s"$a + $b") shouldEqual target.eval(s"$b + $a")
          target.eval(s"$a * $b") shouldEqual target.eval(s"$b * $a")
        }
        forAll(nonMaxInt, nonMaxInt, nonMaxInt) { (a: Int, b: Int, c: Int) =>
          target.eval(s"$a + ($b + $c)") shouldEqual target.eval(s"($a + $b) + $c")
          target.eval(s"$a + ($b - $c)") shouldEqual target.eval(s"($a + $b) - $c")
        }
      }
      "should be able to assign a variable within its context to the result of an expression" in {
        forAll (ident, nonMaxInt, nonMaxInt)
        { (name: String, a: Int, b: Int) =>
          val ctx = target
          val e = new Event(ctx) {
            override def evalEvent() = {
              target.eval(s"$name = $a + $b")
            }
          }

          ctx fireEvent e
          ctx.pump
          ctx eval name shouldEqual a + b
        }
        forAll (ident, nonMaxInt, nonMaxInt)
        { (name: String, a: Int, b: Int) =>
          val ctx = target
          val e = new Event(ctx) {
            override def evalEvent() = {
              target.eval(s"$name = $a - $b")
            }
          }

          ctx fireEvent e
          ctx.pump
          ctx eval name shouldEqual a - b
        }
        forAll (ident, nonMaxInt, nonMaxInt)
        { (name: String, a: Int, b: Int) =>
          val ctx = target
          val e = new Event(ctx) {
            override def evalEvent() = {
              target.eval(s"$name = $a *$b")
            }
          }

          ctx fireEvent e
          ctx.pump
          ctx eval name shouldEqual a * b
        }
      }
      "should be able to overwrite an existing variable within its context" in {
        forAll (ident, nonMaxInt, nonMaxInt)
        { (name: String, a: Int, b: Int) =>
          val ctx = target
          ctx eval s"$name = $a"
          ctx eval name shouldEqual a

          val e = new Event(ctx) {
            override def evalEvent() = {
              target.eval(s"$name = $b")
            }
          }

          ctx fireEvent e
          ctx.pump
          ctx eval name shouldEqual b
        }
      }
    }
    "when invalidated" - {
      "should invalidate any child Events" in {
        class InvalidationTarget(name: String, origin: Context) extends Event(name, origin) {
          def evalEvent = fail("Event should have been invalidated")
        }
        val c = target
        val e1 = new InvalidationTarget("I should be invalidated", c)
        val e2 = new InvalidationTarget("I should also be invalidated", c)
        val e3 = new InvalidationTarget("I should also be invalidated", c)

        c fireEvent e1
        e1 fireEventChild e2
        e2 fireEventChild e3

        e1 invalidate()
        c pump

        e1 should not be 'valid
        e2 should not be 'valid
        e3 should not be 'valid
      }
    }

  }

}
