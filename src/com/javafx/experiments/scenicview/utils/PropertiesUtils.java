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
