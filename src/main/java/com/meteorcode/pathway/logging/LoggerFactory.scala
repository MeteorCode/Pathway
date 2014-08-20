package com.meteorcode.pathway.logging

/**
 * A singleton class that provides the game's logger. By default, this is an instance of
 * [[com.meteorcode.pathway.logging.LogTee LogTee]], but it can be configured using the
 * [[com.meteorcode.pathway.logging.LoggerFactory#setLogger setLogger]] method.
 *
 * Created by hawk on 8/19/14.
 */
object LoggerFactory {
  private var logger: LogDestination = new LogTee()

  /**
   * Sets the logger that this stores a reference to.
   * @param logger The new logger
   */
  def setLogger(logger: LogDestination) = { this.logger = logger }

  /**
   * Returns a reference to the game's global logger
   * @return reference to the game's global logger
   */
  def getLogger: LogDestination = logger

}
