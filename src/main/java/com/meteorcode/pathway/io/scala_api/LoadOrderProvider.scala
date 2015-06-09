package com.meteorcode.pathway.io
package scala_api

/**
 * Interface representing a policy provider for resolving load-order conflicts.
 * An implementation of this is consulted by a [[com.meteorcode.pathway.io.ResourceManager ResourceManager]] to
 * determine the correct priority order.
 *
 * This file was created by Hawk on 8/13/14.
 *
 * @author Hawk Weisman
 * @see [[com.meteorcode.pathway.io.ResourceManager]]
 */
@deprecated("This can be replaced with a type alias for (Seq[FileHandle]) => Seq[FileHandle]," +
  "wrapper class can be moved to Java API.",
  since="v2.0.0")
trait LoadOrderProvider {
  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a List of [[com.meteorcode.pathway.io.FileHandle FileHandle]] representing top-level roots in the
   *              physical filesystem.
   * @return a List of those FileHandles ordered by their load priority
   */
  def orderPaths(paths: Seq[FileHandle]): Seq[FileHandle]
}
