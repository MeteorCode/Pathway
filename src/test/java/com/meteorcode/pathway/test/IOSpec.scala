package com.meteorcode.pathway.test

import java.io._
import java.nio.charset.Charset
import java.nio.file.{FileSystems, Files}

import com.meteorcode.pathway.io.{FileHandle, DesktopFileHandle, AlphabeticLoadPolicy, ResourceManager}
import com.meteorcode.pathway.logging.{NullLogger, LoggerFactory}
import com.meteorcode.pathway.test.tags.FilesystemTest


import org.mockito.Mockito._

import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec, BeforeAndAfter}

import scala.collection.JavaConversions._

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A COMPREHENSIVE UNIT TEST - io classes should be unit with mock objects as well
 *
 * Created by hawk on 5/30/15.
 */

class IOSpec extends PathwaySpec {

  var manager: ResourceManager = null

  override def beforeEach() {
    manager = new ResourceManager("build/resources/test",
      "build/resources/test/write",
      new AlphabeticLoadPolicy)
    new File("build/resources/test/testDir/emptyTestDir").mkdir
    super.beforeEach()
  }

  override def afterEach() {
    // clean up the write directory so that it won't exist next time tests are run
    Files.deleteIfExists(FileSystems.getDefault.getPath("build/resources/test/write/test5.txt"))
    Files.deleteIfExists(FileSystems.getDefault.getPath("build/resources/test/write/"))

    // clean up the empty test dir
    Files.deleteIfExists(FileSystems.getDefault.getPath("build/resources/test/emptyTestDir/"))
  }

