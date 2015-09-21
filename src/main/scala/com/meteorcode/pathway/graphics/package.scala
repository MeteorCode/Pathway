package com.meteorcode.pathway

import scala.concurrent.Future

/**
 * ==Pathway Graphics==
 *
 * Created by hawk on 9/21/15.
 */
package object graphics {

  /**
   * Creates a new [[GraphicsContext]]
   * @return a [[Future]] on a [[GraphicsContext]]
   */
  def createGraphicsContext: Future[GraphicsContext]
    = ??? // TODO: Implement me
}
