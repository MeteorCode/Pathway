package com.meteorcode.pathway.io

package object scala_api {
  /**
   * Scala API definition for a load-order policy.
   *
   * TODO: Make ResourceManager support this
   * TODO: Provide Java API for a Policy wrapper class and implicit conversion to this.
   */
  type LoadOrderPolicy = (Seq[FileHandle]) => Seq[FileHandle]

}
