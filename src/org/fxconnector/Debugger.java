package org.fxconnector;

public class Debugger {
    
    private static boolean debug = false;
    
    private Debugger() { }

    public static void setDebug(boolean debug) {
        Debugger.debug = debug;
    }
    
    public static void debug(Object msg) {
        if (debug) {
            System.out.println(msg);
        }
    }
    
    public static boolean isDebug() {
        return debug;
    }
}
