package com.meteorcode.pathway.test

import com.meteorcode.pathway.model.{GameObject, Context}
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers,FreeSpec}
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

import me.hawkweisman.util.randomJavaIdent
/**
 * Created by hawk on 5/8/15.
 */
class ContextSpec extends FreeSpec with Matchers with PropertyChecks with MockitoSugar {
  val random = new scala.util.Random

  val ident: Gen[String] = for {
    len  <- Gen.choose(1,500) // 500 seems reasonable
    name <- randomJavaIdent(len)(random)
  } yield name

  "A Context" - {
    "should know its own name" in {
      forAll { (name: String) =>
        val target = new Context(name)
        target.getName shouldEqual name
        target.toString shouldEqual s"[$name Context][]"
      }
    }
    "should allow GameObjects to be added" in {
      val target = new Context("Target")
      val obj1 = mock[GameObject]
      val obj2 = mock[GameObject]

      target addGameObject obj1
      target.getGameObjects should contain only obj1

      target addGameObject obj2
      target.getGameObjects should contain allOf(obj1, obj2)
    }
    "should allow GameObjects to be removed" in {
      val target = new Context("Target")
      val obj1 = mock[GameObject]
      val obj2 = mock[GameObject]

      target addGameObject obj1
      target addGameObject obj2
      target.getGameObjects should contain(obj1)
      target.getGameObjects should contain(obj2)

      target removeGameObject obj2
      target.getGameObjects should contain only obj1
    }
    "should support arbitrary Object injection" in {
      forAll (ident, arbitrary[List[AnyVal]]) {
        (name: String, thing: List[AnyVal]) =>
          val target = new Context("Target")
          target injectObject(name, thing)
          target eval name shouldEqual thing
      }
      forAll (ident, arbitrary[Map[AnyVal,AnyVal]]) {
        (name: String, thing: Map[AnyVal, AnyVal]) =>
          val target = new Context("Target")
          target injectObject(name, thing)
          target eval name shouldEqual thing
      }
      forAll (ident, arbitrary[String]) {
        (name: String, thing: String) =>
          val target = new Context("Target")
          target injectObject(name, thing)
          target eval name shouldEqual thing
      }
    }

    "should allow injected Objects to be removed" in {
      forAll (ident, arbitrary[List[AnyVal]]) { (name: String, thing: List[AnyVal]) =>
        val target = new Context("Target")
        target injectObject(name, thing)
        target eval name shouldEqual thing

        target removeObject name
        target eval name shouldBe null
      }
      forAll (ident, arbitrary[Map[AnyVal,AnyVal]]) {
        (name: String, thing: Map[AnyVal, AnyVal]) =>
          val target = new Context("Target")
          target injectObject(name, thing)
          target eval name shouldEqual thing

          target removeObject name
          target eval name shouldBe null
      }
      forAll (ident, arbitrary[String]) {
        (name: String, thing: String) =>
          val target = new Context("Target")
          target injectObject(name, thing)
          target eval name shouldEqual thing

          target removeObject name
          target eval name shouldBe null
      }
    }
  }
}