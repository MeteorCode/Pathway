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

  private[this] lazy val defaultLocation: nio.file.Path
    = Paths.get(System.getProperty("user.home"))
           .resolve(".pathway")

  private[this] lazy val defaultSrcURL: URL
    = this.getClass
          .getProtectionDomain
          .getCodeSource
          .getLocation

  private[this] val nativeExt: Option[String]
    = System.getProperty("os.name") match {
        case os if os contains "Mac" =>
          logger info s"Operating system is $os, unpacking .dylib libraries"
          Some("dylib")
        case os if os contains "Win" =>
          logger info s"Operating system is $os, unpacking .dll libraries"
          Some("dll")
        case os if os.contains("nix") || os.contains("nux") =>
          logger info s"Operating system is $os, unpacking .so libraries"
          Some("so")
        case os =>
          logger warn s"Unknown operating system $os, will unpack all natives"
          None
      }


  /**
   * Attempts to unpack native JARs into the host filesystem, because JNI
   * requires this.
   * @return True if and only if the unpacking succeeds, and the classpath
   *         is edited to include the new natives directory. False otherwise.
   */
  def unpackNatives(destLocation: nio.file.Path = defaultLocation,
                    srcURL: URL = defaultSrcURL): Try[Unit]
     //We cannot unpack if the target location is read only.
    = if(!destLocation.getFileSystem.isReadOnly) {
        logger info s"Unpacking natives from $srcURL to $destLocation"
        //Add destLocation/native to the classloader via an ugly hack
        UnpackerJavaCallouts.mangleClassloader(destLocation.resolve("native").toString)

        val zURI = URI.create("jar:" + srcURL.toURI.toString + "!/lwjgl-natives")
        logger info s"zURI is $zURI"
        Try{
          Files.createDirectory(destLocation)
          Files.createDirectories(destLocation.resolve("native"))
          logger info s"Created local natives directory $destLocation"
        } recover {
            case x: FileAlreadyExistsException =>
              logger debug s"Local natives directory $destLocation already exists"
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
              = { logger info s"Previsit dir $dir"
                  if(dir.toString.compareTo(top.toString) != 0) {
                    try {
                      Files.createDirectory(
                        destLocation.resolve("native")
                                    .resolve(top.relativize(dir).toString)
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
                  logger trace s"Visiting: $file"
                  nativeExt match {
                    case Some(ext) if file.toString.endsWith(ext) =>
                      Files.copy(
                        Files.newInputStream(file),
                        destLocation.resolve("native")
                                    .resolve(top.relativize(file).toString)
                      )
                      logger debug s"Copied $file to natives directory"
                    case None =>
                      Files.copy(
                        Files.newInputStream(file),
                        destLocation.resolve("native")
                                    .resolve(top.relativize(file).toString)
                      )
                      logger debug s"Copied $file to natives directory"
                    case _ =>
                  }
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
        val str = "Could not unpack natives, " +
          s"destination $destLocation was read-only!"
        logger warn str
        Failure(new IOException(str))
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
