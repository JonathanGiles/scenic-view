package com.javafx.experiments.scenicview.utils;

import java.io.*;
import java.util.Properties;

import com.javafx.experiments.scenicview.connector.remote.RemoteScenicViewImpl;
import com.javafx.experiments.scenicview.utils.ClassPathDialog.PathChangeListener;

/**
 * 
 * @author Jonathan
 */
public class ScenicViewBooter implements PathChangeListener {

    public static void main(final String[] args) {
        new ScenicViewBooter();
    }

    private static Properties properties;
    private static boolean isAttachAPIAvailable;
    private static boolean isJFXAvailable;

    private ScenicViewBooter() {
        isAttachAPIAvailable = false;
        isJFXAvailable = false;
        boolean wasAttachAPIAvailable = false;
        boolean wasJFXAvailable = false;
        // Test if we can load a class from tools.jar
        try {
            Class.forName("com.sun.tools.attach.AttachNotSupportedException").newInstance();
            isAttachAPIAvailable = true;
            wasAttachAPIAvailable = true;
        } catch (final Exception e) {
            System.out.println("Tools.jar not found");
        }
        // Test if we can load a class from jfxrt.jar
        try {
            Class.forName("javafx.beans.property.SimpleBooleanProperty").newInstance();
            isJFXAvailable = true;
            wasJFXAvailable = true;
        } catch (final Exception e) {
            System.out.println("jfxrt.jar not found");
        }

        if (wasAttachAPIAvailable && wasJFXAvailable) {
            // Launch ScenicView directly
            RemoteScenicViewImpl.start();
        } else {
            // First read the properties file to find previous entries
            properties = PropertiesUtils.loadProperties();
            String attachPath = properties.getProperty("attachPath");
            String jfxPath = properties.getProperty("jfxPath");
            if (checkPath(attachPath)) {
                isAttachAPIAvailable = true;
            }
            if (checkPath(jfxPath)) {
                isJFXAvailable = true;
            }
            // If we don't have valid entries in the properties file try to find
            // them
            if (!isAttachAPIAvailable) {
                attachPath = getToolsClassPath();
            }
            if (!isJFXAvailable) {
                jfxPath = getJFXClassPath();
            }
            new ClassPathDialog(this, attachPath, jfxPath).setVisible(true);
        }

    }

    private boolean checkPath(final String path) {
        if (path != null && !path.equals("")) {
            return new File(path).exists();
        }
        return false;
    }

    private String getJFXClassPath() {
        // TODO Auto-generated method stub
        return "C:\\Archivos de programa\\Oracle\\JavaFX 2.2 Runtime\\lib\\jfxrt.jar";
    }

    private String getToolsClassPath() {
        final String javaHome = System.getProperty("java.home");
        if (!javaHome.contains("jdk")) {
            System.out.println("Error: No JDK found on system");
            return null;
        }

        // This points to, for example, "C:\Program Files
        // (x86)\Java\jdk1.6.0_30\jre"
        // This is one level too deep. We want to pop up and then go into the
        // lib directory to find tools.jar
        System.out.println("JDK found at: " + javaHome);

        final File toolsJar = new File(javaHome + "/../lib/tools.jar");
        if (!toolsJar.exists()) {
            System.out.println("Error: Can not find tools.jar on system - disabling VM lookup");
            return null;
        }

        System.out.println("Attempting to load tools.jar file from here:");
        System.out.println(toolsJar.getAbsolutePath());

        return toolsJar.getAbsolutePath();
        // try {
        // ClassPathHacker.addFile(toolsJar);
        // } catch (final IOException ex) {
        // ex.printStackTrace();
        // isAttachAPIAvailable = false;
        // return;
        // }

    }

    @Override public void onPathChanged(final String toolsPath, final String jfxPath) {
        // TODO Auto-generated method stub
        try {
            ClassPathHacker.addFile(new File(toolsPath));
        } catch (final IOException ex) {
            ex.printStackTrace();

            return;
        }
        try {
            ClassPathHacker.addFile(new File(jfxPath));
        } catch (final IOException ex) {
            ex.printStackTrace();

            return;
        }
        properties.setProperty("attachPath", toolsPath);
        properties.setProperty("jfxPath", jfxPath);
        PropertiesUtils.saveProperties(properties);
        RemoteScenicViewImpl.start();
    }
}
