package com.meteorcode.pathway.logging
import java.util.logging.{Logger, Level}

/**
 * A LogDestination for writing to a java.util.logging logger.
 * By default, this logs at the "info" level.
 * TODO: this should eventually check to see if a java.util.logging log level is in the tag, and log at that level.
 * Created by hawk on 8/22/14.
 */
class UtilLogWrapper(protected val logger: Logger) extends LogDestination {
  /**
   * Log a String to this destination with default context
   * @param message the String to log
   */
  def log(message: String) = logger.log(Level.INFO, message)

  /**
   * Log a String to this destination with the given context tag
   * @param tag The context tag to label this log with
   * @param message The message to log
   */
  def log(tag: String, message: String) = logger.log(Level.INFO, tag + ": " + message)

  /**
   * Log a String, with optional Throwable (such as an exception), and default context
   * to this destination.
   * Throwables are logged to SEVERE by default.
   * 
   * @param message The message to log
   * @param t The Throwable to add to the log
   */
  def log(message: String, t: Throwable) = logger.log(Level.SEVERE, message, t)

  /**
   * Log a message with context and Throwable attached. 
   * Throwables are logged to SEVERE by default.
   * 
   * @param tag The context tag to label this log with
   * @param message The message to log
   * @param t The Throwable to add to the log
   */
  def log(tag: String, message: String, t: Throwable) = logger.log(Level.SEVERE, tag + ": " + message, t)

}
