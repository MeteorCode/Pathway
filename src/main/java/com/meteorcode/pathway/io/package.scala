package com.meteorcode.pathway

/**
 * Created by hawk on 12/27/14.
 */
package object io {
  val inArchive = """([\s\S]*[^\/]*)(.zip|.jar)\/([^\/]+.*[^\/]*)*""".r
  val isArchive = """([^\/\.]+)(.zip|.jar)""".r
}
