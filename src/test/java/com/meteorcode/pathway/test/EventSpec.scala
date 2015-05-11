package com.meteorcode.pathway.test

/**
 * Created by hawk on 5/11/15.
 */
import com.meteorcode.pathway.model.{Context, GameObject}
import com.meteorcode.pathway.script.ScriptException
import org.scalacheck.Gen
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers, FreeSpec}

class EventSpec extends FreeSpec with Matchers with PropertyChecks with MockitoSugar {

  val nonMaxInt = Gen.choose(Integer.MIN_VALUE+1, Integer.MAX_VALUE - 1)
  def target = new Context("Target")

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
          target.eval(s"$a + ($b + $c)") shouldEqual target.eval(s"($a + $b) + c")
          target.eval(s"$a - ($b - $c)") shouldEqual target.eval(s"($a - $b) - c")
        }
      }

    }

  }

}
