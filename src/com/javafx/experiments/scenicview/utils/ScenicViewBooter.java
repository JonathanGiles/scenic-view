package com.javafx.experiments.scenicview.utils;

import java.io.*;
import java.util.Properties;

import com.javafx.experiments.scenicview.connector.remote.RemoteScenicViewImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 
 * @author Jonathan
 */
public class ScenicViewBooter {
    
    public static final String TOOLS_JAR_PATH_KEY = "attachPath";
    public static final String JFXRT_JAR_PATH_KEY = "jfxPath";

    public static void main(final String[] args) {
        new ScenicViewBooter();
    }

    private static Properties properties;

    private ScenicViewBooter() {
        // first we check if the classes are already on the classpath
        boolean[] checks = testClassPathRequirements();
        boolean isAttachAPIAvailable = checks[0];
        boolean isJFXAvailable = checks[1];

        if (isAttachAPIAvailable && isJFXAvailable) {
            // Launch ScenicView directly
            RemoteScenicViewImpl.start();
        } else {
            // If we are here, the classes are not on the classpath.
            // First, we read the properties file to find previous entries
            properties = PropertiesUtils.loadProperties();
            
            String attachPath = "";
            String jfxPath = "";
            
            boolean needAttachAPI = ! isAttachAPIAvailable;
            boolean needJFXAPI = ! isJFXAvailable;

            if (needAttachAPI) {
                // the tools.jar file
                // firstly we try the properties reference
                attachPath = properties.getProperty(TOOLS_JAR_PATH_KEY);
                needAttachAPI = ! checkPath(attachPath);
                if (needAttachAPI) {
                    // If we can't get it from the properties file, we try to 
                    // find it on the users operating system
                    attachPath = getToolsClassPath();
                    needAttachAPI = ! checkPath(attachPath);
                }
                
                if (! needAttachAPI) {
                    updateClassPath(attachPath);
                }
            }
            
            if (needJFXAPI) {
                // the jfxrt.jar file
                // firstly we try the properties reference
                jfxPath = properties.getProperty("jfxPath");
                needJFXAPI = ! checkPath(jfxPath);
                if (needJFXAPI) {
                    // If we can't get it from the properties file, we try to 
                    // find it on the users operating system
                    jfxPath = getJFXClassPath();
                    needJFXAPI = ! checkPath(jfxPath);
                }
                
                if (! needJFXAPI) {
                    updateClassPath(jfxPath);
                }
            }
            
            if (needAttachAPI || needJFXAPI) {
                ClassPathDialog dialog = new ClassPathDialog(attachPath, jfxPath, true);
                dialog.setPathChangeListener(new PathChangeListener() {
                    public void onPathChanged(Map<String, URI> map) {
                        URI toolsPath = map.get(PathChangeListener.TOOLS_JAR_KEY);
                        URI jfxPath = map.get(PathChangeListener.JFXRT_JAR_KEY);
                        updateClassPath(toolsPath);
                        updateClassPath(jfxPath);
                        properties.setProperty(TOOLS_JAR_PATH_KEY, toolsPath.toASCIIString());
                        properties.setProperty(JFXRT_JAR_PATH_KEY, jfxPath.toASCIIString());
                        PropertiesUtils.saveProperties();
                        RemoteScenicViewImpl.start();
                    }
                });
                dialog.setVisible(true);
            } else {
                RemoteScenicViewImpl.start();
            }
        }
    }

    private boolean checkPath(final String path) {
        try {
            if (path != null && !path.equals("")) {
                if (new File(path).exists()) {
                    return true;
                } else if (new File(new URI(path)).exists()) {
                    return true;
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getJFXClassPath() {
        // see if we can find JavaFX at the runtime path
        String path = System.getProperty("javafx.runtime.path");
        path = path == null ? "" : path;
        
        properties.setProperty(JFXRT_JAR_PATH_KEY, path);
        return path; 
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

//        System.out.println("Attempting to load tools.jar file from here:");
//        System.out.println(toolsJar.getAbsolutePath());

        String path = toolsJar.getAbsolutePath();
        properties.setProperty(TOOLS_JAR_PATH_KEY, path);
        
        return path;
    }
    
    private void updateClassPath(final String uriPath) {
        try {
            updateClassPath(new URI(uriPath));
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateClassPath(final URI uri) {
        try {
            URL url = uri.toURL();
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
            System.out.println("tools.jar not found");
        }
        
        // Test if we can load a class from jfxrt.jar
        try {
            Class.forName("javafx.beans.property.SimpleBooleanProperty").newInstance();
            isJFXAvailable = true;
        } catch (final Exception e) {
            System.out.println("jfxrt.jar not found");
        }
        
        return new boolean[] { isAttachAPIAvailable, isJFXAvailable};
    }
}
