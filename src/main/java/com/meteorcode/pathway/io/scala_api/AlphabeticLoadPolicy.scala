package com.meteorcode.pathway.io
package scala_api

import java.io.{IOException, File}

import scala.collection.JavaConversions._

/**
 * Simple [[LoadOrderProvider LoadOrderProvider]] implementation.
 * Paths are given higher priority based on their alphabetic position (case-insensitive).
 *
 * You can use this as an example while writing your own LoadOrderProviders.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * Created by hawk on 8/13/14.
 */
class AlphabeticLoadPolicy extends LoadOrderProvider {
  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a set of Strings representing top-level paths in the physical filesystem.
   * @return a List of those paths ordered by their load priority
   */
  def orderPaths(paths: Seq[FileHandle]): Seq[FileHandle] = paths.sortWith(
    _.physicalPath
      .getOrElse(throw new IOException("FATAL: FileHandle did not have a physical path"))
      .split(File.separatorChar)
      .last
      .toLowerCase
      <
      _.physicalPath
        .getOrElse(throw new IOException("FATAL: FileHandle did not have a physical path"))
        .split(File.separatorChar)
        .last
  )
}
