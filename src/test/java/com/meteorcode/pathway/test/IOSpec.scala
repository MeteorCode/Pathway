package com.meteorcode.pathway.test

import java.io.{BufferedInputStream, InputStream, OutputStream, BufferedOutputStream, File}
import java.nio.file.{FileSystems, Files}

import com.meteorcode.pathway.io.{AlphabeticLoadPolicy, ResourceManager}
import org.scalatest.BeforeAndAfter

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A COMPREHENSIVE UNIT TEST - io classes should be unit with mock objects as well
 *
 * Created by hawk on 5/30/15.
 */
class IOSpec extends PathwaySpec with BeforeAndAfter {
  var manager: ResourceManager = null

  before {
    manager = new ResourceManager("build/resources/test",
      "build/resources/test/write",
      new AlphabeticLoadPolicy)
    new File("build/resources/test/testDir/emptyTestDir").mkdir
  }

  after {
    // clean up the write directory so that it won't exist next time tests are run
    Files.deleteIfExists(FileSystems.getDefault.getPath("build/resources/test/write/test5.txt"))
    Files.deleteIfExists(FileSystems.getDefault.getPath("build/resources/test/write/"))

    // clean up the empty test dir
    Files.deleteIfExists(FileSystems.getDefault.getPath("build/resources/test/emptyTestDir/"))
  }

  "A FileHandle" when {
    "into a file that exists in the file system" should {
      "have the correct extension" in {
        manager.handle("/test1.txt").extension shouldEqual "txt"
      }
      "have the correct name" in {
        manager.handle("/test1.txt").name shouldEqual "test1"
      }
      "not be a directory" in {
        manager.handle("/test1.txt") should not be a ('directory)
      }
      "not be writable" in {
        manager.handle("/test1.txt") should not be 'writable
      }
      "allow the contents to be read as a String" in {
        manager.handle("/test1.txt").readString shouldEqual "hi!"
      }
      "not list any child drectories" in {
        manager.handle("/test1.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" in {
        manager.handle("/test1.txt").read shouldBe an [InputStream]
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" in {
        manager.handle("/test1.txt").read(8) shouldBe a [BufferedInputStream]
      }
    }
    "into a nonexistant file in the write directory" should {
      "contain the written string after a call to writeString()" in {
        val target = manager.handle("/write/test5.txt")
        target.writeString("hello", false)
        target.readString shouldEqual "hello"
      }
      "return an OutputStream from calls to write() in append mode" in {
        manager.handle("/write/test5.txt").write(true) shouldBe an [OutputStream]
      }
      "return a BufferedOutputStream from calls to write() in append mode with a buffer size" in {
        manager.handle("/write/test5.txt").write(8,true) shouldBe a [BufferedOutputStream]
      }
      "return an OutputStream from calls to write() not in append mode" in {
        manager.handle("/write/test5.txt").write(false) shouldBe an [OutputStream]
      }
      "return a BufferedOutputStream from calls to write() not in append mode with a buffer size" in {
        manager.handle("/write/test5.txt").write(8,false) shouldBe a [BufferedOutputStream]
      }
    }
  }

}
