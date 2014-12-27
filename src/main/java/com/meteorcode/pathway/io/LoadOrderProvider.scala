package com.meteorcode.pathway.io

import scala.collection.mutable

/**
 * Interface representing a policy provider for resolving load-order conflicts.
 * An implementation of this is consulted by a [[com.meteorcode.pathway.io.ResourceManager ResourceManager]] to
 * determine the correct priority order.
 *
 * Created by Hawk on 8/13/14.
 *
 * @see [[com.meteorcode.pathway.io.ResourceManager]]
 */
trait LoadOrderProvider {
  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a List of [[com.meteorcode.pathway.io.FileHandle FileHandle]] representing top-level roots in the
   *              physical filesystem.
   * @return a List of those FileHandles ordered by their load priority
   */
  def orderPaths(paths: java.util.List[FileHandle]): java.util.List[FileHandle]
}
