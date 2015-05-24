package com.meteorcode.pathway.test

import com.meteorcode.pathway.model.{Context, GameObject}

import org.mockito.Mockito._

import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}

/**
 * Created by hawk on 5/22/15.
 */
class GameObjectSpec extends WordSpec with Matchers with PropertyChecks with MockitoSugar {

  "A GameObject" when {
    "given a GameID" should {
      "return the correct GameID" in {
        forAll { (gid: Long) => new GameObject(gid){}.getGameID shouldBe gid }
      }
    }
    "attached to a Context" should {
      "change to a different Context" in {
        val ctx1 = mock[Context]
        val ctx2 = mock[Context]
        val target = new GameObject(){}

        target.getContext shouldBe null

        target changeContext ctx1
        verify (ctx1, times(1)) addGameObject target
        target.getContext shouldBe ctx1

        target changeContext ctx2
        verify (ctx1, times(1)) removeGameObject target
        verify (ctx2, times(1)) addGameObject target
        target.getContext shouldBe ctx2
        verifyNoMoreInteractions(ctx1)

      }
    }
  }

}
