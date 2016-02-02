package com.meteorcode.pathway

import scala.language.postfixOps

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

  /**
   * Scala API definition for a load-order policy.
   *
   * TODO: Make ResourceManager support this
   * TODO: Provide Java API for a Policy wrapper class and
   * 			 implicit conversion to this.
   */
  type LoadOrderPolicy = (Seq[FileHandle]) ⇒ Seq[FileHandle]

  protected[io] implicit class Path(val path: String) {

    lazy val extension: Option[String]
      = path split '.' drop 1 lastOption

    lazy val name: Option[String]
      = path.split('/').lastOption flatMap {  _ split '.' headOption }

    /**
      * Removes the trailing slash from a path.
 *
      * @return the path with any trailing slashes removed.
      */
    lazy val withoutTrailingSlash: String
      = if (path endsWith "/") { path dropRight 1 } else { path }

    lazy val parent: Option[String]
      = path split '/' dropRight 1 lastOption
  }

  // Regex for determining if a path is inside an archive
  protected[io] val inArchiveRE
    = """([\s\S]*[^\/]*)(.zip|.jar)\/([^\/]+.*[^\/]*)*""".r
  // Regex for determining if a path is fo an archive file
  protected[io] val isArchiveRE = """([^\/\.]+)(.zip|.jar)""".r
  // Regex for extracting subdirectories
  protected[io] val subdirRE    = """^[^\/]+\/*$""".r


}
