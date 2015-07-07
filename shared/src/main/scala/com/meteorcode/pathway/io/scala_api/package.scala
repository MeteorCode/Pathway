package com.meteorcode.pathway.io
/**
 * ==Pathway IO Scala API==
 *
 * This is a version of the Pathway input/output API to be used by Scala
 * clients.
 *
 * @author Hawk Weisman
 * Created by hawk on 6/10/15.
 */
package object scala_api {
  /**
   * Scala API definition for a load-order policy.
   *
   * TODO: Make ResourceManager support this
   * TODO: Provide Java API for a Policy wrapper class and
   * 			 implicit conversion to this.
   */
  type LoadOrderPolicy = (Seq[FileHandle]) => Seq[FileHandle]

  protected[io] implicit class Path(val path: String) {

    def extension: Option[String]
      = path.split('.').drop(1)
            .lastOption

    def name: Option[String]
      = path.split('/').lastOption
            .flatMap( _.split('.').headOption )
  }

}
