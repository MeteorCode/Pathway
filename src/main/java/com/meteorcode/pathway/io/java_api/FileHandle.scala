package com.meteorcode.pathway.io.java_api

import com.meteorcode.pathway.io.scala_api

import scala.language.implicitConversions

/**
 * Created by hawk on 6/10/15.
 */
class FileHandle protected[io] (private val underlying: scala_api.FileHandle) {


}

object FileHandle {
  implicit def fromScala(handle: scala_api.FileHandle): FileHandle = new FileHandle(handle)
  implicit def asScala(handle: FileHandle): scala_api.FileHandle = handle.underlying
}