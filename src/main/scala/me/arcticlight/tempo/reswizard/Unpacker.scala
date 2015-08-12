package me.arcticlight.tempo.reswizard

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.net.{URL,URI}

import com.typesafe.scalalogging.LazyLogging

import collection.JavaConverters._
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
  val sep = System.getProperty("file.separator")
  val defaultLocation = System.getProperty("user.home") + s"$sep.pathway$sep"

  /**
   * Attempts to unpack native JARs into the host filesystem, because JNI
   * requires this.
   * @return True if and only if the unpacking succeeds, and the classpath
   *         is edited to include the new natives directory. False otherwise.
   */
  def unpackNatives(destLocation: String = defaultLocation,
                    srcURL:URL = this.getClass
                                     .getProtectionDomain
                                     .getCodeSource
                                     .getLocation): Boolean = {

    val targetDir = Paths.get(destLocation)

    //Add destLocation/native to the classloader via an ugly hack
    UnpackerJavaCallouts.mangleClassloader(
      Paths.get(destLocation)
           .resolve("native")
           .toString
      )

    //We cannot unpack if the target location is read only.
    if(targetDir.getFileSystem.isReadOnly) return false;

    val zURI = URI.create("jar:" + srcURL.toURI.toString + "!/lwjgl-natives")
    try {
      FileSystems.newFileSystem(zURI, Map("create" -> "false").asJava)
    } catch {
      case x: FileSystemAlreadyExistsException =>
      case x if NonFatal(x) =>
        logger.warn("An exception occurred while creating FileSystem", x)
    }

    val top = Paths.get(zURI)
    Files.walkFileTree(top, new FileVisitor[Path] with LazyLogging {
      import FileVisitResult._

      override def preVisitDirectory(dir: Path,attrs: BasicFileAttributes): FileVisitResult
        = { if(dir.toString.compareTo(top.toString) != 0) {
              try {
                Files.createDirectory(
                  targetDir.resolve(top.relativize(dir).toString)
                )
              } catch {
                case x: FileAlreadyExistsException =>
                case x if NonFatal(x) =>
                  logger.warn(s"An exception occurred before visiting $dir", x)
              }
            }
          CONTINUE
        }

      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult
        = CONTINUE

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult
        = CONTINUE

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult
        = { try {
              Files.copy(Files.newInputStream(file),
                         targetDir.resolve(top.relativize(file).toString)
                )
            } catch {
              case x: FileAlreadyExistsException =>
              case x if NonFatal(x) =>
                logger.warn(s"An exception occurred while visiting $file", x)
            }
            CONTINUE
          }
    })
    return true
  }
}
