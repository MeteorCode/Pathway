package com.meteorcode.pathway.test

import com.meteorcode.common.ForkTable
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers, FreeSpec}

/**
 * Created by hawk on 5/22/15.
 */
class ForkTableSpec extends WordSpec with Matchers with PropertyChecks with MockitoSugar {

  "A ForkTable" when {
    "empty" should {
      def target = new ForkTable[Int,Int]
      "have size 0" in {
        target should have size 0
      }
      "contain no keys" in {
        forAll { (i: Int) =>
         target should not contain i
        }
      }
      "return None when getting a key" in {
        forAll { (i: Int) =>
          target get i shouldBe None
        }
      }
      "return None when removing a key" in {
        forAll { (i: Int) =>
          target remove i shouldBe None
        }
      }
    }
    "newly created at the root level" should {
      def target = new ForkTable[Int,Int]
      "have no children" in {
        target.getChildren shouldBe 'empty
      }
      "have no parent" in {
        target.getParent shouldBe None
      }
      "be a root" in {
        target should be a 'root
      }
      "not be a leaf" in {
        target should not be a 'leaf
      }
    }
  }
}
