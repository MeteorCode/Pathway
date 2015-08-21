package com.meteorcode.pathway.test

import javax.script.ScriptEngineManager

import com.meteorcode.pathway.script.ScriptMonad
import jdk.nashorn.api.scripting.NashornScriptEngineFactory

import org.scalameter.api._

/**
 * Performance tests for benchmarking Nashorn JavaScript performance.
 *
 * Created by hawk on 8/20/15.
 */
object NashornBenchmarks
extends PerformanceTest.Quickbenchmark {

  val nothing = Gen.unit("unit")
  val nashornFactory = new NashornScriptEngineFactory
  val manager = new ScriptEngineManager

  performance of "Nashorn" in {
    measure method "NashornScriptEngineFactory spin-up" in {
      using(nothing) in { _ ⇒
        val engine = nashornFactory.getScriptEngine
        engine eval ""
      }
    }

    measure method "ScriptEngineManager spin-up" in {
      using(nothing) in { _ ⇒
        val engine = manager.getEngineByName("nashorn")
        engine eval ""
      }
    }
  }

  performance of "ScriptMonad" in {
    measure method "spin-up and apply" in {
      using (nothing) in { _ ⇒
        ScriptMonad() apply ""
      }
    }
  }
}
