package com.meteorcode.pathway.test

import com.meteorcode.pathway.logging.{NullLogger, LoggerFactory, ConcurrentCache}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}
import org.scalacheck.{Gen, Arbitrary}
import scala.collection.JavaConversions._

/**
 * Created by hawk on 5/8/15.
 */
@RunWith(classOf[JUnitRunner])
class ConcurrentCacheSpec extends PathwaySpec {

  val reasonableSizes = for (n <- Gen.choose(1, 10000)) yield n
  val listsAndSizes = for {
    l <- Arbitrary.arbitrary[Seq[Int]]
    s <- Gen.choose(0, l.size)
  } yield (l, s)

  "A ConcurrentCache" when {
    "instantiated with the default constructor" should {
      "not be null" in { new ConcurrentCache[String] should not be null }
    }
    "instantiated with the sized constructor" should {
      "throw an exception when size is less < 1" in {
        forAll { (i: Int) => whenever (i < 1) {
            the [IllegalArgumentException] thrownBy {
              new ConcurrentCache[String](-1)
            } should have message "Buffer size must be greater than 1."
          }
        }
      }
      "have the requested size when size is >= 1" in {
        forAll (reasonableSizes) { (i: Int) => whenever (
          i > 0 && i < 10000 //keep us from hitting JVM array size limit
        ) {
          new ConcurrentCache[String](i).getSize should equal (i)
          }
        }
      }
    }
    "of size n" should {
      "unwind to the last n items inserted" in {
        forAll (listsAndSizes) { case ((ints: Seq[Int], cacheSize: Int)) =>
          whenever(ints.length > 1 && cacheSize <= ints.length && cacheSize > 0) {
            val cache = new ConcurrentCache[Int](cacheSize)
            val expected = (ints slice(ints.length - math.min(ints.length, cacheSize), ints.length))
            ints.foreach(cache insert)
            val unwound: Seq[Int] = cache.unwind()
            unwound.toVector shouldEqual expected.reverse
          }
        }
      }
    }
  }

}
