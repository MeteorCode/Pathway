package com.meteorcode.pathway.io

import java.io.IOException

import scala.io.Source
import scala.language.implicitConversions

/**
 * Unsafe implicit conversions. Don't use these unless you're sure you know
 * what you're doing.
 *
 * Created by hawk on 6/10/15.
 */
object UnsafeImplicits {

  /**
   * Implicitly converts a [[FileHandle]] to a [[scala.io.Source]], throwing
   * any exceptions produced when reading the file. Only use this when you know
   * files will be readable, or while wrapped in a Try block.
   * @param f a [[FileHandle]]
   * @return a [[scala.io.Source]]
   */
  @throws(classOf[IOException])
  implicit def filehandleAsSource(f: FileHandle): Source
    = Source fromInputStream f.read.get

}
