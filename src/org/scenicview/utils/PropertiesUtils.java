/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.scenicview.utils;

import java.io.*;
import java.util.Properties;

public class PropertiesUtils {

    private static final String SCENIC_VIEW_PROPERTIES_FILE = "scenicView.properties";
    
    private static Properties properties;

    private PropertiesUtils() {
        
    }
    
    public static Properties getProperties() {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties;
    }
    
    public static boolean containsKey(String key) {
        return getProperties().contains(key);
    }

    private static Properties loadProperties() {
        Properties _properties = new Properties();
        
        try {
            final File propertiesFile = new File(SCENIC_VIEW_PROPERTIES_FILE);
            if (propertiesFile.exists()) {
                final FileInputStream in = new FileInputStream(propertiesFile);
                try {
                    _properties.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (final Exception e) {
            ScenicViewDebug.print("Error while loading preferences");
        }
        return _properties;
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
            ExceptionLogger.submitException(e, "Error while saving preferences");
        }
    }

}
