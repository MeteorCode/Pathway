package com.meteorcode.pathway.logging;

/**
 * Created by hawk on 8/20/14.
 */
public class LoggerFactory {
    private static LogDestination instance;

    public LoggerFactory() {};

    public static void setLogger (LogDestination logger) { instance = logger; }

    public static LogDestination getLogger () {
        if(instance == null) instance = new LogTee();
        return instance;
    }
}
