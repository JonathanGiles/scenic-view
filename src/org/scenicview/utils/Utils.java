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

import java.io.File;
import java.net.*;
import java.util.logging.*;

/**
 * 
 */
public class Utils {
    public static URI encodePath(final String path) {
        try {
            @SuppressWarnings("deprecation") final URL url = new File(path).toURL();
            return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
        } catch (final URISyntaxException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (final MalformedURLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean checkPath(final String path) {
//        try {
            if (path != null && !path.equals("")) {
                File file = new File(path);
                if (file.exists()) {
                    return true;
//                } else if (new File(new URI(path)).exists()) {
//                    return true;
                }
            }
//        } catch (final URISyntaxException e) {
//            ExceptionLogger.submitException(e);
//        }
        return false;
    }

    public static URI toURI(final String uri) {
        try {
            if (new File(uri).exists()) {
                return encodePath(new File(uri).getAbsolutePath());
            }
            return new URI(uri);
        } catch (final URISyntaxException e) {
            ExceptionLogger.submitException(e);
            return null;
        }
    }

}
