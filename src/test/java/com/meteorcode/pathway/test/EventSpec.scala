package com.meteorcode.pathway.test

/**
 * Created by hawk on 5/11/15.
 */
import com.meteorcode.pathway.model.{Context, GameObject}
import com.meteorcode.pathway.script.ScriptException
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers, FreeSpec}
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

import me.hawkweisman.util.randomJavaIdent
/**
 * Created by hawk on 5/8/15.
 */
class EventSpec extends FreeSpec with Matchers with PropertyChecks with MockitoSugar {

  "An Event" - {
    "when evaluating BeanShell expressions" - {
      "should evaluate the empty string to null" in {
        new Context("Context C_1") eval "" shouldBe null
      }
      "should throw a null pointer exception when evaluating null" in {
        val ex = the [ScriptException] thrownBy (new Context("Context C_1") eval null.asInstanceOf[String])
        ex should have message "Null is not a valid script."
        ex getCause() shouldBe a [NullPointerException]
      }
      "should evaluate integer arithmetic expressions correctly" in {
        forAll { (a: Int, b: Int) =>
          new Context("Context C_1") eval s"$a + $b" should equal (a + b)
        }
        forAll { (a: Int, b: Int) =>
          new Context("Context C_1") eval s"$a - $b" should equal (a - b)
        }
        forAll { (a: Int, b: Int) =>
          new Context("Context C_1") eval s"$a / $b" should equal (a / b)
        }
        forAll { (a: Int, b: Int) =>
          new Context("Context C_1") eval s"$a * $b" should equal (a * b)
        }
      }
      "should obey the arithmetic properties of equality" in {
        forAll { (a: Int, b: Int) =>
          val target = new Context("Context C_1")
          target.eval(s"$a + $b") shouldEqual target.eval(s"$b + $a")
          target.eval(s"$a - $b") shouldEqual target.eval(s"$b - $a")
          target.eval(s"$a * $b") shouldEqual target.eval(s"$b * $a")
        }
        forAll { (a: Int, b: Int, c: Int) =>
          val target = new Context("Context C_1")
          target.eval(s"$a + ($b + $c)") shouldEqual target.eval(s"($a + $b) + c")
          target.eval(s"$a - ($b - $c)") shouldEqual target.eval(s"($a - $b) - c")
        }
      }

    }

  }

}
