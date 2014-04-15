package org.scenicview.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class LicenseUtils {
    
    static final String LICENSE_PROPERTY = "license";

    static Map<String, String> getProperties(File licenseFile) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(licenseFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String,String> propertiesMap = new HashMap<>();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            if (LICENSE_PROPERTY.equals(entry.getKey())) continue;
            propertiesMap.put((String)entry.getKey(), (String)entry.getValue());
        }
        return propertiesMap;
    }

    static String readFile(File file, boolean ignoreCommentLines) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
    
            while((line = reader.readLine()) != null) {
                if (ignoreCommentLines && line.startsWith("#")) {
                    continue;
                }
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
    
            return stringBuilder.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "";
    }

    static String computeHash(String string) {
        System.out.println("Computing hash on '" + string + "'");
        try {
            // create a hash of the license file
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            
            messageDigest.reset();
            byte[] buffer = string.getBytes();
            messageDigest.update(buffer);
            byte[] digest = messageDigest.digest();
    
            // Convert the byte to hex format
            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
    
            return hexStr;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        return "";
    }
}
