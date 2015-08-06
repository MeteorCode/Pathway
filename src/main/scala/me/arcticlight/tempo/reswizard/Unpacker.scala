package me.arcticlight.tempo.reswizard

import java.io.{File, IOException}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

/**
 * Unpacker is a utility class capable of unpacking
 * resources included in a Jar file into the local filesystem
 * near the Jar when it is run. This is useful for, e.g., unpacking
 * Native dependencies such as LWJGL's natives before running code
 * that requires it.
 */
object Unpacker {
  
  val defaultLocation = System.getProperty("user.home") + System.getProperty("file.separator") + ".pathway" + System.getProperty("file.separator")
  
  /**
   * Attempts to unpack native JARs into the host filesystem, because JNI requires this.
   * @return True if and only if the unpacking succeeds, and the classpath is edited to include the new natives directory. False otherwise.
   */
  def unpackNatives(destLocation:String = defaultLocation, srcURL:URL = this.getClass.getProtectionDomain.getCodeSource.getLocation):Boolean = {
    val targetDir = Paths.get(destLocation)
    
    //Add destLocation/native to the classloader via an ugly hack
    me.arcticlight.tempo.reswizard.UnpackerJavaCallouts.mangleClassloader(destLocation.resolve("native").toString)
    
    //We cannot unpack if the target location is read only.
    if(targetDir.getFileSystem.isReadOnly) return false;
    
    val zURI = java.net.URI.create("jar:" + srcURL.toURI.toString + "!/buildres")
    try {
      FileSystems.newFileSystem(zURI, Map("create" -> "false").asJava)
    } catch {
      case x: FileSystemAlreadyExistsException =>
      case x: Throwable => System.err.println(s"Warning: ${x.getMessage}")
    }
    
    val top = Paths.get(zURI)
    Files.walkFileTree(top, new FileVisitor[Path] {
      import FileVisitResult._
      
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if(dir.toString.compareTo(top.toString) != 0) {
          try {
            Files.createDirectory(targetDir.resolve(top.relativize(dir).toString))
          } catch {
            case x: FileAlreadyExistsException =>
            case x: Throwable => System.err.println(s"Warning: ${x.getMessage}")
          }
        }
        
        CONTINUE
      }
      
      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = CONTINUE
      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = CONTINUE
      
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        try {
          Files.copy(Files.newInputStream(file), targetDir.resolve(top.relativize(file).toString))
        } catch {
          case x: FileAlreadyExistsException =>
          case x: Throwable => System.err.println(s"Warning: ${x.getMessage}")
        }
        CONTINUE
      }
    })
  }
  
  
  def UnpackResources(): Unit = {

    import collection.JavaConverters._
    val myURL = this.getClass.getProtectionDomain.getCodeSource.getLocation
    val myDir = java.nio.file.Paths.get(myURL.toURI).getParent

    me.arcticlight.tempo.reswizard.UnpackerJavaCallouts.mangleClassloader(myDir.resolve("native").toString)

    if (myDir.getFileSystem.isReadOnly) return
    
    //TODO: currently here. Do we want to 
    val ZURI = java.net.URI.create("jar:" + myURL.toURI.toString + "!/buildres")
    try {
      FileSystems.newFileSystem(ZURI, Map("create" -> "false").asJava)
    } catch {
      case x: FileSystemAlreadyExistsException =>
      case x: Throwable => System.err.println(s"Warning: ${x.getMessage}")
    }
    val top = Paths.get(ZURI)
    Files.walkFileTree(top, new FileVisitor[Path] {

      import FileVisitResult._

      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (dir.toString.compareTo(top.toString) != 0)
          try {
            Files.createDirectory(myDir.resolve(top.relativize(dir).toString))
          } catch {
            case x: FileAlreadyExistsException =>
            case x: Throwable => println("Warn: " + x.getMessage)
          }
        CONTINUE
      }

      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = CONTINUE

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        try {
          Files.copy(Files.newInputStream(file), myDir.resolve(top.relativize(file).toString))
        } catch {
          case x: FileAlreadyExistsException =>
          case x: Throwable => println("Warn: " + x.getMessage)
        }
        CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = CONTINUE
    })
  }
}