  "A FileHandle" when {
    "into a file that exists in the file system" should {
      "have the correct extension" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").extension shouldEqual "txt"
      }
      "have the correct name" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").name shouldEqual "test1"
      }
      "not be a directory" taggedAs FilesystemTest in {
        manager.handle("/test1.txt") should not be a ('directory)
      }
      "not be writable" taggedAs FilesystemTest in {
        manager.handle("/test1.txt") should not be 'writable
      }
      "allow the contents to be read as a String" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").readString shouldEqual "hi!"
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").readString(Charset.defaultCharset()) shouldEqual "hi!"
      }
      "not list any child drectories" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" taggedAs FilesystemTest in {
        val result = manager.handle("/test1.txt").read
        result shouldBe an [InputStream]
        result should not be null
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" taggedAs FilesystemTest in {
        val result = manager.handle("/test1.txt").read(8)
        result shouldBe a [BufferedInputStream]
        result should not be null
      }
      "return null from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", append=true)
        } should have message "FileHandle /test1.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", append=false)
        } should have message "FileHandle /test1.txt is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest  in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /test1.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test1.txt").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /test1.txt is not writable."
      }
    }
    "into a nonexistant file in the write directory" should {
      "contain the written string after a call to writeString()" taggedAs FilesystemTest in {
        val target = manager.handle("/write/test5.txt")
        target.writeString("hello", false)
        target.readString shouldEqual "hello"
      }
      "return an OutputStream from calls to write() in append mode" taggedAs FilesystemTest in {
        val result = manager.handle("/write/test5.txt").write(append=true)
        result shouldBe an [OutputStream]
        result should not be null
      }
      "return a BufferedOutputStream from calls to write() in append mode with a buffer size" taggedAs FilesystemTest in {
        val result = manager.handle("/write/test5.txt").write(8,append=true)
        result shouldBe a [BufferedOutputStream]
        result should not be null
      }
      "return an OutputStream from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        val result = manager.handle("/write/test5.txt").write(append=false)
        result shouldBe an [OutputStream]
        result should not be null
      }
      "return a BufferedOutputStream from calls to write() in overwrite mode with a buffer size" taggedAs FilesystemTest in {
        val result = manager.handle("/write/test5.txt").write(8,append=false)
        result shouldBe a [BufferedOutputStream]
        result should not be null
      }
    }
    "into a nonexistant file outside of the write directory" should {
      "throw an IOException when instantiated" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("testDir/FILE THAT DOESN'T EXIST")
        } should have message "A filehandle to an empty path (testDir/FILE THAT DOESN'T EXIST) was requested, and the requested path was not writable"
        }
      }
    "into an existant directory on the file system" should {
      "be a directory" taggedAs FilesystemTest in { manager.handle("/testDir") should be a ('directory) }
      "not be writable" taggedAs FilesystemTest in { manager.handle("/testDir") should not be 'writable }
      "not have an extension" taggedAs FilesystemTest in { manager.handle("/testDir").extension shouldEqual ""}
      "know its name" taggedAs FilesystemTest in {manager.handle("/testDir").name shouldEqual "testDir"}
      "return null from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/testDir").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testDir").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/testDir").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testDir").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", append=true)
        } should have message "FileHandle /testDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", append=false)
        } should have message "FileHandle /testDir is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /testDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testDir").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /testDir is not writable."
      }
      "allow access into child files" taggedAs FilesystemTest in {
        manager.handle("/testDir").child("test3.txt").readString shouldEqual "yet again hi"
        manager.handle("/testDir").child("test4.txt").readString shouldEqual "still hi"
      }
    }
    "into a file within a directory on the file system" should {
      "allow access to the parent" taggedAs FilesystemTest in {
        manager.handle("/testDir").child("test3.txt").parent shouldEqual manager.handle("/testDir")
      }
      "allow access to its siblings" taggedAs FilesystemTest in {
        manager.handle("/testDir").child("test3.txt").sibling("test4.txt") shouldEqual manager.handle("/testDir/test4.txt")
      }
    }
    "into a file in a Zip archive" should {
      "have the correct extension" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt").extension shouldEqual "txt"}
      "have the correct name" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt").name shouldEqual "zippedtest"}
      "not be a directory" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt") should not be a ('directory)}
      "not be writable" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt") should not be 'writable}
      "allow the contents to be read as a String" in {
        manager.handle("/zippedtest.txt").readString shouldEqual "also hi!"
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").readString(Charset.defaultCharset()) shouldEqual "also hi!"
      }
      "not list any child items" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" taggedAs FilesystemTest in {
        val result = manager.handle("/zippedtest.txt").read
        result shouldBe an [InputStream]
        result should not be null
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" taggedAs FilesystemTest in {
        val result = manager.handle("/zippedtest.txt").read(8)
        result shouldBe a [BufferedInputStream]
        result should not be null
      }
      "return null from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", append=true)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", append=false)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/zippedtest.txt").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /zippedtest.txt is not writable."
      }
    }
    "into a file in a Jar archive" should {
      "have the correct extension" taggedAs FilesystemTest in { manager.handle("/test6.txt").extension shouldEqual "txt" }
      "have the correct name" taggedAs FilesystemTest in { manager.handle("/test6.txt").name shouldEqual "test6" }
      "not be a directory" taggedAs FilesystemTest in { manager.handle("/test6.txt") should not be a ('directory) }
      "not be writable" taggedAs FilesystemTest in { manager.handle("/test6.txt") should not be 'writable }
      "allow the contents to be read as a String" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").readString shouldEqual "Continued hi."
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").readString(Charset.defaultCharset()) shouldEqual "Continued hi."
      }
      "not list any child items" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").list shouldBe empty
      }
      "return an InputStream from calls to read()" taggedAs FilesystemTest in {
        val result = manager.handle("/test6.txt").read
        result shouldBe an [InputStream]
        result should not be null
      }
      "return a BufferedInputStream from calls to read() with a buffer sized" taggedAs FilesystemTest in {
        val result = manager.handle("/test6.txt").read(8)
        result shouldBe a [BufferedInputStream]
        result should not be null
      }
      "return null from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").write(append=true) shouldBe null
      }
      "return null from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").write(8,append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", append=true)
        } should have message "FileHandle /test6.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", append=false)
        } should have message "FileHandle /test6.txt is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /test6.txt is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/test6.txt").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /test6.txt is not writable."
      }
    }
    "into a directory in a Jar archive" should {
      "be a directory" taggedAs FilesystemTest in {manager.handle("/testJarDir") should be a ('directory) }
      "not be writable" taggedAs FilesystemTest in {manager.handle("/testJarDir") should not be ('writable) }
      "not have an extension" taggedAs FilesystemTest in { manager.handle("/testJarDir").extension shouldEqual ""}
      "know its name" taggedAs FilesystemTest in {manager.handle("/testJarDir").name shouldEqual "testJarDir"}
      "return null from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/testJarDir/").write(append=true) shouldBe null
      }
      "return null from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").write(append=false) shouldBe null
      }
      "return null from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").write(8,append=false) shouldBe null
      }
      "throw an IOException from calls to writeString() in append mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", append=true)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", append=false)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "throw an IOException from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", Charset.defaultCharset(),append=true)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "throw an IOException from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        the [IOException] thrownBy {
          manager.handle("/testJarDir").writeString("hi", Charset.defaultCharset(),append=false)
        } should have message "FileHandle /testJarDir is not writable."
      }
      "allow access into child files" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").child("test7.md").readString shouldEqual "Hi continues."
      }
    }
    "permission is denied by the host OS" should {
      "not throw an exception" in {
        val fakeFile = mock[File]
        when(fakeFile.createNewFile).thenThrow(new IOException("Permission denied"))
        when(fakeFile.isDirectory).thenReturn(false)
        when(fakeFile.exists).thenReturn(false)

        new DesktopFileHandle("/write/fakepath", "/write/fakepath", fakeFile, manager).writable shouldBe false

        verify(fakeFile, times(1)).isDirectory
        verify(fakeFile, times(1)).exists
        verify(fakeFile, times(1)).createNewFile

      }
    }
    "other exceptions are thrown by the host OS" should {
      "pass through the exception" in {
        val fakeFile = mock[File]
        when(fakeFile.createNewFile).thenThrow(new IOException("SOMETHING BAD TOOK PLACE I GUESS"))
        when(fakeFile.isDirectory).thenReturn(false)
        when(fakeFile.exists).thenReturn(false)

        the [IOException] thrownBy {
          new DesktopFileHandle("/write/fakepath", "/write/fakepath", fakeFile, manager).writable
        } should have message "SOMETHING BAD TOOK PLACE I GUESS"

        verify(fakeFile, times(1)).isDirectory
        verify(fakeFile, times(1)).exists
        verify(fakeFile, times(1)).createNewFile
      }
    }
  }

  "A ResourceManager" when {
    "ordering paths alphabetically" should {
      "apply the directories in alphabetical order" taggedAs FilesystemTest in {
        val directories = List[FileHandle](
          new DesktopFileHandle("", "build/resources/test/loadOrder/b", null),
          new DesktopFileHandle("", "build/resources/test/loadOrder/a", null),
          new DesktopFileHandle("", "build/resources/test/loadOrder/c", null)
        )

        val target = new ResourceManager(directories, new AlphabeticLoadPolicy())
        target.handle("/testLoadOrder.txt").readString shouldEqual "I AM CORRECT"
      }
    }
    "handling the same path multiple times" should {
      "return a cached FileHandle rather than a new one" taggedAs FilesystemTest in {
        manager.handle("/test1.txt") shouldBe manager.handle("/test1.txt")
      }
    }
  }
}
