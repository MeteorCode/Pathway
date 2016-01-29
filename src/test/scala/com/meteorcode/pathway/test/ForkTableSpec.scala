package com.meteorcode.pathway.test

import com.meteorcode.common.ForkTable

import org.scalatest.prop.PropertyChecks
import org.scalatest.{WordSpec, Matchers}

import scala.language.postfixOps

/**
 * Spec for the [[ForkTable]] data structure
 *
 * Created by hawk on 5/22/15.
 */

class ForkTableSpec extends WordSpec
  with Matchers
  with PropertyChecks {

  "A ForkTable" when {
    "empty" should {
      def target = new ForkTable[Int, Int]
      "have size 0" in {
        target should have size 0
      }
      "have chain size 0" in {
        target.chainSize shouldBe 0
      }
      "contain no keys" in {
        forAll { (i: Int) ⇒
          target should not contain i
        }
      }
      "return None when getting a key" in {
        forAll { (i: Int) ⇒
          target get i shouldBe None
        }
      }
      "return None when removing a key" in {
        forAll { (i: Int) ⇒
          target remove i shouldBe None
        }
      }
    }
    "at the root level" should {
      def target = new ForkTable[Int, Int]
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
        target shouldNot be a 'leaf
      }
      "allow iteration over all of its mappings" in {
        forAll{ (contents: Map[Int,Int]) ⇒
          val t = target
          contents.foreach { case ((k,v)) ⇒ t.put(k,v) }
          t.foreach{
            contents should contain (_)
          }
        }
      }
    }
    "forked" should {
      "know its children" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            // we are not testing for hash collisions here
            whenever(val1 != val2 ) {
              val target = new ForkTable[Int, Int]
              val aFork = target.fork()
              val anotherFork = target.fork()

              // this is so ScalaTest doesn't think the two forks are the same object
              aFork.put(key1, val1)
              anotherFork.put(key2, val2)

              target.getChildren should contain (aFork)
              target.getChildren should contain (anotherFork)
            }
        }
      }
      "not contain keys defined in its forks" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            whenever(key1 != key2) { // we are not testing for hash collisions here
              val target = new ForkTable[Int, Int]
              val aFork = target.fork()
              val anotherFork = target.fork()

              // this is so ScalaTest doesn't think the two forks are the same object
              aFork.put(key1, val1)
              anotherFork.put(key2, val2)

              target should not contain(key1 → val1, key2 → val2)
            }
        }
      }
      "not chain contain keys defined in its forks" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            whenever(key1 != key2) { // we are not testing for hash collisions here
              val target = new ForkTable[Int, Int]
              val aFork = target.fork()
              val anotherFork = target.fork()

              // this is so ScalaTest doesn't think the two forks are the same object
              aFork.put(key1, val1)
              anotherFork.put(key2, val2)

              target.chainContains(key1) shouldBe false
              target.chainContains(key2) shouldBe false
            }
        }
      }
      "not allow access keys defined in its forks" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            whenever(key1 != key2) { // we are not testing for hash collisions here
              val target = new ForkTable[Int, Int]
              val aFork = target.fork()
              val anotherFork = target.fork()

              // this is so ScalaTest doesn't think the two forks are the same object
              aFork.put(key1, val1)
              anotherFork.put(key2, val2)

              target.get(key1) shouldBe None
              target.get(key2) shouldBe None
            }
        }
      }
    }
    "a fork" should {
      "know its parent" in {
        val parent = new ForkTable[Int, Int]
        val fork = parent.fork()
        fork.getParent shouldBe Some(parent)
      }
      "not be a root" in {
        val target = new ForkTable[Int, Int].fork()
        target shouldNot be a 'root
      }
      "be a leaf" in {
        val target = new ForkTable[Int, Int].fork()
        target should be a 'leaf
      }
      "chain contain all keys in its parent" in {
        forAll {
          (key: Int, value: Int) ⇒
            val parent = new ForkTable[Int, Int]
            val fork = parent.fork()
            parent.put(key, value)
            fork.chainContains(key) shouldBe true
        }
      }
      "allow access to keys defined in its parent" in {
        forAll {
          (key: Int, value: Int) ⇒
            val parent = new ForkTable[Int, Int]
            val fork = parent.fork()
            parent.put(key, value)
            fork.get(key) should equal(parent.get(key))
        }
      }
      "not contain keys defined another fork at its level" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            whenever(key1 != key2) {
              // we are not testing for hash collisions here
              val parent = new ForkTable[Int, Int]
              val fork = parent.fork()
              val another = parent.fork()

              fork.put(key1, val1)

              fork should contain(key1 → val1)
              another should not contain (key1 → val1)

              another.put(key2, val2)

              another should contain(key2 → val2)
              fork should not contain (key2 → val2)
            }
        }
      }
      "not chain contain keys defined another fork at its level" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            whenever(key1 != key2) {
              // we are not testing for hash collisions here
              val parent = new ForkTable[Int, Int]
              val fork = parent.fork()
              val another = parent.fork()

              fork.put(key1, val1)

              fork.chainContains(key1) shouldBe true
              another.chainContains(key1) shouldBe false

              another.put(key2, val2)

              another.chainContains(key2) shouldBe true
              fork.chainContains(key2) shouldBe false
            }
        }
      }
      "not allow access to keys defined another fork at its level" in {
        forAll {
          (key1: Int, val1: Int, key2: Int, val2: Int) ⇒
            whenever(key1 != key2) {
              // we are not testing for hash collisions here
              val parent = new ForkTable[Int, Int]
              val fork = parent.fork()
              val another = parent.fork()

              fork.put(key1, val1)

              fork.get(key1) shouldBe Some(val1)
              another.get(key1) shouldBe None

              another.put(key2, val2)

              another.get(key2) shouldBe Some(val2)
              fork.get(key2) shouldBe None
            }
        }
      }
      "white out rather than remove keys defined in its parent" in {
        forAll {
          (key: Int, value: Int) ⇒
            val parent = new ForkTable[Int, Int]
            val fork = parent.fork()
            parent.put(key, value)

            fork.remove(key) shouldBe Some(value)

            parent should contain(key → value)
            fork should not contain (key → value)
            fork.chainContains(key) shouldBe false
            fork.get(key) shouldBe None
        }
      }
      "have chain size equal to its size plus the sizes of all parents" in {
        forAll {
          (parentContents: Map[Int, Int], myContents: Map[Int, Int]) ⇒
            val parent = new ForkTable[Int, Int]
            val fork = parent.fork()

            parentContents.foreach { case ((k, v)) ⇒ parent.put(k, v) }
            myContents.foreach { case ((k, v)) ⇒ fork.put(k, v) }

            fork.chainSize shouldEqual (parentContents.size + myContents.size)
        }
      }
      "have size equal to its size" in {
        forAll {
          (parentContents: Map[Int, Int], myContents: Map[Int, Int]) ⇒
            val parent = new ForkTable[Int, Int]
            val fork = parent.fork()
            parentContents.foreach { case ((k, v)) ⇒ parent.put(k, v) }
            myContents.foreach { case ((k, v)) ⇒ fork.put(k, v) }

            fork should have size myContents.size
        }
      }
      "allow iteration over all of the mappings in its chain" in {
        forAll {
          (parentContents: Map[Int, Int], myContents: Map[Int, Int]) ⇒
            val parent = new ForkTable[Int, Int]
            parentContents foreach { case ((k, v)) ⇒ parent.put(k, v) }
            val fork = parent.fork()
            myContents foreach { case ((k, v)) ⇒ fork.put(k, v) }

            val allContents = parentContents ++ myContents

            // basically, this is ensuring that the iterator iterates over all the mappings in
            // the parent's contents and my contents
            allContents.foldLeft(fork){
              (acc: ForkTable[Int,Int], kv: (Int, Int)) ⇒ acc.remove(kv._1); acc
            } shouldBe empty
        }
      }
    }
    "frozen" should {
      "not be modified by changes to lower levels" in {
       val root = new ForkTable[Int,Int]
       root.put(1,1)
       val level2 = root.fork()

       level2.put(2,2)
       level2.freeze()

       root.put(4,4)

       level2.get(4) shouldBe None
       level2 should not contain (4 → 4)
     }
   }
 }
}
