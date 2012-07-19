package com.javafx.experiments.scenicview.utils;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

import com.javafx.experiments.scenicview.connector.remote.RemoteScenicViewImpl;

/**
 * 
 * @author Jonathan
 */
public class ScenicViewBooter {

    public static final String TOOLS_JAR_PATH_KEY = "attachPath";
    public static final String JFXRT_JAR_PATH_KEY = "jfxPath";

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                // instatiate the ClassPathDialog at startup to workaround
                // issues
                // if it is created after JavaFX has started.
                ClassPathDialog.init();
            }
        });

        new ScenicViewBooter();
    }

    private static Properties properties;

    private ScenicViewBooter() {
        // first we check if the classes are already on the classpath
        final boolean[] checks = testClassPathRequirements();
        final boolean isAttachAPIAvailable = checks[0];
        final boolean isJFXAvailable = checks[1];

        if (isAttachAPIAvailable && isJFXAvailable) {
            // Launch ScenicView directly
            RemoteScenicViewImpl.start();
        } else {
            // If we are here, the classes are not on the classpath.
            // First, we read the properties file to find previous entries
            properties = PropertiesUtils.loadProperties();

            String attachPath = "";
            String jfxPath = "";

            boolean needAttachAPI = !isAttachAPIAvailable;
            boolean needJFXAPI = !isJFXAvailable;

            if (needAttachAPI) {
                // the tools.jar file
                // firstly we try the properties reference
                attachPath = properties.getProperty(TOOLS_JAR_PATH_KEY);
                needAttachAPI = !Utils.checkPath(attachPath);
                if (needAttachAPI) {
                    // If we can't get it from the properties file, we try to
                    // find it on the users operating system
                    attachPath = getToolsClassPath();
                    needAttachAPI = !Utils.checkPath(attachPath);
                }

                if (!needAttachAPI) {
                    updateClassPath(attachPath);
                }
            }

            if (needJFXAPI) {
                // the jfxrt.jar file
                // firstly we try the properties reference
                jfxPath = properties.getProperty("jfxPath");
                needJFXAPI = !Utils.checkPath(jfxPath);
                if (needJFXAPI) {
                    // If we can't get it from the properties file, we try to
                    // find it on the users operating system
                    jfxPath = getJFXClassPath();
                    needJFXAPI = !Utils.checkPath(jfxPath);
                }

                if (!needJFXAPI) {
                    updateClassPath(jfxPath);
                }
            }

            if (needAttachAPI || needJFXAPI) {

                /**
                 * This needs to be improved, in this situation we already have
                 * attachAPI but not because it was saved in the file, try to
                 * fill the path by finding it
                 */
                if (!needAttachAPI && (attachPath == null || attachPath.equals(""))) {
                    attachPath = getToolsClassPath();
                }
                /**
                 * This needs to be improved, in this situation we already have
                 * jfxAPI but not because it was saved in the file, try to fill
                 * the path by finding it
                 */
                if (!needJFXAPI && (jfxPath == null || jfxPath.equals(""))) {
                    jfxPath = getJFXClassPath();
                }

                final String _attachPath = attachPath;
                final String _jfxPath = jfxPath;

                ClassPathDialog.showDialog(_attachPath, _jfxPath, true, new PathChangeListener() {
                    @Override public void onPathChanged(final Map<String, URI> map) {
                        final URI toolsPath = map.get(PathChangeListener.TOOLS_JAR_KEY);
                        final URI jfxPath = map.get(PathChangeListener.JFXRT_JAR_KEY);
                        updateClassPath(toolsPath);
                        updateClassPath(jfxPath);
                        properties.setProperty(TOOLS_JAR_PATH_KEY, toolsPath.toASCIIString());
                        properties.setProperty(JFXRT_JAR_PATH_KEY, jfxPath.toASCIIString());
                        PropertiesUtils.saveProperties();
                        ClassPathDialog.hideDialog();
                        RemoteScenicViewImpl.start();
                    }
                });
            } else {
                RemoteScenicViewImpl.start();
            }
        }
    }

    private String getJFXClassPath() {
        final List<String> results = JfxrtFinder.findJfxrt();
        // // see if we can find JavaFX at the runtime path
        // String path = System.getProperty("javafx.runtime.path");
        // path = path == null ? "" : path;

        for (final String path : results) {
            if (path != null && new File(path).exists()) {
                // properties.setProperty(JFXRT_JAR_PATH_KEY, path);
                return path;
            }
        }

        return "";
    }

    private String getToolsClassPath() {
        final String javaHome = System.getProperty("java.home");
        if (!javaHome.contains("jdk")) {
            // JOptionPane.showMessageDialog(null, "No JDK found");
            System.out.println("Error: No JDK found on system");
            return null;
        }

        // This points to, for example, "C:\Program Files
        // (x86)\Java\jdk1.6.0_30\jre"
        // This is one level too deep. We want to pop up and then go into the
        // lib directory to find tools.jar
        System.out.println("JDK found at: " + javaHome);

        File toolsJar = new File(javaHome + "/../lib/tools.jar");
        if (!toolsJar.exists()) {
            // JOptionPane.showMessageDialog(null, "No tools.jar found at\n" +
            // toolsJar);
            boolean found = false;
            if (Utils.isMac()) {
                toolsJar = getToolsClassPathOnMAC();
                if (toolsJar != null) {
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Error: Can not find tools.jar on system - disabling VM lookup");
                return null;
            }
        }

        // System.out.println("Attempting to load tools.jar file from here:");
        // System.out.println(toolsJar.getAbsolutePath());

        final String path = toolsJar.getAbsolutePath();
        // properties.setProperty(TOOLS_JAR_PATH_KEY, path);

        return path;
    }

    private void updateClassPath(final String uriPath) {
        updateClassPath(Utils.toURI(uriPath));
    }

    private void updateClassPath(final URI uri) {
        try {
            final URL url = uri.toURL();
            System.out.println("Adding to classpath: " + url);
            ClassPathHacker.addURL(url);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean[] testClassPathRequirements() {
        boolean isAttachAPIAvailable = false;
        boolean isJFXAvailable = false;

        // Test if we can load a class from tools.jar
        try {
            Class.forName("com.sun.tools.attach.AttachNotSupportedException").newInstance();
            isAttachAPIAvailable = true;
        } catch (final Exception e) {
            System.out.println("Java Attach API was not found");
        }

        // Test if we can load a class from jfxrt.jar
        try {
            Class.forName("javafx.beans.property.SimpleBooleanProperty").newInstance();
            isJFXAvailable = true;
        } catch (final Exception e) {
            System.out.println("JavaFX API was not found");
        }

        return new boolean[] { isAttachAPIAvailable, isJFXAvailable };
    }

    private File getToolsClassPathOnMAC() {
        /**
         * Apparently tools.jar can be on: a) /Library/Java/JavaVirtualMachines/
         * b) System/Library/Java/JavaVirtualMachines/
         */
        File toolsFile = getToolsClassPathOnMAC(new File("/Library/Java/JavaVirtualMachines/"));
        if (toolsFile == null) {
            toolsFile = getToolsClassPathOnMAC(new File("/System/Library/Java/JavaVirtualMachines/"));
        }
        return toolsFile;
    }

    private File getToolsClassPathOnMAC(final File jvmsRoot) {
        System.out.println("Testing tools classPath on MAC:" + jvmsRoot.getAbsolutePath());
        final File[] jdks = jvmsRoot.listFiles(new FileFilter() {

            @Override public boolean accept(final File dir) {
                return (dir.isDirectory() && dir.getName().indexOf(".jdk") != -1);
            }
        });
        for (int i = 0; i < jdks.length; i++) {
            System.out.println("Valid JDKs:" + jdks[i].getName());
        }
        final String javaVersion = System.getProperty("java.version");
        /**
         * If we are using JDK6 using classes.jar otherwise tools.jar
         */
        if (javaVersion.indexOf("1.6") != -1) {
            for (int i = 0; i < jdks.length; i++) {
                if (jdks[i].getName().indexOf("1.6") != -1) {
                    final File classesFile = new File(jdks[i], "Contents/Classes/classes.jar");
                    if (classesFile.exists()) {
                        System.out.println("Classes file found on MAC in:" + classesFile.getAbsolutePath());
                        return classesFile;
                    }
                }
            }
        }
        if (javaVersion.indexOf("1.7") != -1) {
            for (int i = 0; i < jdks.length; i++) {
                if (jdks[i].getName().indexOf("1.7") != -1) {
                    final File toolsFile = new File(jdks[i], "Contents/Home/lib/tools.jar");
                    if (toolsFile.exists()) {
                        System.out.println("Tools file found on MAC in:" + toolsFile.getAbsolutePath());
                        return toolsFile;
                    }
                }
            }
        }
        return null;
    }
}
