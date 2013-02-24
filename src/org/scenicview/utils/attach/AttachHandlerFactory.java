/*
 * Copyright (c) 2013 Oracle and/or its affiliates.
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
package org.scenicview.utils.attach;

import org.scenicview.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.scenicview.utils.ClassPathDialog;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.PropertiesUtils;
import org.scenicview.utils.ScenicViewBooter;
import static org.scenicview.utils.ScenicViewBooter.JDK_PATH_KEY;

/**
 *
 */
public class AttachHandlerFactory {
    
    private static Properties properties;
    private static AttachHandler attachHandler;

    private AttachHandlerFactory() {
        // no-op
    }

    public static void initAttachAPI(final Stage stage) {
        // first we check if the classes are already on the classpath
        boolean isAttachAPIAvailable = AttachHandlerFactory.isAttachAvailable();
        if (isAttachAPIAvailable) return;
        
        // we read the properties file to find previous entries
        properties = PropertiesUtils.getProperties();

//        String jdkHome = "";
        JDKToolsJarPair jdkHome = null;

        boolean needAttachAPI = true;

        if (needAttachAPI) {
            AttachHandler attachHandler = getAttachHandler();
            
            // firstly we try the properties reference
            String jdpPathPropertyValue = properties.getProperty(JDK_PATH_KEY);
            jdkHome = jdpPathPropertyValue == null ? null : new JDKToolsJarPair(jdpPathPropertyValue);
            needAttachAPI = jdkHome == null || !Utils.checkPath(jdkHome.getJdkPath().getAbsolutePath());
            if (needAttachAPI) {
                // If we can't get it from the properties file, we try to
                // find it on the users operating system
                List<JDKToolsJarPair> jdkPaths = new ArrayList<>();
                attachHandler.getOrderedJDKPaths(jdkPaths);
                
                // TODO we should handle this better!
                if (! jdkPaths.isEmpty()) {
                    JDKToolsJarPair jdkPathFile = jdkPaths.get(0);
                    if (jdkPathFile != null) {
                        jdkHome = jdkPathFile;
                        needAttachAPI = !Utils.checkPath(jdkHome.getJdkPath().getAbsolutePath());
                    }
                }
            }

            if (!needAttachAPI) {
                addToolsJarToClasspath(jdkHome);
            }
        }

        if (needAttachAPI) {
            /**
             * This needs to be improved, in this situation we already have
             * attachAPI but not because it was saved in the file, try to
             * fill the path by finding it
             */
            if (!needAttachAPI && jdkHome == null) {
                List<JDKToolsJarPair> jdkPaths = new ArrayList<>();
                attachHandler.getOrderedJDKPaths(jdkPaths);
                
                // TODO we should handle this better!
                if (! jdkPaths.isEmpty()) {
                    JDKToolsJarPair jdkPathFile = jdkPaths.get(0);
                    if (jdkPathFile != null) {
                        jdkHome = jdkPathFile;
                    }
                }
            }

//            final String _attachPath = jdkHome;
            if (com.sun.javafx.Utils.isMac()) {
                System.setProperty("javafx.macosx.embedded", "true");
            }

            final File jdkPathFile = new ClassPathDialog(null).show(stage);
            if (jdkPathFile != null) {
                String jdkPath = jdkPathFile.getAbsolutePath();
                if (jdkPath == null || jdkPath.isEmpty()) {
                    Platform.exit();
                }
                jdkHome = new JDKToolsJarPair(jdkPath);
                addToolsJarToClasspath(jdkHome);
                properties.setProperty(ScenicViewBooter.JDK_PATH_KEY, jdkPath);
                PropertiesUtils.saveProperties();
            }
        }
        
        try {
            patchAttachLibrary(jdkHome);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static boolean isAttachAvailable() {
        // Test if we can load a class from tools.jar
        try {
            Class.forName("com.sun.tools.attach.AttachNotSupportedException").newInstance();
            return true;
        } catch (final Exception e) {
            debug("Java Attach API was not found");
            return false;
        }
    }
    
    private static AttachHandler getAttachHandler() {
        if (attachHandler == null) {
            if (com.sun.javafx.Utils.isWindows()) {
                attachHandler = new WindowsAttachHandler();
            } else if (com.sun.javafx.Utils.isMac()) {
                attachHandler = new MacAttachHandler();
            } else {
                // TODO handle alternate operating systems like Linux, etc
            }
        }
        return attachHandler;
    }
    
    // TODO there is interesting code at the following URL for enumerating all
    // available AttachProviders:
    // http://stackoverflow.com/questions/11209267/dynamic-library-loading-with-pre-and-post-check-using-serviceloader
    private static void patchAttachLibrary(final JDKToolsJarPair jdkHome) {
        File jdkHomeFile = jdkHome.getJdkPath();

        try {
            System.loadLibrary("attach");
        } catch (final UnsatisfiedLinkError e) {
            /**
             * Try to set or modify java.library.path
             */
            final String path = jdkHomeFile.getAbsolutePath() + File.separator + 
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
    
    private static void addToolsJarToClasspath(final JDKToolsJarPair jdkPath) {
        try {
            final URL url = Utils.toURI(jdkPath.getToolsPath(getAttachHandler()).getAbsolutePath()).toURL();
            debug("Adding to classpath: " + url);

            final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            final Class<?> sysclass = URLClassLoader.class;
        
            @SuppressWarnings("unchecked") final Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static void doBasicJdkSearch(List<JDKToolsJarPair> jdkPaths) {
        final String javaHome = System.getProperty("java.home");
        if (! isJDKFolder(javaHome)) {
            System.out.println("Error: No JDK found on system");
            return;
        }

        // This points to, for example, "C:\Program Files
        // (x86)\Java\jdk1.6.0_30\jre"
        // This is one level too deep. We want to pop up and then go into the
        // lib directory to find tools.jar
        debug("JDK found at: " + javaHome);

        File jdkHome = new File(javaHome);// + "/../lib/tools.jar");
        if (jdkHome.exists()) {
            jdkPaths.add(new JDKToolsJarPair(jdkHome));
        }
    }
    
    static boolean isJDKFolder(String path) {
        if (path == null) return false;
        File f = new File(path);
        return f.exists() && f.isDirectory() && f.getName().contains("jdk");
    }

    private static void debug(final String log) {
//        if (debug) {
//            System.out.println(log);
//        }
    }
}
