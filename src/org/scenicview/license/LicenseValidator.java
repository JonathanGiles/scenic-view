package org.scenicview.license;

import java.io.File;
import java.util.Map;

/**
 * A license file is just a text file containing relevant metadata that has a 
 * license key included. The license key is generated based on a hash of the 
 * file and a private password as a salt
 */
public class LicenseValidator {
    
    public static boolean validate(File licenseFile) {
        if (! licenseFile.exists()) {
            return false;
        }
        
        // read in the license file as a string
        String string = LicenseUtils.readFile(licenseFile, true);
        
        // get the license hash
        int licensePropertyStartPos = string.indexOf(LicenseUtils.LICENSE_PROPERTY);
        int hashStartPos = licensePropertyStartPos + LicenseUtils.LICENSE_PROPERTY.length() + 1;
        int hashEndPos = Math.max(string.indexOf("\n", hashStartPos), string.length());
        String expectedHash = string.substring(hashStartPos, hashEndPos);
        
        // get the string without the license line at all
        String propertiesOnlyString = string.substring(0, licensePropertyStartPos) + string.substring(hashEndPos);
        propertiesOnlyString = propertiesOnlyString.trim();
        
        String actualHash = LicenseUtils.computeHash(propertiesOnlyString);
        
        return expectedHash.equals(actualHash);
    }
    
    public static Map<String,String> getLicenseProperties(File licenseFile) {
        return LicenseUtils.getProperties(licenseFile);
    }
}