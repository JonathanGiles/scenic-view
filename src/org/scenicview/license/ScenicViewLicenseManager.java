package org.scenicview.license;

import java.io.File;
import java.util.Map;

import org.scenicview.utils.ScenicViewDebug;

public class ScenicViewLicenseManager {

    private static enum Mode {
        FREE,
        PAID
    }

    private static final Mode scenicViewMode;

    private static final File LICENSE_FILE = new File("license.key");

    private static Map<String,String> licenseProperties;

    static {
        // check for and load license file
        boolean isLicensed = LicenseValidator.validate(LICENSE_FILE);
        scenicViewMode = isLicensed ? Mode.PAID : Mode.FREE;
        if (isLicensed) {
            ScenicViewDebug.print("Found license, loading license properties:");
            licenseProperties = LicenseValidator.getLicenseProperties(LICENSE_FILE);
            licenseProperties.forEach((key, value) -> {
                ScenicViewDebug.print("    " + key + " = " + value);
            });
        }
    }

    public static void start() {
        // no-op
    }

    public static boolean isPaid() {
        // for now, everyone gets all the features! :-)
        return true;
        //return scenicViewMode == Mode.PAID;
    }
    
    public static Map<String, String> getLicenseProperties() {
        return licenseProperties;
    }
}
