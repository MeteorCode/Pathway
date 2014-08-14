package com.meteorcode.pathway.io
import scala.collection.mutable

/**
 * Simple [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]] implementation.
 * Paths are given higher priority based on their alphabetic position (case-insensitive).
 *
 * You can use this as an example while writing your own LoadOrderProviders.
 *
 * Created by hawk on 8/13/14.
 */
class AlphabeticLoadPolicy extends LoadOrderProvider {
  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a set of Strings representing top-level paths in the physical filesystem.
   * @return a List of those paths ordered by their load priority
   */
  def orderPaths(paths: List[FileHandle]): List[FileHandle] = paths.sortWith(_.name.toLowerCase < _.name.toLowerCase)
}
