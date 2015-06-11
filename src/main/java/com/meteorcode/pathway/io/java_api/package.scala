package com.meteorcode.pathway.io

import java.util.Optional

import scala.language.implicitConversions

/**
 * Created by hawk on 6/10/15.
 */
package object java_api {
  implicit def optionToOptional[T](option: Option[T]): Optional[T] = option map
    (value => Optional.of(value)) getOrElse Optional.empty()

}
