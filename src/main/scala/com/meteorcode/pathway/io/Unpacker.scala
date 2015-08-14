package com.meteorcode.pathway.io

import java.io.IOException
import java.net.{URI, URL}
import java.nio
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging

import me.hawkweisman.util.TryWithFold

import scala.collection.JavaConverters._
import scala.util.{Success, Failure, Try}
import scala.util.control.NonFatal

/**
 * Unpacker is a utility class capable of unpacking
 * resources included in a Jar file into the local filesystem
 * near the Jar when it is run. This is useful for, e.g., unpacking
 * Native dependencies such as LWJGL's natives before running code
 * that requires it.
 */
object Unpacker
extends LazyLogging {
  private[this] val sep = System.getProperty("file.separator")

  private[this] lazy val defaultLocation: String
    = System.getProperty("user.home") + s"$sep.pathway$sep"

  private[this] lazy val defaultSrcURL: URL
    = this.getClass
          .getProtectionDomain
          .getCodeSource
          .getLocation

  /**
   * Attempts to unpack native JARs into the host filesystem, because JNI
   * requires this.
   * @return True if and only if the unpacking succeeds, and the classpath
   *         is edited to include the new natives directory. False otherwise.
   */
  def unpackNatives(destLocation: String = defaultLocation,
                    srcURL: URL = defaultSrcURL): Try[Boolean] = {
    logger info s"Unpacking natives from $destLocation to $srcURL"
    val targetDir = Paths.get(destLocation)

    //We cannot unpack if the target location is read only.
    if(!targetDir.getFileSystem.isReadOnly) {

      //Add destLocation/native to the classloader via an ugly hack
      UnpackerJavaCallouts.mangleClassloader(
        Paths.get(destLocation)
          .resolve("native")
          .toString
      )

      val zURI = URI.create("jar:" + srcURL.toURI.toString + "!/lwjgl-natives")
      Try{
        Files.createDirectory(targetDir)
        logger info s"Created local natives directory $targetDir"
      } recover {
          case x: FileAlreadyExistsException =>
            logger debug s"Local natives directory $targetDir already exists"
      } flatMap { _ =>
        Try(FileSystems.newFileSystem(zURI, Map("create" -> "false").asJava))
          .recover {
            case x: FileSystemAlreadyExistsException =>
          }
      } flatMap { _ =>
        val top = Paths.get(zURI)
        Try(Files.walkFileTree(top,
          new FileVisitor[nio.file.Path] with LazyLogging {
          import FileVisitResult._

          override def preVisitDirectory(dir: nio.file.Path,
                                         attrs: BasicFileAttributes): FileVisitResult
            = { if(dir.toString.compareTo(top.toString) != 0) {
                  try {
                    Files.createDirectory(
                      targetDir.resolve(top.relativize(dir).toString)
                    )
                  } catch {
                    case x: FileAlreadyExistsException =>
                    case x if NonFatal(x) =>
                      logger warn (s"An exception occurred before visiting $dir", x)
                  }
                }
                  CONTINUE
              }

          override def visitFileFailed(file: nio.file.Path,
                                       exc: IOException): FileVisitResult
            = CONTINUE

          override def postVisitDirectory(dir: nio.file.Path,
                                          exc: IOException): FileVisitResult
            = CONTINUE

          override def visitFile(file: nio.file.Path,
                                 attrs: BasicFileAttributes): FileVisitResult
            = Try {
                Files.copy(
                  Files.newInputStream(file),
                  targetDir.resolve(top.relativize(file).toString)
                )
                logger trace s"Copied $file to natives directory"
              } recover {
                case x: FileAlreadyExistsException =>
                  logger trace s"Natives file $file already exists, skipping"
                case x if NonFatal(x) =>
                  logger warn (s"Exception while visiting $file!", x)
              } fold (
                up => throw up,
                _  => CONTINUE
              )
        })
        ) map { _ => true }
      }

    } else {
      // the target location was read-only
      logger warn s"Natives location $targetDir was read-only!"
      Success(false)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // REMOVE BEFORE FLIGHT -- DO NOT RUN THIS IN PRODUCTION BUILDS PLEASE
  // Unpacker test method
  def main (args: Array[String]): Unit
    = println(unpackNatives() match {
        case Success(true)  => s"Natives unpacked to $defaultLocation"
        case Success(false) => s"Natives not unpacked, target was read-only"
        case Failure(why)   => s"Something went wrong!\n$why"
      })
  // REMOVE BEFORE FLIGHT
  /////////////////////////////////////////////////////////////////////////////
}
