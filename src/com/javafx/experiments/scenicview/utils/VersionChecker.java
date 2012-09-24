package com.javafx.experiments.scenicview.utils;

import java.io.*;
import java.net.*;

public class VersionChecker {

    public static String checkVersion(final String actual) {
        URL u;
        InputStream is = null;
        final StringBuilder s = new StringBuilder();
        final byte[] buffer = new byte[256];

        try {
            u = new URL("file:/c:/elevate.txt");
            is = u.openStream(); // throws an IOException
            int read;

            while ((read = is.read(buffer)) != -1) {
                s.append(new String(buffer, 0, read, "UTF-8"));
            }
            final String info = s.toString();
            if (info.indexOf(actual) == -1) {
                return info;
            }

        } catch (final MalformedURLException mue) {
            mue.printStackTrace();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (final IOException ioe) {
                // just going to ignore this one
            }
        }
        return null;
    }

}