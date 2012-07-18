/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javafx.experiments.scenicview.utils;

import java.io.File;
import java.net.*;
import java.util.logging.*;

/**
 * 
 * @author Jonathan
 */
class Utils {
    public static URI encodePath(final String path) {
        try {
            final URL url = new File(path).toURL();
            return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
        } catch (final URISyntaxException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (final MalformedURLException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean isMac() {

        final String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);

    }

    static boolean checkPath(final String path) {
        try {
            if (path != null && !path.equals("")) {
                if (new File(path).exists()) {
                    return true;
                } else if (new File(new URI(path)).exists()) {
                    return true;
                }
            }
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    static URI toURI(final String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
