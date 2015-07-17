package com.meteorcode.pathway

/**
 * ==Pathway I/O==
 *
 * This packaga contains the Pathway I/O subsystem, including various
 * [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]]
 * implementations, the [[ResourceManager]],  and classes for loading mods
 * and determining load order.
 *
 * The root [[com.meteorcode.pathway.io.java_api]] package contains a public
 * Java API suitable for use from any Java-compatible language. Scala callers
 * may prefer to use [[com.meteorcode.pathway.io.scala_api]], which
 * contains a more idiomatic Scala API that is used internally by Pathway.
 * The Scala API uses Scala collections rather than Java collections, provides
 * support for functional programming with [[scala.Option]] and
 * [[scala.util.Try]], and may provide some significant performance benefits
 * for Scala users.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * This file was created by Hawk on 12/27/14.
 */
package object io {

  // Regex for determining if a path is inside an archive
  protected[io] val inArchiveRE
    = """([\s\S]*[^\/]*)(.zip|.jar)\/([^\/]+.*[^\/]*)*""".r
  // Regex for determining if a path is fo an archive file
  protected[io] val isArchiveRE = """([^\/\.]+)(.zip|.jar)""".r
  // Regex for extracting subdirectories
  protected[io] val subdirRE    = """^[^\/]+\/*$""".r

  /**
   * Removes the trailing slash from a path.
   * @param  name a String represnting a path
   * @return the pathw ith any trailing slashes removed.
   */
  protected[io] def trailingSlash(name: String)
    = if (name endsWith "/") name dropRight 1 else name
}
