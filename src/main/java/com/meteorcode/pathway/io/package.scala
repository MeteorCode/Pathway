package com.meteorcode.pathway

/**
 * Created by hawk on 12/27/14.
 */
package object io {
  val inArchive = """([\s\S]*[^\/]*)(.zip|.jar)\/([^\/]+.*[^\/]*)*""".r
  val isArchive = """([^\/\.]+)(.zip|.jar)""".r

  protected[io] def trailingSlash(name: String) = if (name endsWith "/") name dropRight 1 else name
}
