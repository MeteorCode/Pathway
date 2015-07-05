package com.meteorcode.pathway.io

import java.util.Optional

import scala.language.implicitConversions

/**
 * Pathway IO Java API
 * -------------------
 *
 * This is a version of the Pathway input/output API to be used by Java
 * clients.
 *
 * @author Hawk Weisman
 * Created by hawk on 6/10/15.
 */
package object java_api {
  /**
   * Implicitly transform a [[scala.Option Option]] into a
   * [[java.util.Optional]].
   *
   * This really ought to be in the Scala standard library.
   *
   * @param  option an [[scala.Option]]
   * @return an [[java.util.Optional]] containing either the contents
   *         of that Option if it is Some, or nothing if it is None.
   */
  implicit def optionToOptional[T >: Null](option: Option[T]): Optional[T]
    = Optional ofNullable (option orNull)

}
