package com.meteorcode.pathway.logging;

/**
 * A LogDestination which outputs to System.out:
 * Logs written to this destination will be written
 * using System.out and Throwables attached here will
 * have printStackTrace called on them.
 * Created by xyzzy on 8/19/14.
 */
public class SysOutLog implements LogDestination {
    public void log(String message) {
        log(getContextTag(), message);
    }

    public void log(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    public void log(String message, Throwable t) {
        log(getContextTag(), message, t);
    }

    public void log(String tag, String message, Throwable t) {
        log(tag, message);
        t.printStackTrace();
    }

    private static String getContextTag() {
        return Thread.currentThread().getName();
    }
}
