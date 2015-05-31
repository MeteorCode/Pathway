package com.meteorcode.pathway.test

import java.io._
import java.nio.charset.Charset
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
      "allow the contents to be read as a String with a specified charset" in {
        manager.handle("/test1.txt").readString(Charset.defaultCharset()) shouldEqual "hi!"
      }
      "not list any child drectories" in {
        manager.handle("/test1.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" in {
        val result = manager.handle("/test1.txt").read
        result shouldBe an [InputStream]
        result should not be null
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" in {
        val result = manager.handle("/test1.txt").read(8)
        result shouldBe a [BufferedInputStream]
        result should not be null
      }
      "return null from calls to write() in append mode" in {
        manager.handle("/test1.txt").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" in {
        manager.handle("/test1.txt").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" in {
        manager.handle("/test1.txt").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" in {
        manager.handle("/test1.txt").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", append=true)
        } should have message "FileHandle /test1.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", append=false)
        } should have message "FileHandle /test1.txt is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /test1.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /test1.txt is not writable."
      }
    }
    "into a nonexistant file in the write directory" should {
      "contain the written string after a call to writeString()" in {
        val target = manager.handle("/write/test5.txt")
        target.writeString("hello", false)
        target.readString shouldEqual "hello"
      }
      "return an OutputStream from calls to write() in append mode" in {
        val result = manager.handle("/write/test5.txt").write(append=true)
        result shouldBe an [OutputStream]
        result should not be null
      }
      "return a BufferedOutputStream from calls to write() in append mode with a buffer size" in {
        val result = manager.handle("/write/test5.txt").write(8,append=true)
        result shouldBe a [BufferedOutputStream]
        result should not be null
      }
      "return an OutputStream from calls to write() in overwrite mode" in {
        val result = manager.handle("/write/test5.txt").write(append=false)
        result shouldBe an [OutputStream]
        result should not be null
      }
      "return a BufferedOutputStream from calls to write() in overwrite mode with a buffer size" in {
        val result = manager.handle("/write/test5.txt").write(8,append=false)
        result shouldBe a [BufferedOutputStream]
        result should not be null
      }
    }
    "into a nonexistant file outside of the write directory" should {
      "throw an IOException when instantiated" in {
        the [IOException] thrownBy {
          manager.handle("testDir/FILE THAT DOESN'T EXIST")
        } should have message "A filehandle to an empty path (testDir/FILE THAT DOESN'T EXIST) was requested, and the requested path was not writable"
        }
      }
    "into an existant directory on the file system" should {
      "be a directory" in { manager.handle("/testDir") should be a ('directory) }
      "not be writable" in { manager.handle("/testDir") should not be 'writable }
      "not have an extension" in { manager.handle("/testDir").extension shouldEqual ""}
      "know its name" in {manager.handle("/testDir").name shouldEqual "testDir"}
      "return null from calls to write() in append mode" in {
        manager.handle("/testDir").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" in {
        manager.handle("/testDir").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" in {
        manager.handle("/testDir").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" in {
        manager.handle("/testDir").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", append=true)
        } should have message "FileHandle /testDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", append=false)
        } should have message "FileHandle /testDir is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /testDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /testDir is not writable."
      }
      "allow access into child files" in {
        manager.handle("/testDir").child("test3.txt").readString shouldEqual "yet again hi"
        manager.handle("/testDir").child("test4.txt").readString shouldEqual "still hi"
      }
    }
    "into a file within a directory on the file system" should {
      "allow access to the parent" in {
        manager.handle("/testDir").child("test3.txt").parent shouldEqual manager.handle("/testDir")
      }
      "allow access to its siblings" in {
        manager.handle("/testDir").child("test3.txt").sibling("test4.txt") shouldEqual manager.handle("/testDir/test4.txt")
      }
    }
    "into a file in a Zip archive" should {
      "have the correct extension" in {manager.handle("/zippedtest.txt").extension shouldEqual "txt"}
      "have the correct name" in {manager.handle("/zippedtest.txt").name shouldEqual "zippedtest"}
      "not be a directory" in {manager.handle("/zippedtest.txt") should not be a ('directory)}
      "not be writable" in {manager.handle("/zippedtest.txt") should not be 'writable}
      "allow the contents to be read as a String" in {
        manager.handle("/zippedtest.txt").readString shouldEqual "also hi!"
      }
      "allow the contents to be read as a String with a specified charset" in {
        manager.handle("/zippedtest.txt").readString(Charset.defaultCharset()) shouldEqual "also hi!"
      }
      "not list any child items" in {
        manager.handle("/zippedtest.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" in {
        val result = manager.handle("/zippedtest.txt").read
        result shouldBe an [InputStream]
        result should not be null
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" in {
        val result = manager.handle("/zippedtest.txt").read(8)
        result shouldBe a [BufferedInputStream]
        result should not be null
      }
      "return null from calls to write() in append mode" in {
        manager.handle("/zippedtest.txt").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" in {
        manager.handle("/zippedtest.txt").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" in {
        manager.handle("/zippedtest.txt").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" in {
        manager.handle("/zippedtest.txt").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", append=true)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", append=false)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
    }
    "into a file in a Jar archive" should {
      "have the correct extension" in { manager.handle("/test6.txt").extension shouldEqual "txt" }
      "have the correct name" in { manager.handle("/test6.txt").name shouldEqual "test6" }
      "not be a directory" in { manager.handle("/test6.txt") should not be a ('directory) }
      "not be writable" in { manager.handle("/test6.txt") should not be 'writable }
      "allow the contents to be read as a String" in {
        manager.handle("/test6.txt").readString shouldEqual "Continued hi."
      }
      "allow the contents to be read as a String with a specified charset" in {
        manager.handle("/test6.txt").readString(Charset.defaultCharset()) shouldEqual "Continued hi."
      }
      "not list any child items" in {
        manager.handle("/test6.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" in {
        val result = manager.handle("/test6.txt").read
        result shouldBe an [InputStream]
        result should not be null
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" in {
        val result = manager.handle("/test6.txt").read(8)
        result shouldBe a [BufferedInputStream]
        result should not be null
      }
      "return null from calls to write() in append mode" in {
        manager.handle("/test6.txt").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" in {
        manager.handle("/test6.txt").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" in {
        manager.handle("/test6.txt").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" in {
        manager.handle("/test6.txt").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", append=true)
        } should have message "FileHandle /test6.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", append=false)
        } should have message "FileHandle /test6.txt is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /test6.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /test6.txt is not writable."
      }
    }
    "into a directory in a Jar archive" should {
      "be a directory" in {manager.handle("/testJarDir") should be a ('directory) }
      "not be writable" in {manager.handle("/testJarDir") should not be ('writable) }
      "not have an extension" in { manager.handle("/testJarDir").extension shouldEqual ""}
      "know its name" in {manager.handle("/testJarDir").name shouldEqual "testJarDir"}
      "return null from calls to write() in append mode" in {
        manager.handle("/testJarDir/").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" in {
        manager.handle("/testJarDir").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" in {
        manager.handle("/testJarDir").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" in {
        manager.handle("/testJarDir").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", append=true)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", append=false)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "allow access into child files" in {
        manager.handle("/testJarDir").child("test7.md").readString shouldEqual "Hi continues"
      }
    }
  }
}
