package com.meteorcode.pathway.io.scala

import java.io._
import java.nio.charset.Charset

import scala.util.Try
import scala.io.Source

/**
 * Created by hawk on 5/9/15.
 */
trait FileHandle {

  def exists: Boolean
  def isDirectory: Boolean
  def isWritable: Boolean
  def path: String
  protected[io] def physicalPath: String
  protected[io] def file: Option[File]
  def list: Try[Seq[FileHandle]]
  def read: Try[InputStream]
  def read(bufSize: Int): Try[BufferedInputStream]
  def readString: Try[String] = readString(Charset.defaultCharset)
  def readString(charset: Charset): Try[String] = read.map(Source fromInputStream _ mkString)
  def write(append:Boolean): Try[OutputStream] // TODO: possibly Option[Try[OutputStream]]?
  def write(bufferSize: Integer, append: Boolean): Try[BufferedOutputStream]
  def writeString(string: String, append: Boolean): Try[Unit]
  def writeString(string: String, charset: Charset, append: Boolean): Try[Unit]
  def delete: Try[Unit]
  def length: Long
  // These should maybe make option/try?
  def child(childName: String): FileHandle
  def sibling(siblingName: String): FileHandle
  def parent: FileHandle

}
