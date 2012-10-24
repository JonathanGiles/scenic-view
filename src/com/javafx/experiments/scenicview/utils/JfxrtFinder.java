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
package com.javafx.experiments.scenicview.utils;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class JfxrtFinder {
    private static boolean verbose = false;

    // JavaFX family version that this Launcher is compatible with
    private static final String JAVAFX_FAMILY_VERSION = "2.";

    // Minimum JavaFX version required to run the app
    // (keep separate from JAVAFX_FAMILY_VERSION check as
    // we want 2.2.1 SDK to be ok to run app that needs 2.1.0
    // and prefix based match is not enough)
    // TODO: refactor this to have version to be supplied from the build
    // environment
    // (but we do NOT want another class or property file in the app bundle!)
    // (any other options besides java source code preprocessing?)
    private static final String JAVAFX_REQUIRED_VERSION = "2.1.0";

    private static final String ZERO_VERSION = "0.0.0";

    private static URL fileToURL(final File file) throws IOException {
        return file.getCanonicalFile().toURI().toURL();
    }

    private static String findLaunchMethodInJar(final String jfxRtPathName) {
        final File jfxRtPath = new File(jfxRtPathName);

        // Verify that we can read <jfxRtPathName>/lib/jfxrt.jar
        final File jfxRtLibPath = new File(jfxRtPath, "lib");
        final File jfxRtJar = new File(jfxRtLibPath, "jfxrt.jar");
        if (!jfxRtJar.canRead()) {
            if (verbose) {
                System.err.println("Unable to read " + jfxRtJar.toString());
            }
            return null;
        }

        return jfxRtJar.toString();
    }

    // convert version string in the form of x.y.z into int array of (x,y.z)
    // return the array if version string can be converted.
    // otherwise retun null
    private static int[] convertVersionStringtoArray(final String version) {
        final int[] v = new int[3];
        if (version == null)
            return null;

        final String s[] = version.split("\\.");
        if (s.length == 3) {
            v[0] = Integer.parseInt(s[0]);
            v[1] = Integer.parseInt(s[1]);
            v[2] = Integer.parseInt(s[2]);
            return v;
        }
        // version string passed in is bad
        return null;
    }

    // compare the two version array a1 and a2
    // return 0 if the two array contains the same version information
    // (or both are invalid version specs)
    // return 1 if a2 is greater than a1
    // return -1 if a2 is less than a1
    private static int compareVersionArray(final int[] a1, final int[] a2) {
        final boolean isValid1 = (a1 != null) && (a1.length == 3);
        final boolean isValid2 = (a2 != null) && (a2.length == 3);

        // both bad
        if (!isValid1 && !isValid2) {
            return 0;
        }

        // a2 < a1
        if (!isValid2) {
            return -1;
        }

        // a2 > a1
        if (!isValid1) {
            return 1;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a2[i] > a1[i])
                return 1;
            if (a2[i] < a1[i])
                return -1;
        }

        return 0;
    }

    /**
     * If we are on Windows, look in the system registry for the installed
     * JavaFX runtime.
     * 
     * @return the path to the JavaFX Runtime or null
     */

    private static String lookupRegistry() {
        if (!System.getProperty("os.name").startsWith("Win")) {
            return null;
        }

        final String javaHome = System.getProperty("java.home");
        if (verbose) {
            System.err.println("java.home = " + javaHome);
        }
        if (javaHome == null || javaHome.equals("")) {
            return null;
        }

        try {
            // Load deploy.jar, get a Config instance and load the native
            // libraries; then load the windows registry class and lookup
            // the method to get the windows registry entry

            final File jreLibPath = new File(javaHome, "lib");
            final File deployJar = new File(jreLibPath, "deploy.jar");

            final URL[] urls = new URL[] { fileToURL(deployJar) };
            if (verbose) {
                System.err.println(">>>> URL to deploy.jar = " + urls[0]);
            }

            final ClassLoader deployClassLoader = new URLClassLoader(urls, null);

            try {
                // Load and initialize the native deploy library, ignore
                // exception
                final String configClassName = "com.sun.deploy.config.Config";
                final Class configClass = Class.forName(configClassName, true, deployClassLoader);
                Method m = configClass.getMethod("getInstance", null);
                final Object config = m.invoke(null, null);
                m = configClass.getMethod("loadDeployNativeLib", null);
                m.invoke(config, null);
            } catch (final Exception ex) {
                // Ignore any exception, since JDK7 no longer has this method
            }

            final String winRegistryWrapperClassName = "com.sun.deploy.association.utility.WinRegistryWrapper";

            final Class winRegistryWrapperClass = Class.forName(winRegistryWrapperClassName, true, deployClassLoader);

            final Method mGetSubKeys = winRegistryWrapperClass.getMethod("WinRegGetSubKeys", new Class[] { Integer.TYPE, String.class, Integer.TYPE });

            final Field HKEY_LOCAL_MACHINE_Field2 = winRegistryWrapperClass.getField("HKEY_LOCAL_MACHINE");
            final int HKEY_LOCAL_MACHINE2 = HKEY_LOCAL_MACHINE_Field2.getInt(null);
            final String registryKey = "Software\\Oracle\\JavaFX\\";

            // Read the registry and find all installed JavaFX runtime versions
            // under HKLM\Software\Oracle\JavaFX\
            final String[] fxVersions = (String[]) mGetSubKeys.invoke(null, new Object[] { new Integer(HKEY_LOCAL_MACHINE2), registryKey, new Integer(255) });

            if (fxVersions == null) {
                // No JavaFX runtime installed in the system
                return null;
            }
            String version = ZERO_VERSION;
            // Iterate thru all installed JavaFX runtime verions in the system
            for (int i = 0; i < fxVersions.length; i++) {
                // get the latest version that is compatibible with the
                // launcher JavaFX family version and meets minimum version
                // requirement
                if (fxVersions[i].startsWith(JAVAFX_FAMILY_VERSION) && fxVersions[i].compareTo(JAVAFX_REQUIRED_VERSION) >= 0) {
                    final int[] v1Array = convertVersionStringtoArray(version);
                    final int[] v2Array = convertVersionStringtoArray(fxVersions[i]);
                    if (compareVersionArray(v1Array, v2Array) > 0) {
                        version = fxVersions[i];
                    }
                } else {
                    if (verbose) {
                        System.err.println("  Skip version " + fxVersions[i] + " (required=" + JAVAFX_REQUIRED_VERSION + ")");
                    }
                }
            }

            if (version.equals(ZERO_VERSION)) {
                // No installed JavaFX runtime compatible with this Launcher
                return null;
            }

            // Read the registry entry for: Software\Oracle\JavaFX\<version>
            final String winRegistryClassName = "com.sun.deploy.util.WinRegistry";
            final Class winRegistryClass = Class.forName(winRegistryClassName, true, deployClassLoader);
            final Method mGet = winRegistryClass.getMethod("getString", new Class[] { Integer.TYPE, String.class, String.class });
            final Field HKEY_LOCAL_MACHINE_Field = winRegistryClass.getField("HKEY_LOCAL_MACHINE");
            final int HKEY_LOCAL_MACHINE = HKEY_LOCAL_MACHINE_Field.getInt(null);
            final String path = (String) mGet.invoke(null, new Object[] { new Integer(HKEY_LOCAL_MACHINE), registryKey + version, "Path" });
            if (verbose) {
                System.err.println("FOUND KEY: " + registryKey + version + " = " + path);
            }
            return path;
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static List<String> findJfxrt() {
        final List<String> jfxrtFiles = new ArrayList<String>();

        String javafxRuntimePath = System.getProperty("javafx.runtime.path");
        if (javafxRuntimePath != null) {
            if (verbose) {
                System.err.println("    javafx.runtime.path = " + javafxRuntimePath);
            }
            jfxrtFiles.add(javafxRuntimePath);
        }

        if (verbose) {
            System.err.println("3) Look for cobundled JavaFX ... " + "[java.home=" + System.getProperty("java.home"));
        }
        javafxRuntimePath = findLaunchMethodInJar(System.getProperty("java.home"));
        if (javafxRuntimePath != null) {
            jfxrtFiles.add(javafxRuntimePath);
        }

        // Check the platform registry for this architecture.
        if (verbose) {
            System.err.println("4) Look in the OS platform registry...");
        }
        javafxRuntimePath = lookupRegistry();
        if (javafxRuntimePath != null) {
            if (verbose) {
                System.err.println("    Installed JavaFX runtime found in: " + javafxRuntimePath);
            }
            javafxRuntimePath = findLaunchMethodInJar(javafxRuntimePath);
            jfxrtFiles.add(javafxRuntimePath);
        }

        // TODO: remove the following hard-coded paths; they are there so that
        // the
        // apps/experiments will work without having JavaFX pre-installed (or on
        // the Mac)

        // Check the platform registry for this architecture.
        if (verbose) {
            System.err.println("5) Look in hardcoded paths");
        }
        final String[] hardCodedPaths = { "../rt", "../../../../rt", /*
                                                                      * this is
                                                                      * for
                                                                      * sample
                                                                      * code in
                                                                      * the
                                                                      * sdk/apps
                                                                      * /src/
                                                                      * BrickBreaker
                                                                      * /dist
                                                                      */
        "../../sdk/rt", "../../../artifacts/sdk/rt" };

        for (int i = 0; i < hardCodedPaths.length; i++) {
            javafxRuntimePath = hardCodedPaths[i];
            javafxRuntimePath = findLaunchMethodInJar(javafxRuntimePath);
            if (javafxRuntimePath != null) {
                jfxrtFiles.add(javafxRuntimePath);
            }
        }

        return jfxrtFiles;
    }
}
