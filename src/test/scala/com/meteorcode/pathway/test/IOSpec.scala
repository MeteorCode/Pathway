package com.meteorcode.pathway.test

import java.io._
import java.nio.charset.Charset
import java.nio.file.{FileSystems, Files}

import com.meteorcode.pathway.io._
import com.meteorcode.pathway.test.tags.FilesystemTest

import org.mockito.Mockito._

import org.scalatest.{TryValues, OptionValues}
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec, BeforeAndAfter}


/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A COMPREHENSIVE UNIT TEST - io classes should be unit with mock objects as well
 *
 * Created by hawk on 5/30/15.
 */
class IOSpec
extends WordSpec
  with Matchers
  with MockitoSugar
  with PropertyChecks
  with OptionValues
  with TryValues
  with BeforeAndAfter {


  trait Manager {
    var manager: ResourceManager = new ResourceManager(
      "./target/scala-2.11/test-classes/test-filesystem",
      "./target/scala-2.11/test-classes/test-filesystem/write",
      LoadPolicies.alphabetic)
    new File("./target/scala-2.11/test-classes/emptyTestDir").mkdir
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
      "have the correct extension" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value.extension shouldEqual "txt"
      }
      "have the correct name" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value.name shouldEqual "test1"
      }
      "not be a directory" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value should not be a ('directory)
      }
      "not be writable" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value should not be 'writable
      }
      "allow the contents to be read as a String" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .readString.success.value shouldEqual "hi!"
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .readString(Charset.defaultCharset()).success.value shouldEqual "hi!"
      }
      "not list any child drectories" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .list.success.value shouldBe empty
      }
      "return a Success(InputStream) from calls to read()" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .read.success.value shouldBe an [InputStream]
      }
      "return a Success(BufferedInputStream) from calls to read() with a buffer sized" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .read(8).success.value shouldBe a [BufferedInputStream]
      }
      "return None from calls to write() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest  in new Manager {
        manager.handle("/test1.txt")
          .success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /test1.txt is not writable."
      }
    }
    "into a nonexistant file in the write directory" should {
      "contain the written string after a call to writeString()" taggedAs FilesystemTest in new Manager {
        val target = manager.handle("/write/test5.txt").success.value
        target.writeString("hello", append=false)
        target.readString.success.value shouldEqual "hello"
      }
      "return Some(OutputStream) from calls to write() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/write/test5.txt").success.value
          .write(append=true).value shouldBe an [OutputStream]
      }
      "return Some(BufferedOutputStream) from calls to write() in append mode with a buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/write/test5.txt").success.value
          .write(8,append=true).value shouldBe a [BufferedOutputStream]
      }
      "return Some(OutputStream) from calls to write() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/write/test5.txt").success.value
          .write(append=false).value shouldBe an [OutputStream]
      }
      "return Some(BufferedOutputStream) from calls to write() in overwrite mode with a buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/write/test5.txt").success.value
          .write(8,append=false).value shouldBe a [BufferedOutputStream]
      }
    }
    "into a nonexistant file outside of the write directory" should {
      "throw an IOException when instantiated" taggedAs FilesystemTest in new Manager {
        manager.handle("testDir/FILE THAT DOESN'T EXIST")
          .failure.exception should have message "A filehandle to an empty path (testDir/FILE THAT DOESN'T EXIST) was requested, and the requested path was not writable"
      }
    }
    "into an existant directory on the file system" should {
      "be a directory" taggedAs FilesystemTest in new Manager { manager.handle("/testDir").success.value should be a ('directory) }
      "not be writable" taggedAs FilesystemTest in new Manager { manager.handle("/testDir").success.value should not be 'writable }
      "not have an extension" taggedAs FilesystemTest in new Manager { manager.handle("/testDir").success.value.extension shouldEqual ""}
      "know its name" taggedAs FilesystemTest in new Manager {manager.handle("/testDir").success.value.name shouldEqual "testDir"}
      "return None from calls to write() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "return a Failure(IOException)n from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /testDir is not writable."
      }
      "allow access into child files" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .child("test3.txt").success.value
          .readString.success.value shouldEqual "yet again hi"
        manager.handle("/testDir").success.value
          .child("test4.txt").success.value
          .readString.success.value shouldEqual "still hi"
      }
    }
    "into a file within a directory on the file system" should {
      "allow access to the parent" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .child("test3.txt").success.value
          .parent.success.value shouldEqual manager.handle("/testDir").success.value
      }
      "allow access to its siblings" taggedAs FilesystemTest in new Manager {
        manager.handle("/testDir").success.value
          .child("test3.txt").success.value
          .sibling("test4.txt").success.value shouldEqual manager.handle("/testDir/test4.txt").success.value
      }
    }
    "into a file in a Zip archive" should {
      "have the correct extension" taggedAs FilesystemTest in new Manager {manager.handle("/zippedtest.txt").success.value.extension shouldEqual "txt"}
      "have the correct name" taggedAs FilesystemTest in new Manager {manager.handle("/zippedtest.txt").success.value.name shouldEqual "zippedtest"}
      "not be a directory" taggedAs FilesystemTest in new Manager {manager.handle("/zippedtest.txt").success.value should not be a ('directory)}
      "not be writable" taggedAs FilesystemTest in new Manager {manager.handle("/zippedtest.txt").success.value should not be 'writable}
      "allow the contents to be read as a String" in new Manager {
        manager.handle("/zippedtest.txt")
          .success.value
          .readString.success.value shouldEqual "also hi!"
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt")
          .success.value
          .readString(Charset.defaultCharset()).success.value shouldEqual "also hi!"
      }
      "not list any child items" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .list.success.value shouldBe empty
      }
      "return Some(InputStream) from calls to read()" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .read.success.value shouldBe an [InputStream]
      }
      "return Some(BufferedInputStream) from calls to read() with a buffer sized" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .read(8).success.value shouldBe a [BufferedInputStream]
      }
      "return None from calls to write() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/zippedtest.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /zippedtest.txt is not writable."
      }
    }
    "into a file in a Jar archive" should {
      "have the correct extension" taggedAs FilesystemTest in new Manager { manager.handle("/test6.txt").success.value.extension shouldEqual "txt" }
      "have the correct name" taggedAs FilesystemTest in new Manager { manager.handle("/test6.txt").success.value.name shouldEqual "test6" }
      "not be a directory" taggedAs FilesystemTest in new Manager { manager.handle("/test6.txt").success.value should not be a ('directory) }
      "not be writable" taggedAs FilesystemTest in new Manager { manager.handle("/test6.txt").success.value should not be 'writable }
      "allow the contents to be read as a String" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt")
          .success.value
          .readString.success.value shouldEqual "Continued hi."
      }
      "allow the contents to be read as a String with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .readString(Charset.defaultCharset()).success.value shouldEqual "Continued hi."
      }
      "not list any child items" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .list.success.value shouldBe empty
      }
      "return Some(InputStream) from calls to read()" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .read.success.value shouldBe an [InputStream]
      }
      "return Some(BufferedInputStream) from calls to read() with a buffer sized" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .read(8).success.value shouldBe a [BufferedInputStream]
      }
      "return None from calls to write() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/test6.txt").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /test6.txt is not writable."
      }
    }
    "into a directory in a Jar archive" should {
      "be a directory" taggedAs FilesystemTest in new Manager {manager.handle("/testJarDir").success.value should be a ('directory) }
      "not be writable" taggedAs FilesystemTest in new Manager {manager.handle("/testJarDir").success.value should not be ('writable) }
      "not have an extension" taggedAs FilesystemTest in new Manager { manager.handle("/testJarDir").success.value.extension shouldEqual ""}
      "know its name" taggedAs FilesystemTest in new Manager {manager.handle("/testJarDir").success.value.name shouldEqual "testJarDir"}
      "return None from calls to write() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir/").success.value
          .write(append=true) shouldBe None
      }
      "return None from calls to write() in append mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .write(8,append=true) shouldBe None
      }
      "return None from calls to write() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .write(append=false) shouldBe None
      }
      "return None from calls to write() in overwrite mode with a specified buffer size" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .write(8,append=false) shouldBe None
      }
      "return a Failure(IOException) from calls to writeString() in append mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .writeString("hi", append=true)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .writeString("hi", append=false)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in append mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=true)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "return a Failure(IOException) from calls to writeString() in overwrite mode with a specified charset" taggedAs FilesystemTest in new Manager {
        manager.handle("/testJarDir").success.value
          .writeString("hi", Charset.defaultCharset(),append=false)
          .failure.exception should have message "FileHandle /testJarDir is not writable."
      }
      "allow access into child files" taggedAs FilesystemTest in new Manager {
        val child = manager.handle("/testJarDir").success.value
          .child("test7.md")
        info(child.toString)
        child.success.value
          .readString.success.value shouldEqual "Hi continues."
      }
    }
    "permission is denied by the host OS" should {
      "not throw an exception" in new Manager {
        val fakeFile = mock[File]
        when(fakeFile.createNewFile).thenThrow(new IOException("Permission denied"))
        when(fakeFile.isDirectory).thenReturn(false)
        when(fakeFile.canWrite).thenReturn(false)

        new FilesystemFileHandle("/write/fakepath", "/write/fakepath", fakeFile, Some(manager)).writable shouldBe false

        verify(fakeFile, times(1)).isDirectory
        verify(fakeFile, times(1)).canWrite
        verify(fakeFile, times(1)).createNewFile

      }
    }
    "other exceptions are thrown by the host OS" should {
      "pass through the exception" in new Manager {
        val fakeFile = mock[File]
        when(fakeFile.createNewFile).thenThrow(new IOException("SOMETHING BAD TOOK PLACE I GUESS"))
        when(fakeFile.isDirectory).thenReturn(false)
        when(fakeFile.exists).thenReturn(false)

        val exception = the [IOException] thrownBy {
          new FilesystemFileHandle("/write/fakepath", "/write/fakepath", fakeFile, Some(manager)).writable
        }
        exception should have message
          "Could not create FileHandle FilesystemFileHandle: /write/fakepath, an exception occured."
        exception.getCause should have message "SOMETHING BAD TOOK PLACE I GUESS"

        verify(fakeFile, times(1)).isDirectory
        verify(fakeFile, times(1)).canWrite
        verify(fakeFile, times(1)).createNewFile
      }
    }
  }

  "A ResourceManager" when {
    "ordering paths alphabetically" should {
      "apply the directories in alphabetical order" taggedAs FilesystemTest in {
        val directories = Seq[FileHandle](
          new FilesystemFileHandle(""
            , "./target/scala-2.11/test-classes/test-filesystem/loadOrder/a"
            , None),
          new FilesystemFileHandle(""
            , "./target/scala-2.11/test-classes/test-filesystem/loadOrder/b"
            , None),
          new FilesystemFileHandle(""
            , "./target/scala-2.11/test-classes/test-filesystem/loadOrder/c"
            , None)
        )

        val target = new ResourceManager(directories, order = LoadPolicies.alphabetic)
        target.handle("/testLoadOrder.txt").success.value
          .readString.success.value shouldEqual "I AM CORRECT"
      }
    }
    "handling the same path multiple times" should {
      "return a cached FileHandle rather than a new one" taggedAs FilesystemTest in new Manager {
        manager.handle("/test1.txt").success.value shouldBe manager.handle("/test1.txt").success.value
      }
    }
  }
}
