package com.meteorcode.pathway.test

import com.meteorcode.pathway.logging.{NullLogger, LoggerFactory}

import me.hawkweisman.util._

import org.scalacheck.Gen
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec, BeforeAndAfter}

/**
 * Created by hawk on 5/30/15.
 */
abstract class PathwaySpec extends WordSpec with Matchers with PropertyChecks with MockitoSugar with BeforeAndAfter {
  // quash the obnoxious and unnecessary log messages during testing
  LoggerFactory setLogger new NullLogger

  before {
    System.gc()
  }
}

trait IdentGenerators {
  val random = new scala.util.Random

  // generates random Java identifiers
  val ident: Gen[String] = for {
    len  <- Gen.choose(1,500) // 500 seems reasonable
    name <- randomJavaIdent(len)(random)
  } yield name

  val invalidIdentStart: Gen[String] = for {
    start <- Gen.oneOf('1','2','3','4','5','6','7','8','9','-','+','*','?','\'','{','}',';',',')
    len  <- Gen.choose(1,500)
  } yield s"$start${randomJavaIdent(len)(random)}"

  val invalidAt: Gen[(Int,String)] = for {
    len <- Gen.choose(1,500)
    pos <- Gen.choose(0,len)
    invalid <- Gen.oneOf('-','+','*','?','\'','{','}',';',',', '/', '[',']','"','\\','|')
  } yield (pos, randomJavaIdent(len)(random).patch(pos, s"$invalid", 1))

  val spaceAt: Gen[(Int,String)] = for {
    len <- Gen.choose(1,500)
    pos <- Gen.choose(0,len)
  } yield (pos, randomJavaIdent(len)(random).patch(pos, " ", 1))

  val reservedWords: Gen[String] = Gen.oneOf("abstract", "assert", "boolean",
    "break", "byte", "case", "catch", "char", "class", "const",
    "continue", "do", "double", "else", "enum", "extends", "final",
    "finally", "float", "for", "if", "goto", "implements",
    "import", "instanceof", "int", "interface", "long", "native",
    "new", "package", "private", "protected", "public", "return",
    "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "try",
    "void", "volatile", "while")


}
