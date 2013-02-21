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

import com.sun.tools.attach.spi.AttachProvider;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.application.Application.setUserAgentStylesheet;
import javafx.stage.Stage;

import org.fxconnector.remote.FXConnectorFactory;
import org.scenicview.ScenicView;
import org.scenicview.utils.attach.AttachHandlerFactory;

/**
 * 
 */
public class ScenicViewBooter extends Application {

    public static final String JDK_PATH_KEY = "jdkPath";

    private static boolean debug = false;

    private static void debug(final String log) {
        if (debug) {
            System.out.println(log);
        }
    }

    public static void main(final String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-debug")) {
                    debug = true;
                }
            }
        }
        launch(args);
    }

    private void activateDebug() {
        FXConnectorFactory.setDebug(debug);
        ScenicView.setDebug(debug);
    }

    private Stage stage;

    @Override public void start(final Stage stage) throws Exception {
        this.stage = stage;

        // Test if we can load a class from jfxrt.jar
        try {
            Class.forName("javafx.beans.property.SimpleBooleanProperty").newInstance();
        } catch (final Exception e) {
            // Fatal error - JavaFX should be on the classpath for all users
            // of Java 8.0 and above (which is what Scenic View 8.0 and above
            // targets.
            System.out.println("Error: JavaFX not found");
            System.exit(-1);
        }
        
        AttachHandlerFactory.initAttachAPI(stage);
        
        activateDebug();
        
        setUserAgentStylesheet(STYLESHEET_MODENA);
        RemoteScenicViewLauncher launcher = new RemoteScenicViewLauncher();
        launcher.start(stage);
    }
}
