package com.meteorcode.pathway.logging

/**
 * Trait to be mixed in to Scala classes to provide logging functionality.
 *
 * Created by hawk on 5/24/15.
 */
trait Logging {
  protected lazy val logger: LogDestination = LoggerFactory.getLogger
}
