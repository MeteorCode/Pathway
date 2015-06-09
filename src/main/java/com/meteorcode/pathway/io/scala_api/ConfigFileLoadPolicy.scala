package com.meteorcode.pathway.io.scala_api

import java.io.IOException

import scala.collection.JavaConversions._

/**
 * An example of a load-order policy that uses a configuration file to determine load order.
 *
 * This load order policy takes a FileHandle to a text file, and uses that file as a configuration to determine the load
 * order. The configuration file should contain an ordered list of root directories, one per line, with the directory
 * at the top of the list being the highest-priority and the file at the bottom of the list being the lowest priority.
 * Lines beginning with `//` will be treated as comments and ignored.
 *
 * This also demonstrates the use of "fallback" load orders. If the configuration file cannot be used to order all
 * roots, it will pass any roots it could not order to the fallback
 * [[LoadOrderProvider LoadOrderProvider]], order them according to that provider's policy,
 * and then insert them at the end of the list. The default fallback load policy is the
 * [[AlphabeticLoadPolicy AlphabeticLoadPolicy]], but another may be specified.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * Created by hawk on 8/15/14.
 */
class ConfigFileLoadPolicy(config: FileHandle, fallback: LoadOrderProvider) extends LoadOrderProvider {

  private[this] val order = config
    .readString
    .getOrElse(throw new IOException("FATAL: could not read from config file"))
    .split("/") filter ((line) => !line.startsWith("//"))

  /**
   * Constructor for a ConfigFileLoadPolicy without a specified fallback option. The fallback option will default to
   * [[AlphabeticLoadPolicy alphabetic order]].
   * @param config a [[com.meteorcode.pathway.io.scala_api.FileHandle FileHandle]] into a text file containing the specified
   *               load order
   */
  def this (config: FileHandle) = this(config, new AlphabeticLoadPolicy())

  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a set of Strings representing top-level paths in the physical filesystem.
   * @return a List of those paths ordered by their load priority
   */
  def orderPaths(paths: Seq[FileHandle]): Seq[FileHandle] = {
    var result = List[FileHandle]()
    for (path <- order)
      result = result :+ paths.find {f =>
        f.physicalPath
          .getOrElse(throw new IOException("FATAL: FileHandle did not have a physical path")) == path}.get
    if (result.length < paths.length)
      result = result ++ fallback.orderPaths(paths filter (p => !(result contains p)))
    result
  }

}
