package com.meteorcode.pathway.io.scala_api

import java.io.{File, IOException, InputStream, OutputStream}
import java.util

import scala.util.Try

/**
 * Wraps another [[FileHandle]] with a different virtual path.
 * This is for internal use only.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * This file was created by Hawk on 8/27/14.
 */
protected[io] class RedirectFileHandle (
  protected val wrapped: FileHandle,
  virtualPath: String)
extends FileHandle(virtualPath, wrapped.manager) {

  override def exists: Boolean = wrapped.exists

  override protected[io] def file: Option[File] = wrapped.file

  override def length: Long = wrapped.length

  override def write(append: Boolean): Option[OutputStream]
    = wrapped.write(append)

  override def delete: Boolean = wrapped.delete

  override def isDirectory: Boolean = wrapped.isDirectory

  override def list: Try[Seq[FileHandle]] = wrapped.list

  override def read: Try[InputStream] = wrapped.read

  override def writable: Boolean = wrapped.writable

  override protected[io] def physicalPath: Option[String]
    = wrapped.physicalPath
}
