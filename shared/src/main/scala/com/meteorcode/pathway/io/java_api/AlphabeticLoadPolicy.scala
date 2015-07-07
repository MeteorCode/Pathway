package com.meteorcode.pathway.io.java_api

import scala.collection.JavaConverters.{
  seqAsJavaListConverter,
  asScalaBufferConverter
}
import java.io.{File, IOException}
import java.util

/**
 * Simple [[java_api.LoadOrderProvider LoadOrderProvider]] implementation.
 *
 * Paths are given higher priority based on their alphabetic position
 * (case-insensitive).
 *
 * You can use this as an example while writing your own LoadOrderProviders.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * Created by hawk on 8/13/14.
 */
class AlphabeticLoadPolicy extends LoadOrderProvider {

  private[this] def lastPathElem(handle: FileHandle): String
    = handle.assumePhysPath
            .split(File.separatorChar)
            .lastOption
            .getOrElse("")
            .toLowerCase
  /**
   * Takes an unordered set of top-level paths and returns a list of those
   * paths, ordered by load priority.
   *
   * @param paths a list of [[java_api.FileHandle FileHandles]]
   *              representing top-level paths in the physical filesystem.
   * @return a List of those paths ordered by their load priority
   */
  def orderPaths(paths: util.List[FileHandle]): util.List[FileHandle]
    = paths.asScala  // TODO: rewrite this as Scala API compliant
           .sortWith( lastPathElem(_) < lastPathElem(_) )
           .asJava
}
