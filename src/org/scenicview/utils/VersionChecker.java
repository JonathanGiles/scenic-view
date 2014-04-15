/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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