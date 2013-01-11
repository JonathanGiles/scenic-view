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

import java.io.File;
import java.net.*;
import java.util.logging.*;

/**
 * 
 */
class Utils {
    public static URI encodePath(final String path) {
        try {
            @SuppressWarnings("deprecation") final URL url = new File(path).toURL();
            return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
        } catch (final URISyntaxException ex) {
            Logger.getLogger(SwingClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (final MalformedURLException ex) {
            Logger.getLogger(SwingClassPathDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean isMac() {

        final String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isWindows() {

        final String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

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
            ExceptionLogger.submitException(e);
        }
        return false;
    }

    static URI toURI(final String uri) {
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
