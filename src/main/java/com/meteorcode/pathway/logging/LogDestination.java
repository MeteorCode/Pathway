package com.meteorcode.pathway.logging;

/**
 * A generic logger destination, which may accept Strings.
 * Created by xyzzy on 8/19/14.
 */
public interface LogDestination {

    /**
     * Log a String to this destination with default context
     * @param message the String to log
     */
    public void log(String message);

    /**
     * Log a String to this destination with the given context tag
     * @param tag The context tag to label this log with
     * @param message The message to log
     */
    public void log(String tag, String message);

    /**
     * Log a String, with optional Throwable (such as an exception), and default context
     * to this destination.
     * @param message The message to log
     * @param t The Throwable to add to the log
     */
    public void log(String message, Throwable t);

    /**
     * Log a message with context and Throwable attached.
     * @param tag The context tag to label this log with
     * @param message The message to log
     * @param t The Throwable to add to the log
     */
    public void log(String tag, String message, Throwable t);
}