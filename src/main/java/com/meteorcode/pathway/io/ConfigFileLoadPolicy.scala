package com.meteorcode.pathway.io

import scala.collection.mutable
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
 * [[com.meteorcode.pathway.io.LoadOrderProvider LoadOrderProvider]], order them according to that provider's policy,
 * and then insert them at the end of the list. The default fallback load policy is the
 * [[com.meteorcode.pathway.io.AlphabeticLoadPolicy AlphabeticLoadPolicy]], but another may be specified.
 *
 * Created by hawk on 8/15/14.
 */
class ConfigFileLoadPolicy(config: FileHandle, fallback: LoadOrderProvider) extends LoadOrderProvider {
  private val order = for (line <- config.readString.split("\n")
                           if !line.startsWith("//")) yield line

  /**
   * Constructor for a ConfigFileLoadPolicy without a specified fallback option. The fallback option will default to
   * [[com.meteorcode.pathway.io.AlphabeticLoadPolicy alphabetic order]].
   * @param config a [[com.meteorcode.pathway.io.FileHandle FileHandle]] into a text file containing the specified
   *               load order
   */
  def this (config: FileHandle) = this(config, new AlphabeticLoadPolicy())

  /**
   * Takes an unordered set of top-level paths and returns a list of those paths, ordered by load priority.
   * @param paths a set of Strings representing top-level paths in the physical filesystem.
   * @return a List of those paths ordered by their load priority
   */
  def orderPaths(paths: java.util.List[FileHandle]): java.util.List[FileHandle] = {
    var result = List[FileHandle]()
    for (path <- order)
      result = result :+ paths.find{f => f.physicalPath == path}.get
    if (result.length < paths.length)
      result = result ++ fallback.orderPaths(paths.filter(p => !(result contains p)))
    result
  }

}
