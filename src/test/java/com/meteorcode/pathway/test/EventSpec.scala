package com.meteorcode.pathway.test

/**
 * Created by hawk on 5/11/15.
 */
import com.meteorcode.pathway.model.{Context, Event,Property}
import com.meteorcode.pathway.script.ScriptException

import me.hawkweisman.util._

import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers, FreeSpec}

import scala.util.Random

class EventSpec extends FreeSpec with Matchers with PropertyChecks with MockitoSugar {

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
          (id: Integer) =>
            new Property(id) {
              override def onEvent(event: Event, publishedBy:Context) = false
            } should not be (null)
        }
      }
      "should know its own DrawID" in {
        forAll {
          (id: Integer) =>
            new Property(id) {
              override def onEvent(event: Event, publishedBy:Context) = false
            }.getDrawID should equal (id)
        }
      }
    }
  }
  "A Context" - {
    "when evaluating event stacks" - {
      "should place an Event onto the stack when it is fired" in {
        val mockEvent = mock[Event]
        target fireEvent mockEvent
        target viewEventStack() should contain only mockEvent
      }
      "should remove an Event from the stack after pump()" in {
        val mockEvent = mock[Event]
        target fireEvent mockEvent
        target.viewEventStack() should contain only mockEvent

        target.pump
        target.viewEventStack() shouldBe 'empty
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

  }

}
