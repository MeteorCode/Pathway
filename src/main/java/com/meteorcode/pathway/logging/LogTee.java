package com.meteorcode.pathway.logging;

/**
 * LogTee is a simple log multiplexer:
 * It itself is a LogDestination, but any logs which
 * are published to it are sent to both of its child
 * LogDestinations.
 *
 * LogTee does not write anywhere on its own.
 *
 * @author xyzzy
 *
 */
public class LogTee implements LogDestination {
	LogDestination a, b;

    /**
     * Creates the default LogTee, which writes out to
     * an anonymous cache and also to System.out
     */
    public LogTee() {
        this.a = new CacheLog();
        this.b = new SysOutLog();
    }

    /**
     * Multiplexes two custom LogDestinations provided to the LogTee.
     * @param a The first custom destination
     * @param b The second custom destination
     */
    public LogTee(LogDestination a, LogDestination b) {
        this.a = a;
        this.b = b;
    }

    public void log(String message) {
        a.log(message);
        b.log(message);
    }

    public void log(String tag, String message) {
        a.log(tag, message);
        b.log(tag, message);
    }

    public void log(String message, Throwable t) {
        a.log(message, t);
        b.log(message, t);
    }

    public void log(String tag, String message, Throwable t) {
        a.log(tag, message, t);
        b.log(tag, message, t);
    }

    /**
     * Returns the first LogDestination which is being written to by this LogTee
     * @return A LogDestination
     */
    public LogDestination getA() {
        return this.a;
    }

    /**
     * Returns the second LogDestination which is being written to by this LogTee
     * @return A LogDestination
     */
    public LogDestination getB() {
        return this.b;
    }
}
