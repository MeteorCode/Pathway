package com.meteorcode.pathway.io
import java.io.{
  File,
  FileInputStream,
  BufferedInputStream
}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

trait FileHandle {
   /** Returns true if the file exists. */
   def exists(): Boolean
   /** Returns true if this file is a directory. */
   def isDirectory(): Boolean
   /** Returns the paths to the children of this directory. */
   def list: java.util.List[FileHandle]
   /** Returns the paths to the children of this directory with the specified suffix. */
   def list(suffix: String): java.util.List[FileHandle]
   /** Returns a stream for reading this file as bytes. */
   def read(): java.io.InputStream
   /** Returns a buffered stream for reading this file as bytes. */
   def read(bufferSize: Integer): java.io.BufferedInputStream
   /** Reads the entire file into a string using the platform's default charset. */
   def readString(): String
   /**Reads the entire file into a string using the specified charset.*/
   def readString(charset: Charset): String
}
class DesktopFileHandle(path: String) extends FileHandle {
  val file = new File(path)

  def exists: Boolean = file exists
  
  def isDirectory: Boolean = file isDirectory
  
  def list: java.util.List[FileHandle] = {
    var result = new java.util.ArrayList[FileHandle]
    for (path <- file.list ) { result add ResourceManager.read(path) }
    return result
  }
  
  def list(suffix: String): java.util.List[FileHandle] = {
    var result = new java.util.ArrayList[FileHandle]
    for (path <- file.list() if path endsWith(suffix)) { result add ResourceManager.read(path) }
    return result
  }
  
  def read(bufferSize: Integer): java.io.BufferedInputStream = new BufferedInputStream(read(), bufferSize)
  
  def read(): java.io.InputStream = new FileInputStream(file)
  
  def readString(): String = readString(Charset.defaultCharset())
  
  def readString(charset: Charset): String = new String(Files.readAllBytes(Paths.get(path)), charset)
 
}