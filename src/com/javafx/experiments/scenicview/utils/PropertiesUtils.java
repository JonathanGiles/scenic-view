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
import java.util.Properties;

public class PropertiesUtils {

    private static final String SCENIC_VIEW_PROPERTIES_FILE = "scenicView.properties";
    
    private static Properties properties;

    private PropertiesUtils() {
        
    }

    public static Properties loadProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        
        try {
            final File propertiesFile = new File(SCENIC_VIEW_PROPERTIES_FILE);
            if (propertiesFile.exists()) {
                final FileInputStream in = new FileInputStream(propertiesFile);
                try {
                    properties.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (final Exception e) {
            System.out.println("Error while loading preferences");
        }
        return properties;
    }

    public static void saveProperties() {
        try {
            final File propertiesFile = new File(SCENIC_VIEW_PROPERTIES_FILE);
            final FileOutputStream out = new FileOutputStream(propertiesFile);
            try {
                properties.store(out, "ScenicView properties");
            } finally {
                out.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("Error while saving preferences");
        }
    }

}
