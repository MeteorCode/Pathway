package com.meteorcode.pathway.test


import java.io.IOException
import java.util

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import bsh.{InterpreterError, EvalError, Interpreter}

import com.meteorcode.pathway.io.FileHandle
import com.meteorcode.pathway.script.{ScriptContainer, ScriptException, ScriptContainerFactory, ScriptEnvironment}

import me.hawkweisman.util._

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}

import org.mockito.Mockito._

/**
 * Created by hawk on 5/25/15.
 */
class ScriptSpec extends PathwaySpec with IdentGenerators {

  "A ScriptContainerFactory" when {
    "creating a new ScriptContainer with a specified environment" should {
      "link the new instance with the environment" in {
        val environment = mock[ScriptEnvironment]
        val container = new ScriptContainerFactory().getNewInstanceWithEnvironment(environment)
        verify(environment, times(1)).link(container)
      }
    }
    "creating a new ScriptContainer with a different Interpreter" should {
      "use the specified Interpreter rather than the default" in {
        val fakeInterpreter = mock[Interpreter]
        val container = new ScriptContainerFactory(fakeInterpreter).getNewInstance

        container.eval("")

        verify(fakeInterpreter, times(1)).eval("")
      }
    }
  }

  "A ScriptContainer" when {
    "evaluating a BeanShell script from a String" should {
      "pass through the result provided by the Interpreter" in {

        forAll { (script: String, result: String) =>
          whenever(script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)

            doReturn(result).when(fakeInterpreter).eval(script)
            val target = fakeFactory.getNewInstance

            target eval script shouldEqual result
            verify(fakeInterpreter, times(1)).eval(script)
          }
        }
      }
      "throw a ScriptException if the Interpreter throws an EvalError" in {
        forAll { (script: String) =>
          whenever(script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)

            when(fakeInterpreter.eval(script)) thenThrow new EvalError(null, null, null)

            val target = fakeFactory.getNewInstance

            val e = the[ScriptException] thrownBy target.eval(script)
            e.getCause shouldBe an[EvalError]
          }
        }
      }
      "throw a ScriptException if the Interpreter throws an InterpreterError" in {
        forAll { (script: String) =>
          whenever(script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)

            when(fakeInterpreter.eval(script)) thenThrow new InterpreterError("fake interp. error")

            val target = fakeFactory.getNewInstance

            val e = the[ScriptException] thrownBy target.eval(script)
            e.getCause shouldBe an[InterpreterError]
          }
        }
      }
    }
    "evaluating a BeanShell script from a FileHandle" should {
      "pass through the result provided by the interpreter" in {
        forAll { (script: String, result: String) =>
          whenever(script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
            val fakeFileHandle = mock[FileHandle]

            when(fakeFileHandle.readString) thenReturn script
            doReturn(result).when(fakeInterpreter).eval(script)

            val target = fakeFactory.getNewInstance

            target eval fakeFileHandle shouldEqual result

            verify(fakeInterpreter, times(1)).eval(script)
            verify(fakeFileHandle, times(1)).readString
          }
        }
      }
      "pass through any IOExceptions thrown by the FileHandle" in {
        val fakeInterpreter = mock[Interpreter]
        val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
        val fakeFileHandle = mock[FileHandle]

        when(fakeFileHandle.readString) thenThrow new IOException

        val target = fakeFactory.getNewInstance

        an[IOException] should be thrownBy target.eval(fakeFileHandle)
      }
      "throw a ScriptException if the Interpreter throws an EvalError" in {
        forAll { (script: String) =>
          whenever(script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
            val fakeFileHandle = mock[FileHandle]

            when(fakeFileHandle.readString) thenReturn script
            when(fakeInterpreter.eval(script)) thenThrow new EvalError(null, null, null)

            val target = fakeFactory.getNewInstance

            val e = the[ScriptException] thrownBy target.eval(fakeFileHandle)
            e.getCause shouldBe an[EvalError]
          }
        }
      }
      "throw a ScriptException if the Interpreter throws an InterpreterError" in {
        forAll { (script: String) =>
          whenever(script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
            val fakeFileHandle = mock[FileHandle]

            when(fakeFileHandle.readString) thenReturn script
            when(fakeInterpreter.eval(script)) thenThrow new InterpreterError("fake interp. error")

            val target = fakeFactory.getNewInstance

            val e = the[ScriptException] thrownBy target.eval(fakeFileHandle)
            e.getCause shouldBe an[InterpreterError]
          }
        }
      }
    }
    "injecting objects" should {
      "call the `set()` method on the interpreter" in {
        forAll(ident, arbitrary[List[Int]]) { (name: String, obj: List[Int]) =>
          val fakeInterpreter = mock[Interpreter]
          val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
          val target = fakeFactory.getNewInstance

          target injectObject(name, obj)
          verify(fakeInterpreter, times(1)).set(name, obj)
        }
      }
      "throw a ScriptException if object injection results in an EvalError" in {
        forAll(ident, arbitrary[List[Int]]) { (name: String, obj: List[Int]) =>
          val fakeInterpreter = mock[Interpreter]
          val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
          val target = fakeFactory.getNewInstance

          when(fakeInterpreter.set(name, obj)).thenThrow(new EvalError(null, null, null))

          val e = the[ScriptException] thrownBy target.injectObject(name, obj)
          e.getCause shouldBe an[EvalError]
          e should have message s"Error injecting $name into Beanshell"
        }
      }
    }
    "removing objects" should {
      "call the `unset()` method on the Interpreter" in {
        forAll(ident) { (name: String) =>
          val fakeInterpreter = mock[Interpreter]
          val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
          val target = fakeFactory.getNewInstance

          target removeObject name
          verify(fakeInterpreter, times(1)).unset(name)
        }
      }
      "throw a ScriptException if object removal results in an EvalError" in {
        forAll(ident) { (name: String) =>
          val fakeInterpreter = mock[Interpreter]
          val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
          val target = fakeFactory.getNewInstance

          when(fakeInterpreter.unset(name)).thenThrow(new EvalError(null, null, null))

          val e = the[ScriptException] thrownBy target.removeObject(name)
          e.getCause shouldBe an[EvalError]
          e should have message s"Error unbinding $name from Beanshell"
        }
      }
    }
    "accessing objects" should {
      "pass through the value from the Interpreter" in {
        forAll (ident, arbitrary[Int]) { (name: String, result: Int) =>
        whenever(name != "") {
          val fakeInterpreter = mock[Interpreter]
          val fakeFactory = new ScriptContainerFactory(fakeInterpreter)

          doReturn(result).when(fakeInterpreter).eval(name)
          val target = fakeFactory.getNewInstance

          target.access(name) shouldEqual result
          verify(fakeInterpreter, times(1)).eval(name)
        }
      }}
      "throw an IllegalArgumentException when attempting to access a reserved word" in {
        forAll (reservedWords) { (name: String) =>
          val target = new ScriptContainerFactory().getNewInstance
          the [IllegalArgumentException] thrownBy {
            target.access(name)
          } should have message "Variable name cannot be a Java reserved word."
        }
      }
      "throw an IllegalArgumentException when attempting to access a name with an invalid starting character" in {
        forAll (invalidIdentStart) { (name: String) =>
          val target = new ScriptContainerFactory().getNewInstance
          the [IllegalArgumentException] thrownBy {
            target.access(name)
          } should have message "Variable name was not a valid Java identifier; illegal character at position 0"
        }
      }
      "throw an IllegalArgumentException when attempting to access a name containing an invalid character" in {
        forAll (invalidAt) { case ((pos: Int, name: String)) =>
          val target = new ScriptContainerFactory().getNewInstance
          the [IllegalArgumentException] thrownBy {
            target.access(name)
          } should have message s"Variable name was not a valid Java identifier; illegal character at position $pos"
        }
      }
      "throw an IllegalArgumentException when attempting to access a name containing a space" in {
        forAll (spaceAt) { case ((pos: Int, name: String)) =>
          val target = new ScriptContainerFactory().getNewInstance
          the [IllegalArgumentException] thrownBy {
            target.access(name)
          } should have message s"Variable name was not a valid Java identifier; illegal character at position $pos"
        }
      }
    }
  }

  "A ScriptEnvironment" when {
    "initialized without bindings" should {
      "be empty" in {
        new ScriptEnvironment().getBindings shouldBe 'empty
      }
      "return the empty map on calls to getBindings()" in {
        new ScriptEnvironment().getBindings should equal(new util.HashMap[String, Object]())
      }
    }
    "initialized with the empty map" should {
      "be empty" in {
        new ScriptEnvironment(new util.HashMap[String, Object]()).getBindings shouldBe 'empty
      }
      "return the empty map on calls to getBindings()" in {
        new ScriptEnvironment(new util.HashMap[String, Object]()).getBindings should equal(new util.HashMap[String, Object]())
      }
    }
    "initialized with a non-empty map" should {
      "not be empty" in {
        forAll { (map: Map[String, List[AnyVal]]) =>
          whenever (map nonEmpty) { new ScriptEnvironment(map).getBindings should not be 'empty }
        }
      }
      "return that map on calls to getBindings()" in {
        forAll { (map: Map[String, List[AnyVal]]) =>
          whenever (map nonEmpty) { new ScriptEnvironment(map).getBindings.asScala should equal(map) }
        }
      }
    }
    "adding a binding" should {
      "add the new key to its bindings" in {
        forAll { (k: String, v: AnyVal) =>
          val target = new ScriptEnvironment()
          target.addBinding(k, v)
          target.getBindings.asScala should contain key k
        }
      }
      "add the new mapping to its bindings" in {
        forAll { (k: String, v: AnyVal) =>
          val target = new ScriptEnvironment()
          target.addBinding(k, v)
          target.getBindings.asScala should contain (k -> v)
        }
      }
      "bind the new variable binding in any ScriptContainers it is linked to" in {
        forAll { (k: String, v: AnyVal) =>
          val container = mock[ScriptContainer]
          val target = new ScriptEnvironment()

          target.link(container)
          target.addBinding(k, v)

          verify(container, times(1)).injectObject(k, v)
        }
      }
      "not bind the new binding in any ScriptContainers it has been unlinked from" in {
        forAll { (key1: String, value1: AnyVal, key2: String, value2: AnyVal) =>
          whenever (key1 != key2) { // no hash collisions pls kthnx
            val container = mock[ScriptContainer]
            val target = new ScriptEnvironment()

            target.link(container)
            target.addBinding(key1, value1)

            verify(container, times(1)).injectObject(key1, value1)

            target.unlink(container)
            target.addBinding(key2, value2)

            verify(container, times(1)).removeObject(key1)
            verifyNoMoreInteractions(container)
          }
        }
      }
    }
    "adding multiple bindings from a HashMap" should {
      "add all new keys to its bindings" in {
        forAll {
          // `List[AnyVal]` has been chosen as the type signature for a generic
          // Object by test engineer fiat (ScalaCheck knows how to make
          // arbitrary lists easily, arbitrary `AnyRef`s less so)
          (newBindings: Map[String, List[AnyVal]]) =>
            val target = new ScriptEnvironment
            target addBindings newBindings
            newBindings.keySet foreach (target.getBindings should contain key _)
        }
      }
      "add all new mappings to its bindings" in {
        forAll { (newBindings: Map[String, List[AnyVal]]) =>
          val target = new ScriptEnvironment
          target addBindings newBindings
          newBindings foreach (target.getBindings.asScala should contain(_))
        }
      }
      "bind the new variable bindings in any ScriptContainer it is linked to" in {
        forAll { (newBindings: Map[String, List[AnyVal]]) =>
          val container = mock[ScriptContainer]
          val target = new ScriptEnvironment()

          target.link(container)
          target.addBindings(newBindings)

          newBindings foreach { case ((k, v)) => verify(container, times(1)).injectObject(k, v) }
        }
        forAll { (newBindings: Map[String, String]) => // may as well try a couple more types
          val container = mock[ScriptContainer]
          val target = new ScriptEnvironment()

          target.link(container)
          target.addBindings(newBindings)

          newBindings foreach { case ((k, v)) => verify(container, times(1)).injectObject(k, v) }
        }
        forAll { (newBindings: Map[String, Map[String,Int]]) =>
          val container = mock[ScriptContainer]
          val target = new ScriptEnvironment()

          target.link(container)
          target.addBindings(newBindings)

          newBindings foreach { case ((k, v)) => verify(container, times(1)).injectObject(k, v) }
        }
      }
      "not bind the new bindings in any ScriptContainers it has been unlinked from" in {
        forAll { (newBindings1: Map[String, List[AnyVal]], newBindings2: Map[String, List[AnyVal]]) =>
          val container = mock[ScriptContainer]
          val target = new ScriptEnvironment()

          target.link(container)
          target.addBindings(newBindings1)

          newBindings1 foreach { case ((k, v)) => verify(container, times(1)).injectObject(k, v) }

          target.unlink(container)
          target.addBindings(newBindings2)
          newBindings1 foreach { case ((k, v)) => verify(container, times(1)).removeObject(k) }
          verifyNoMoreInteractions(container)
        }
      }
    }
    "linking with a ScriptContainer" should {
      "add all its existing bindings" in {
        forAll { (map: Map[String,List[AnyVal]]) =>
          val target = new ScriptEnvironment(map)
          val container = mock[ScriptContainer]

          target link container
          target.getBindings foreach {
            case ((k,v)) => verify(container,times(1)).injectObject(k,v)
          }
        }
      }
      "call the onLink script if one exists" in {
        forAll {
          (script: String, map: Map[String,List[AnyVal]]) =>
          val target = new ScriptEnvironment(map,script)
          val container = mock[ScriptContainer]

          target link container
          verify(container, times(1)).eval(script);
          target.getBindings foreach {
            case ((k,v)) => verify(container,times(1)).injectObject(k,v)
          }

        }
      }

    }
    "unlinking from a ScriptContainer" should {
      "remove all of its bindings from that ScriptContainer" in {
        forAll { (map: Map[String,List[AnyVal]]) =>
          val target = new ScriptEnvironment(map)
          val container = mock[ScriptContainer]

          target link container
          target unlink container

          target.getBindings foreach {
            case ((k,_)) => verify(container,times(1)).removeObject(k)
          }
        }
      }
      "not remove the bindings of another environment linked to that container" in {
        forAll { (map1: Map[String,List[AnyVal]], map2: Map[String,List[AnyVal]]) =>
          whenever ((map1.keySet & map2.keySet) isEmpty) {
            val target = new ScriptEnvironment(map1)
            val other = new ScriptEnvironment(map2)
            val container = mock[ScriptContainer]

            target link container
            other link container
            target unlink container

            other.getBindings foreach {
              case ((k,_)) => verify(container,never()).removeObject(k)
            }
          }
        }
      }
    }

  }
}
