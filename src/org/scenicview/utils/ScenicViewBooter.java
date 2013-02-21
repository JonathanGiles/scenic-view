/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.scenicview.utils;

import com.sun.tools.attach.spi.AttachProvider;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.stage.Stage;

import org.fxconnector.remote.FXConnectorFactory;
import org.scenicview.ScenicView;

/**
 * 
 */
public class ScenicViewBooter extends Application {

    public static final String TOOLS_JAR_PATH_KEY = "attachPath";

    private static boolean debug = false;

    private static void debug(final String log) {
        if (debug) {
            System.out.println(log);
        }
    }

    public static void main(final String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-debug")) {
                    debug = true;
                }
            }
        }
//        new ScenicViewBooter();
        launch(args);
    }

    private void activateDebug() {
        FXConnectorFactory.setDebug(debug);
        ScenicView.setDebug(debug);
    }

    private static Properties properties;
    
    private Stage stage;

    @Override public void start(final Stage stage) throws Exception {
        this.stage = stage;
//    private ScenicViewBooter() {
        // first we check if the classes are already on the classpath
        boolean isAttachAPIAvailable = false;
        boolean isJFXAvailable = false;

        // Test if we can load a class from tools.jar
        try {
            Class.forName("com.sun.tools.attach.AttachNotSupportedException").newInstance();
            isAttachAPIAvailable = true;
        } catch (final Exception e) {
            debug("Java Attach API was not found");
        }

        // Test if we can load a class from jfxrt.jar
        try {
            Class.forName("javafx.beans.property.SimpleBooleanProperty").newInstance();
            isJFXAvailable = true;
        } catch (final Exception e) {
            debug("JavaFX API was not found");
        }
        
        if (isAttachAPIAvailable && isJFXAvailable) {
            // Launch ScenicView directly
            startScenicView(null);
        } else {
            // If we are here, the classes are not on the classpath.
            // First, we read the properties file to find previous entries
            properties = PropertiesUtils.getProperties();

            String attachPath = "";

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
                    File attachFile = getToolsClassPath();
                    if (attachFile != null) {
                        attachPath = attachFile.getAbsolutePath();
                        needAttachAPI = !Utils.checkPath(attachPath);
                    }
                }

                if (!needAttachAPI) {
                    updateClassPath(attachPath);
                }
            }

            if (needJFXAPI) {
                // Fatal error - JavaFX should be on the classpath for all users
                // of Java 8.0 and above (which is what Scenic View 8.0 and above
                // targets.
                System.out.println("Error: JavaFX not found");
                System.exit(-1);
            }

            if (needAttachAPI || needJFXAPI) {

                /**
                 * This needs to be improved, in this situation we already have
                 * attachAPI but not because it was saved in the file, try to
                 * fill the path by finding it
                 */
                if (!needAttachAPI && (attachPath == null || attachPath.isEmpty())) {
                    File attachFile = getToolsClassPath();
                    if (attachFile != null) {
                        attachPath = attachFile.getAbsolutePath();
                    }
                }

                final String _attachPath = attachPath;
                if (Utils.isMac()) {
                    System.setProperty("javafx.macosx.embedded", "true");
                }

                final File toolsPathFile = new ClassPathDialog(_attachPath).show(stage);
                if (toolsPathFile != null) {
                    String toolsPath = toolsPathFile.getAbsolutePath();
                    updateClassPath(toolsPath);
                    properties.setProperty(ScenicViewBooter.TOOLS_JAR_PATH_KEY, toolsPath);
                    PropertiesUtils.saveProperties();
                    ScenicViewBooter.this.startScenicView(toolsPath);
                }
            } else {
                startScenicView(attachPath);
            }
        }
    }

    private void startScenicView(final String attachPath) {
        activateDebug();
        
        try {
            patchAttachLibrary(attachPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        setUserAgentStylesheet(STYLESHEET_MODENA);
        RemoteScenicViewLauncher launcher = new RemoteScenicViewLauncher();
        launcher.start(stage);
    }

    
    // TODO there is interesting code at the following URL for enumerating all
    // available AttachProviders:
    // http://stackoverflow.com/questions/11209267/dynamic-library-loading-with-pre-and-post-check-using-serviceloader
    private void patchAttachLibrary(final String attachPath) {
        if (attachPath != null /*&& Utils.isWindows()*/ && new File(attachPath).exists()) {
            final File jdkHome = new File(attachPath).getParentFile().getParentFile();

            try {
                System.loadLibrary("attach");
            } catch (final UnsatisfiedLinkError e) {
                /**
                 * Try to set or modify java.library.path
                 */
                final String path = jdkHome.getAbsolutePath() + File.separator + 
                                    "jre" + File.separator + 
                                    "bin;" + 
                                    System.getProperty("java.library.path");
                System.setProperty("java.library.path", path);

                try {
                    /**
                     * This code is need for reevaluating the library path
                     */
                    final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                    fieldSysPath.setAccessible(true);
                    fieldSysPath.set(null, null);
                    System.loadLibrary("attach");
                } catch (final Throwable e2) {
                    ExceptionLogger.submitException(e2);
                    System.out.println("Error while trying to put attach.dll in path");
                }
            }
        }
    }
    
    private void updateClassPath(final String uriPath) {
        updateClassPath(Utils.toURI(uriPath));
    }

    private void updateClassPath(final URI uri) {
        try {
            final URL url = uri.toURL();
            debug("Adding to classpath: " + url);
            ClassPathHacker.addURL(url);
        } catch (final IOException ex) {
            ExceptionLogger.submitException(ex);
        }
    }

    
    
    /***************************************************************************
     *                                                                         *
     * Attach API Handling                                                     *
     *                                                                         *
     **************************************************************************/
    
    private File getToolsClassPath() {
        File toolsJarFile = doBasicToolsSearch();
        if (toolsJarFile != null && toolsJarFile.exists()) {
            return toolsJarFile;
        }
        
        if (Utils.isWindows()) {
            // go down windows special path
        } else if (Utils.isMac()) {
            // go down mac special path
            toolsJarFile = getToolsClassPathOnMAC();
            if (toolsJarFile != null && toolsJarFile.exists()) {
                return toolsJarFile;
            }
        }
        
        return null;
    }
    
    private File doBasicToolsSearch() {
        final String javaHome = System.getProperty("java.home");
        if (! isJDKFolder(javaHome)) {
            System.out.println("Error: No JDK found on system");
            return null;
        }

        // This points to, for example, "C:\Program Files
        // (x86)\Java\jdk1.6.0_30\jre"
        // This is one level too deep. We want to pop up and then go into the
        // lib directory to find tools.jar
        debug("JDK found at: " + javaHome);

        File toolsJar = new File(javaHome + "/../lib/tools.jar");
        return toolsJar.exists() ? toolsJar : null;
    }
    
    private boolean isJDKFolder(String path) {
        if (path == null) return false;
        File f = new File(path);
        return f.exists() && f.isDirectory() && f.getName().contains("jdk");
    }

    private File getToolsClassPathOnMAC() {
        Process process = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("/usr/libexec/java_home -V");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        try(
            InputStream inputStream = process.getErrorStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            if (bufferedReader.ready()) {
                bufferedReader.readLine();
            }
            while (bufferedReader.ready()) {
                String versionString = bufferedReader.readLine();
                versionString = versionString.trim();
                
                String version;
                String name;
                String path;
                String[] splitted = versionString.split(" ");
                if (splitted.length == 3) {
                    version = splitted[0];
                    name = splitted[1];
                    path = splitted[2];
                    
                    if (version.endsWith(":")) {
                        version = version.substring(0, version.length() - 1);
                    }
//                    versions.add(new JavaVersion(splitted[0], splitted[1], splitted[2]));
                    
                    final File toolsFile = new File(path, "Contents/Home/lib/tools.jar");
                    if (toolsFile.exists()) {
                        debug("Tools file found on Mac OS in:" + toolsFile.getAbsolutePath());
                        return toolsFile;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
