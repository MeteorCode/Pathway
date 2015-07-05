package com.meteorcode.pathway.io.java_api

import com.meteorcode.pathway.io.scala_api

import java.io.IOException
import java.util

import scala.collection.JavaConverters.{
  seqAsJavaListConverter,
  asScalaBufferConverter
}

/**
 * An example of a load-order policy that uses a configuration file to
 * determine load order.
 *
 * This load order policy takes a [[java_api.FileHandle FileHandle]] to a text
 * file, and uses that file as a configuration to determine the load  order.
 * The configuration file should contain an ordered list of root directories,
 * one per line, with the directory at the top of the list being the
 * highest-priority and the file at the bottom of the list being the lowest
 * priority. Lines beginning with `//` will be treated as comments and ignored.
 *
 * This also demonstrates the use of "fallback" load orders. If the
 * configuration file cannot be used to order all  roots, it will pass any
 * roots it could not order to the fallback
 * [[java_api.LoadOrderProvider LoadOrderProvider]], order them according to
 * that provider's policy, and then insert them at the end of the list. The
 * default fallback load policy is the
 * [[java_api.AlphabeticLoadPolicy AlphabeticLoadPolicy]], but another may be
 * specified.
 *
 * @author Hawk Weisman
 * @since v2.0.0
 *
 * Created by hawk on 8/15/14.
 */
class ConfigFileLoadPolicy(
  config: scala_api.FileHandle,
  fallback: LoadOrderProvider
  )
extends LoadOrderProvider {

  private[this] lazy val order: Seq[String]
    = config.readString
            .map { _.split("\n")
                    .filter(line => !line.startsWith("//"))
                 }
            .getOrElse( throw new IOException("Could not read config file.") )

  private[this] def NoPathException = new IOException (
    "FATAL: FileHandle did not have a physical path"
  )
  /**
   * Constructor for a ConfigFileLoadPolicy without a specified fallback
   * option. The fallback option will default to
   * [[java_api.AlphabeticLoadPolicy alphabetic order]].
   * @param config a [[.java_api.FileHandle FileHandle]] into a text file
   *               containing the specified load order
   */
  def this (config: scala_api.FileHandle)
    = this(config, new AlphabeticLoadPolicy())

  private[this] def orderScalaPaths(paths: Seq[FileHandle]): Seq[FileHandle]
    = order.flatMap { path =>
      paths.find  { f: FileHandle =>
          path == f.physicalPath
                   .getOrElse(throw NoPathException)
           }
         } ++ fallback
          .orderPaths( paths
            .filterNot( (f: FileHandle) =>
               order.contains(f.physicalPath
                               .getOrElse(throw NoPathException))
           ).asJava
         ).asScala



  /**
   * Takes an unordered set of top-level paths and returns a list of those
   * paths, ordered by load priority.
   * @param paths a set of [[java_api.FileHandle FileHandles]] representing
   *              top-level paths in the physical filesystem.
   * @return a List of those paths ordered by their load priority
   */
  def orderPaths(paths: util.List[FileHandle]): util.List[FileHandle]
    = orderScalaPaths(paths.asScala).asJava

}
