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
import java.net.*;

public class VersionChecker {

    public static String checkVersion(final String actual) {
        URL u;
        InputStream is = null;
        final StringBuilder s = new StringBuilder();
        final byte[] buffer = new byte[256];

        try {
            u = new URL("http://jonathangiles.net/scenicView/latestVersion.txt");
            is = u.openStream(); // throws an IOException
            int read;

            while ((read = is.read(buffer)) != -1) {
                s.append(new String(buffer, 0, read, "UTF-8"));
            }
            final String info = s.toString();
            if (info.indexOf(actual) == -1) {
                return info;
            }
        } catch (final MalformedURLException mue) {
            ExceptionLogger.submitException(mue);
        } catch (final IOException ioe) {
            ScenicViewDebug.print("Cannot check last version");
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (final IOException ioe) {
                // just going to ignore this one
            }
        }
        return null;
    }

}