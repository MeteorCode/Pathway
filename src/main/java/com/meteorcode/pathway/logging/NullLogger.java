package com.meteorcode.pathway.logging;

/**
 * NullLogger is a LogDestination which deliberately does absolutely nothing.
 */
public class NullLogger implements LogDestination {


    @Override
    public void log(String message) {

    }

    @Override
    public void log(String tag, String message) {

    }

    @Override
    public void log(String message, Throwable t) {

    }

    @Override
    public void log(String tag, String message, Throwable t) {

    }
}
