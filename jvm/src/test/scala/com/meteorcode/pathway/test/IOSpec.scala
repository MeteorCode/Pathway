package com.meteorcode.pathway.test

import java.io._
import java.nio.charset.Charset
import java.nio.file.{FileSystems, Files}

import com.meteorcode.pathway.io.java_api.AlphabeticLoadPolicy
import com.meteorcode.pathway.io.scala_api.{ResourceManager, FilesystemFileHandle, FileHandle}
import com.meteorcode.pathway.test.tags.FilesystemTest

import org.mockito.Mockito._

import org.scalatest.{TryValues, OptionValues}
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec, BeforeAndAfter}

import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A COMPREHENSIVE UNIT TEST - io classes should be unit with mock objects as well
 *
 * Created by hawk on 5/30/15.
 */
class IOSpec extends PathwaySpec with OptionValues with TryValues {

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
        manager.handle("/test1.txt").success.value.extension shouldEqual "txt"
      }
      "have the correct name" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value.name shouldEqual "test1"
      }
      "not be a directory" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value should not be a ('directory)
      }
      "not be writable" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value should not be 'writable
      }
      "allow the contents to be read as a String" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .readString.success.value shouldEqual "hi!"
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .readString(Charset.defaultCharset()).success.value shouldEqual "hi!"
      }
      "not list any child drectories" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .list.success.value shouldBe empty
      }
      "return a Success(InputStream) from calls to read()" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .read.success.value shouldBe an [InputStream]
      }
      "return a Success(BufferedInputStream) from calls to read() with a buffer sized" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .read(8).success.value shouldBe a [BufferedInputStream]
      }
      "return None from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest  in {
        manager.handle("/test1.txt")
          .success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
    }
    "into a nonexistant file in the write directory" should {
      "contain the written string after a call to writeString()" taggedAs FilesystemTest in {
        val target = manager.handle("/write/test5.txt").success.value
        target.writeString("hello", append=false)
        target.readString.success.value shouldEqual "hello"
      }
      "return Some(OutputStream) from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/write/test5.txt").success.value
          .write(append=true).value shouldBe an [OutputStream]
      }
      "return Some(BufferedOutputStream) from calls to write() in append mode with a buffer size" taggedAs FilesystemTest in {
        manager.handle("/write/test5.txt").success.value
          .write(8,append=true).value shouldBe a [BufferedOutputStream]
      }
      "return Some(OutputStream) from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/write/test5.txt").success.value
          .write(append=false).value shouldBe an [OutputStream]
      }
      "return Some(BufferedOutputStream) from calls to write() in overwrite mode with a buffer size" taggedAs FilesystemTest in {
        manager.handle("/write/test5.txt").success.value
          .write(8,append=false).value shouldBe a [BufferedOutputStream]
      }
    }
    "into a nonexistant file outside of the write directory" should {
      "throw an IOException when instantiated" taggedAs FilesystemTest in {
        manager.handle("testDir/FILE THAT DOESN'T EXIST")
          .failure.exception should have message "A filehandle to an empty path (testDir/FILE THAT DOESN'T EXIST) was requested, and the requested path was not writable"
      }
    }
    "into an existant directory on the file system" should {
      "be a directory" taggedAs FilesystemTest in { manager.handle("/testDir").success.value should be a ('directory) }
      "not be writable" taggedAs FilesystemTest in { manager.handle("/testDir").success.value should not be 'writable }
      "not have an extension" taggedAs FilesystemTest in { manager.handle("/testDir").success.value.extension shouldEqual ""}
      "know its name" taggedAs FilesystemTest in {manager.handle("/testDir").success.value.name shouldEqual "testDir"}
      "return None from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "return a Failure(IOException)n from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "allow access into child files" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .child("test3.txt").success.value
          .readString.success.value shouldEqual "yet again hi"
        manager.handle("/testDir").success.value
          .child("test4.txt").success.value
          .readString.success.value shouldEqual "still hi"
      }
    }
    "into a file within a directory on the file system" should {
      "allow access to the parent" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .child("test3.txt").success.value
          .parent.success.value shouldEqual manager.handle("/testDir").success.value
      }
      "allow access to its siblings" taggedAs FilesystemTest in {
        manager.handle("/testDir").success.value
          .child("test3.txt").success.value
          .sibling("test4.txt").success.value shouldEqual manager.handle("/testDir/test4.txt").success.value
      }
    }
    "into a file in a Zip archive" should {
      "have the correct extension" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt").success.value.extension shouldEqual "txt"}
      "have the correct name" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt").success.value.name shouldEqual "zippedtest"}
      "not be a directory" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt").success.value should not be a ('directory)}
      "not be writable" taggedAs FilesystemTest in {manager.handle("/zippedtest.txt").success.value should not be 'writable}
      "allow the contents to be read as a String" in {
        manager.handle("/zippedtest.txt")
          .success.value
          .readString.success.value shouldEqual "also hi!"
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt")
          .success.value
          .readString(Charset.defaultCharset()).success.value shouldEqual "also hi!"
      }
      "not list any child items" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .list.success.value shouldBe empty
      }
      "return Some(InputStream) from calls to read()" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .read.success.value shouldBe an [InputStream]
      }
      "return Some(BufferedInputStream) from calls to read() with a buffer sized" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .read(8).success.value shouldBe a [BufferedInputStream]
      }
      "return None from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
    }
    "into a file in a Jar archive" should {
      "have the correct extension" taggedAs FilesystemTest in { manager.handle("/test6.txt").success.value.extension shouldEqual "txt" }
      "have the correct name" taggedAs FilesystemTest in { manager.handle("/test6.txt").success.value.name shouldEqual "test6" }
      "not be a directory" taggedAs FilesystemTest in { manager.handle("/test6.txt").success.value should not be a ('directory) }
      "not be writable" taggedAs FilesystemTest in { manager.handle("/test6.txt").success.value should not be 'writable }
      "allow the contents to be read as a String" taggedAs FilesystemTest in {
        manager.handle("/test6.txt")
          .success.value
          .readString.success.value shouldEqual "Continued hi."
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .readString(Charset.defaultCharset()).success.value shouldEqual "Continued hi."
      }
      "not list any child items" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .list.success.value shouldBe empty
      }
      "return Some(InputStream) from calls to read()" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .read.success.value shouldBe an [InputStream]
      }
      "return Some(BufferedInputStream) from calls to read() with a buffer sized" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .read(8).success.value shouldBe a [BufferedInputStream]
      }
      "return None from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/test6.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
    }
    "into a directory in a Jar archive" should {
      "be a directory" taggedAs FilesystemTest in {manager.handle("/testJarDir").success.value should be a ('directory) }
      "not be writable" taggedAs FilesystemTest in {manager.handle("/testJarDir").success.value should not be ('writable) }
      "not have an extension" taggedAs FilesystemTest in { manager.handle("/testJarDir").success.value.extension shouldEqual ""}
      "know its name" taggedAs FilesystemTest in {manager.handle("/testJarDir").success.value.name shouldEqual "testJarDir"}
      "return None from calls to write() in append mode" taggedAs FilesystemTest in {
        manager.handle("/testJarDir/").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "allow access into child files" taggedAs FilesystemTest in {
        manager.handle("/testJarDir").success.value
          .child("test7.md").success.value
          .readString.success.value shouldEqual "Hi continues."
      }
    }
    "permission is denied by the host OS" should {
      "not throw an exception" in {
        val fakeFile = mock[File]
        when(fakeFile.createNewFile).thenThrow(new IOException("Permission denied"))
        when(fakeFile.isDirectory).thenReturn(false)
        when(fakeFile.exists).thenReturn(false)

        new FilesystemFileHandle("/write/fakepath", "/write/fakepath", fakeFile, manager).writable shouldBe false

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
          new FilesystemFileHandle("/write/fakepath", "/write/fakepath", fakeFile, manager).writable
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
        val directories = Seq[FileHandle](
          new FilesystemFileHandle("", "build/resources/test/loadOrder/b", null),
          new FilesystemFileHandle("", "build/resources/test/loadOrder/a", null),
          new FilesystemFileHandle("", "build/resources/test/loadOrder/c", null)
        )

        val target = new ResourceManager(directories, order = new AlphabeticLoadPolicy())
        target.handle("/testLoadOrder.txt").success.value
          .readString.success.value shouldEqual "I AM CORRECT"
      }
    }
    "handling the same path multiple times" should {
      "return a cached FileHandle rather than a new one" taggedAs FilesystemTest in {
        manager.handle("/test1.txt").success.value shouldBe manager.handle("/test1.txt").success.value
      }
    }
  }
}
