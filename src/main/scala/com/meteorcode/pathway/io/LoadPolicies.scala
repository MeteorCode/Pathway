package com.meteorcode.pathway.io

import java.io.{IOException, File}

/**
 * Premade [[LoadOrderPolicy LoadOrderPolicies]] for the
 * Pathway [[ResourceManager]].
 *
 * Created by hawk on 8/9/15.
 */
object LoadPolicies {

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
   * @return a List of those paths ordered by their load priority
   */
  val alphabetic: LoadOrderPolicy
    = (paths) => paths sortWith { lastPathElem(_) < lastPathElem(_) }

  /**
   * Builds a config file policy for the given config file (a [[FileHandle]])
   * and the given fallback policy to be used for paths not in the file.
   *
   * @param config a [[FileHandle]] to the desired configuration file.
   * @param fallback a [[LoadOrderPolicy]] to be used for ordering paths
   *                 not present in the configuration file.
   *                 [[LoadPolicies.alphabetic]] is used by default.
   * @return a [[LoadOrderPolicy]] from the given configuration file.
   */
  def mkConfigPolicy(config: FileHandle,
                     fallback: LoadOrderPolicy = alphabetic): LoadOrderPolicy = {

    lazy val order: Seq[String]
      = config.readString
        .map {
          _.split("\n")
            .filter(line => !line.startsWith("//"))
        }
        .getOrElse(throw new IOException("Could not read config file."))

    (paths) => (order flatMap { path: String =>
        paths find { f: FileHandle =>
          path == f.assumePhysPath
        }
      }) ++ fallback(
        paths filterNot {f: FileHandle =>
          order.contains(f.assumePhysPath)
        }
      )

    }

}
