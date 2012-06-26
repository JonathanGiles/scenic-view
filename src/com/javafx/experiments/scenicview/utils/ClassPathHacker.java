package com.javafx.experiments.scenicview.utils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;

@SuppressWarnings("rawtypes")
public class ClassPathHacker {

    private static final Class[] parameters = new Class[] { URL.class };

    public static void addFile(final String s) throws IOException {
        final File f = new File(s);
        addFile(f);
    }

    @SuppressWarnings("deprecation") public static void addFile(final File f) throws IOException {
        addURL(f.toURL());
    }

    public static void addURL(final URL u) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class sysclass = URLClassLoader.class;

        try {
            @SuppressWarnings("unchecked") final Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { u });
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
