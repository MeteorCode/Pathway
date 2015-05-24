package com.meteorcode.pathway.test

/**
 * Created by hawk on 5/11/15.
 */

import com.meteorcode.pathway.logging.{LoggerFactory,NullLogger}
import com.meteorcode.pathway.model._
import com.meteorcode.pathway.script.ScriptException

import me.hawkweisman.util._

import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers, FreeSpec}

import org.mockito.Mockito._

import scala.util.Random
import scala.collection.JavaConversions._

class ModelSpec extends FreeSpec with Matchers with PropertyChecks with MockitoSugar {
  // quash the obnoxious and unnecessary log messages during testing
  LoggerFactory setLogger new NullLogger

  val random = new Random()

  // generates integers that are not MAX_VALUE or MIN_VALUE
  val nonMaxInt = Gen.choose(Integer.MIN_VALUE+1, Integer.MAX_VALUE - 1)

  // generates random Java identifiers
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
      "should allow a new DrawID to be set" in {
        forAll { (id: Int) =>
          val p = new Property {
            def onEvent(event: Event, publishedBy: Context) = false
          }

          p.getDrawID should not be id

          p setDrawID id

          p.getDrawID shouldBe id
        }
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
            true
          }
        }

        p1 changeContext c
        p2 changeContext c
        c fireEvent e
        c pump

        e.getPayload.stamps should contain allOf (p1, p2)

        e.stampExists(p1) shouldBe true
        e.stampExists(p2) shouldBe true
      }
      "should unstamp itself without disturbing the stamps of other Properties" in {
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
            event.unstamp(this)
            true
          }
        }

        p1 changeContext c
        p2 changeContext c
        c fireEvent e
        c pump

        e.getPayload.stamps should contain only (p1)

        e.stampExists(p1) shouldBe true
        e.stampExists(p2) shouldBe false
      }
    }
    "when interacting with a Context" - {
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
      "should subscribe and unsubscribe on linking and unlinking" in {
        val ctx1 = mock[Context]
        val ctx2 = mock[Context]
        val p = new Property(ctx1) {
          override def onEvent(event: Event,publishedBy: Context) = false
        }

        verify(ctx1) subscribe p

        p changeContext ctx2

        verify(ctx1) unsubscribe p
        verify(ctx2) subscribe p
        verifyNoMoreInteractions(ctx1)
      }
      "should evaluate BeanShell against the Context it is attached to" in {
        val ctx = mock[Context]
        doReturn(2).when(ctx).eval("1+1")

        val p = new Property(ctx) {
          def onEvent(event: Event, publishedBy: Context) = false
        }

        p eval s"1+1"

        verify(ctx, times(1)) eval s"1+1"
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
    "when instantiated from a BeanShell script" -{
      "should set a flag on an Event" in {
        val c = target
        val flagged = new Event("I get a flag", c) {
          def evalEvent() { payload contains "TestFlag" shouldBe true}
        }
        val unflagged = new Event("I don't get a flag", c) {
          def evalEvent() { payload contains "TestFlag" shouldBe false}
        }

        c injectObject ("self", c)
        c eval "import com.meteorcode.pathway.model.*"
        c eval """
class MyProperty extends Property {
  MyProperty(Context c) {super(c);}
  public boolean onEvent(Event event, Context publishedBy) {
    event.patchPayload("TestFlag", true); return true;
  }
}
               """
        c fireEvent unflagged
        c pump

        c eval "new MyProperty(self);"

        c fireEvent flagged
        c pump
      }
      "should invalidate an Event" in {
        val c = target
        val valid = new Event("I stay valid", c) {
          def evalEvent() { this shouldBe 'valid }
        }
        val invalid = new Event("I get invalidated", c) {
          def evalEvent() { this should not be 'valid }
        }

        c injectObject ("self", c)
        c eval "import com.meteorcode.pathway.model.*"
        c eval """
class MyProperty extends Property {
MyProperty(Context c) {super(c);}
public boolean onEvent(Event event, Context publishedBy) {
  event.invalidate(); return true;
}
}
               """
        c fireEvent valid
        c pump

        c eval "new MyProperty(self);"

        c fireEvent invalid
        c pump
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
    "should know its own name" in {
      forAll { (name: String) =>
        val t = new Context(name)
        t.getName shouldEqual name
        t.toString shouldEqual s"[$name Context][]"
      }
    }
    "should allow GameObjects to be added" in {
      val t = target
      val obj1 = mock[GameObject]
      val obj2 = mock[GameObject]

      t addGameObject obj1
      t.getGameObjects should contain only obj1

      t addGameObject obj2
      t.getGameObjects should contain allOf(obj1, obj2)
    }
    "should allow GameObjects to be removed" in {
      val t = target
      val obj1 = mock[GameObject]
      val obj2 = mock[GameObject]

      t addGameObject obj1
      t addGameObject obj2
      t.getGameObjects should contain(obj1)
      t.getGameObjects should contain(obj2)

      t removeGameObject obj2
      t.getGameObjects should contain only obj1
    }
    "should support arbitrary Object injection" in {
      forAll (ident, arbitrary[List[AnyVal]]) {
        (name: String, thing: List[AnyVal]) =>
          val t = target
          t injectObject(name, thing)
          t eval name shouldEqual thing
      }
      forAll (ident, arbitrary[Map[AnyVal,AnyVal]]) {
        (name: String, thing: Map[AnyVal, AnyVal]) =>
          val t = target
          t injectObject(name, thing)
          t eval name shouldEqual thing
      }
      forAll (ident, arbitrary[String]) {
        (name: String, thing: String) =>
          val t = target
          t injectObject(name, thing)
          t eval name shouldEqual thing
      }
    }
    "should allow injected Objects to be removed" in {
      forAll (ident, arbitrary[List[AnyVal]]) { (name: String, thing: List[AnyVal]) =>
        val t = target
        t injectObject(name, thing)
        t eval name shouldEqual thing

        t removeObject name
        t eval name shouldBe null
      }
      forAll (ident, arbitrary[Map[AnyVal,AnyVal]]) {
        (name: String, thing: Map[AnyVal, AnyVal]) =>
          val t = target
          t injectObject(name, thing)
          t eval name shouldEqual thing

          t removeObject name
          t eval name shouldBe null
      }
      forAll (ident, arbitrary[String]) {
        (name: String, thing: String) =>
          val t = target
          t injectObject(name, thing)
          t eval name shouldEqual thing

          t removeObject name
          t eval name shouldBe null
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
    "with a Payload attached"  - {
      "should contain the expected items" in {
        forAll { (name: String, obj: List[Int]) =>
          new Event("I have a payload", Map[String, Object](name -> obj), target) {
            def evalEvent = {}
          } getPayload() get name shouldEqual obj
        }
        forAll { (name: String, obj: Map[String, Int]) =>
          new Event("I have a payload", Map[String, Object](name -> obj), target) {
            def evalEvent = {}
          } getPayload() get name shouldEqual obj
        }
      }
      "should patch the payload with a new mapping" in {
        forAll { (name: String, obj: List[Int]) =>
          whenever(name != "") {
            val e = new Event("My payload is added later", target) {
              def evalEvent = {}
            }
            e patchPayload(name, obj)
            e getPayload() get name shouldEqual obj
          }
        }
        forAll { (name: String, obj: Map[String, Int]) =>
          whenever(name != "") {
            val e = new Event("My payload is added later", target) {
              def evalEvent = {}
            }
            e patchPayload(name, obj)
            e getPayload() get name shouldEqual obj
          }
        }
      }
      "should patch the payload with an existing Map" in {
        forAll { (name: String, obj: Map[String, List[Int]]) =>
          whenever(name != "") {
            val e = new Event("My payload is added later", target) {
              def evalEvent = {}
            }
            e patchPayload Map[String,Object](name -> obj)
            e getPayload() get name shouldEqual obj
          }
        }
      }
    }
    "with a Tile location" - {
      "should know its location" in {
        val mockTile = mock[Tile]
        val target = new Event("test",null,mockTile){override def evalEvent = {}}
        target.getPayload.where shouldBe mockTile
      }
    }
  }
  "A Payload" - {
    "with a Tile location" - {
      "should know the Tile it is located at" in {
        val mockTile = mock[Tile]
        val target = new Payload(mockTile)
        target.location shouldBe mockTile
        target.where shouldBe mockTile
      }
      "should know the Tile's coordinates" in {
        forAll { (x: Int, y: Int) =>
          val mockTile = mock[Tile]
          val target = new Payload(mockTile)
          when(mockTile getPosition) thenReturn(new GridCoordinates(x,y))

          target.x shouldEqual x
          target.y shouldEqual y
        }
      }
    }
  }
  "A Tile" - {
    "should know its type" in {
      val target = new Tile(new GridCoordinates(0,0), Tile.Type.EMPTY)
      target.getType shouldBe Tile.Type.EMPTY
    }
    "when created with the GridCoordinates constructor" - {
      "should know its type" in {
        val target = new Tile(new GridCoordinates(0,0), Tile.Type.EMPTY)
        target.getType shouldBe Tile.Type.EMPTY
      }
      "should know its position" in {
        forAll { (x: Int, y: Int) =>
          val target = new Tile(new GridCoordinates(x,y),Tile.Type.EMPTY)
          target.getPosition shouldEqual new GridCoordinates(x,y)
        }
      }
    }
    "when created with the (Int, Int) constructor" - {
      "should know its type" in {
        val target = new Tile(0,0, Tile.Type.EMPTY)
        target.getType shouldBe Tile.Type.EMPTY
      }
      "should know its position" in {
        forAll { (x: Int, y: Int) =>
          val target = new Tile(x,y,Tile.Type.EMPTY)
          target.getPosition shouldEqual new GridCoordinates(x,y)
        }
      }
    }
    "when occupied by an Entity" - {
      val mockEntity = mock[Entity]
      "should be occupied" in {
        val target = new Tile(0,0,Tile.Type.FLOOR)
        target.setEntity(mockEntity)
        target shouldBe 'occupied
      }
      "should know what Entity occupies it" in {
        val target = new Tile(0,0,Tile.Type.FLOOR)
        target.setEntity(mockEntity)
        target.getEntity shouldBe mockEntity
      }
    }
    "when unoccupied " - {
      "should not be occupied" in {
        val target = new Tile(0,0,Tile.Type.FLOOR)
        target should not be 'occupied
      }
      "should return Null when getEntity is called" in {
        val target = new Tile(0,0,Tile.Type.FLOOR)
        target.getEntity shouldBe null
      }
    }
  }
  "A GridCoordinates pair" - {
    "should have the correct x- and y-values" in {
      forAll { (x: Int, y: Int) =>
        val target = new GridCoordinates(x,y)
        target.getX shouldEqual x
        target.getY shouldEqual y
      }
    }
    "should toString() itself as an ordered pair" in {
      forAll { (x: Int, y: Int) =>
        val target = new GridCoordinates(x,y)
        target.toString shouldEqual s"($x, $y)"
      }
    }
    "should equal another GridCoordinates with the same x- and y-values" in {
      forAll { (x: Int, y: Int) =>
        new GridCoordinates(x,y) shouldEqual new GridCoordinates(x,y)
      }
    }
    "should not equal another GridCoordinates with different x- and y-values" in {
      forAll { (x1: Int, y1: Int, x2: Int, y2: Int) =>
        whenever(x1 != x2 || y1 != y2) {
          new GridCoordinates(x1,y1) should not equal new GridCoordinates(x2,y2)
        }
      }
    }
  }
}
