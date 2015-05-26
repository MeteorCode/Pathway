package com.meteorcode.pathway.test


import java.io.IOException

import bsh.{InterpreterError, EvalError, Interpreter}
import com.meteorcode.pathway.io.FileHandle

import com.meteorcode.pathway.script.{ScriptException, ScriptContainerFactory, ScriptEnvironment}

import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}

import org.mockito.Mockito._

/**
 * Created by hawk on 5/25/15.
 */
class ScriptSpec extends WordSpec with Matchers with PropertyChecks with MockitoSugar {

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
      "pass through the result provided by the interpreter" in {

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
          whenever (script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)

            when(fakeInterpreter.eval(script)) thenThrow new EvalError(null,null,null)

            val target = fakeFactory.getNewInstance

            val e = the [ScriptException] thrownBy target.eval(script)
            e.getCause shouldBe an [EvalError]
          }
        }
      }
      "throw a ScriptException if the Interpreter throws an InterpreterError" in {
        forAll { (script: String) =>
          whenever (script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)

            when(fakeInterpreter.eval(script)) thenThrow new InterpreterError("fake interp. error")

            val target = fakeFactory.getNewInstance

            val e = the [ScriptException] thrownBy target.eval(script)
            e.getCause shouldBe an [InterpreterError]
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

            val target  = fakeFactory.getNewInstance

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

        an [IOException] should be thrownBy target.eval(fakeFileHandle)
      }
      "throw a ScriptException if the Interpreter throws an EvalError" in {
        forAll { (script: String) =>
          whenever (script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
            val fakeFileHandle = mock[FileHandle]

            when(fakeFileHandle.readString) thenReturn script
            when(fakeInterpreter.eval(script)) thenThrow new EvalError(null,null,null)

            val target = fakeFactory.getNewInstance

            val e = the [ScriptException] thrownBy target.eval(fakeFileHandle)
            e.getCause shouldBe an [EvalError]
          }
        }
      }
      "throw a ScriptException if the Interpreter throws an InterpreterError" in {
        forAll { (script: String) =>
          whenever (script != "") {
            val fakeInterpreter = mock[Interpreter]
            val fakeFactory = new ScriptContainerFactory(fakeInterpreter)
            val fakeFileHandle = mock[FileHandle]

            when(fakeFileHandle.readString) thenReturn script
            when(fakeInterpreter.eval(script)) thenThrow new InterpreterError("fake interp. error")

            val target = fakeFactory.getNewInstance

            val e = the [ScriptException] thrownBy target.eval(fakeFileHandle)
            e.getCause shouldBe an [InterpreterError]
          }
        }
      }

    }
  }
}
