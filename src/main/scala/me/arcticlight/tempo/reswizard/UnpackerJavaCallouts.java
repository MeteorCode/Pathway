package me.arcticlight.tempo.reswizard;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Secret evil hack used by Unpacker
 */
class UnpackerJavaCallouts {
    protected static void mangleClassloader(String binaryPath) {
        //Horrible Awful Classpath Hack from The Internet.
        //
        //Source: https://www.java.net/node/655213
        //
        // This Awful Hack doesn't seem to work in Scala-land,
        // so it is farmed out to Java-land temporarily, where
        // it is then called on by Unpacker
        //
        String newLibPath = binaryPath + File.pathSeparator +
                System.getProperty("java.library.path");
        System.setProperty("java.library.path", newLibPath);
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            if (fieldSysPath != null) {
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
        } catch (Throwable ignored) {

        }
    }
}
