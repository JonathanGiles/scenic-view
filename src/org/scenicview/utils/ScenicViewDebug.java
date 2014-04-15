package org.scenicview.utils;



public class ScenicViewDebug {
    
    private static boolean debug = false;
    
    public static void setDebug(final boolean v) {
        debug = v;
    }

    public static void print(final String text) {
        if (debug) {
            System.out.println(text);
        }
    }
}
