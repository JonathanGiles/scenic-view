package com.javafx.experiments.scenicview.utils;

import java.io.*;

import com.javafx.experiments.scenicview.connector.remote.RemoteScenicViewImpl;
import com.javafx.experiments.scenicview.example.ScenicViewExample;

/**
 * 
 * @author Jonathan
 */
public class ScenicViewBooter {

    public static void main(final String[] args) {
        new ScenicViewBooter();
    }

    private static boolean isAttachAPIAvailable = false;

    public static boolean isAttachAPIAvailable() {
        return isAttachAPIAvailable;
    }

    private ScenicViewBooter() {
        // Test if we can load a class from tools.jar
        try {
            Class.forName("com.sun.tools.attach.AttachNotSupportedException").newInstance();
            isAttachAPIAvailable = true;
        } catch (final Exception e) {
            // First thing we need to do is try to get the tools.jar file onto
            // the classpath
            updateClassPath();
        }

        if (isAttachAPIAvailable) {
            RemoteScenicViewImpl.start();
        } else {
            ScenicViewExample.main(new String[0]);
        }
    }

    private void updateClassPath() {
        final String javaHome = System.getProperty("java.home");
        if (!javaHome.contains("jdk")) {
            System.out.println("Error: No JDK found on system - disabling VM lookup");
            isAttachAPIAvailable = false;
            return;
        }

        // This points to, for example, "C:\Program Files
        // (x86)\Java\jdk1.6.0_30\jre"
        // This is one level too deep. We want to pop up and then go into the
        // lib directory to find tools.jar
        System.out.println("JDK found at: " + javaHome);

        final File toolsJar = new File(javaHome + "/../lib/tools.jar");
        if (!toolsJar.exists()) {
            System.out.println("Error: Can not find tools.jar on system - disabling VM lookup");
            isAttachAPIAvailable = false;
            return;
        }

        System.out.println("Attempting to load tools.jar file from here:");
        System.out.println(toolsJar.getAbsolutePath());

        try {
            ClassPathHacker.addFile(toolsJar);
        } catch (final IOException ex) {
            ex.printStackTrace();
            isAttachAPIAvailable = false;
            return;
        }

        isAttachAPIAvailable = true;
    }
}
