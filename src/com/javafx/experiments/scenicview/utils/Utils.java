/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javafx.experiments.scenicview.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonathan
 */
public class Utils {
    public static URI encodePath(String path) {
        try {
            URL url = new File(path).toURL();
            return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
