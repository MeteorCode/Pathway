package com.meteorcode.pathway.io.java_api

import com.meteorcode.pathway.io.scala_api
import com.meteorcode.pathway.io.scala_api.ResourceManager

import scala.collection.JavaConverters.{seqAsJavaListConverter,asScalaBufferConverter}
import scala.language.implicitConversions

/**
 * Interface representing a policy provider for resolving load-order conflicts.
 * An implementation of this is consulted by a [[ResourceManager ResourceManager]] to
 * determine the correct priority order.
 *
 * This file was created by Hawk on 8/13/14.
 *
 * @author Hawk Weisman
 * @see [[ResourceManager]]
 */
abstract class LoadOrderProvider {
  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a List of [[FileHandle]] representing top-level roots in the
   *              physical filesystem.
   * @return a List of those FileHandles ordered by their load priority
   */
  def orderPaths(paths: java.util.List[FileHandle]): java.util.List[FileHandle]
}

object LoadOrderProvider {
  import FileHandle.{fromScala,asScala}
  /**
   * Unwrap a LoadOrderProvider wrapper class into a closure
   * @param wrapper
   * @return
   */
  implicit def unwrapClosure(wrapper: LoadOrderProvider): scala_api.LoadOrderPolicy = {
    (paths: Seq[scala_api.FileHandle]) => wrapper
      .orderPaths(paths
        .map(new FileHandle(_))
        .asJava)
      .asScala
      .map(_.underlying)
  }
}