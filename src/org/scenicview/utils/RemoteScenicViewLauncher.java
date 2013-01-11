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

import java.rmi.RemoteException;

import javafx.stage.Stage;

import org.fxconnector.remote.FXConnectorFactory;
import org.scenicview.ScenicView;
import org.scenicview.update.RemoteVMsUpdateStrategy;
import com.sun.javafx.application.PlatformImpl;

public class RemoteScenicViewLauncher {

    private static ScenicView view;

    private RemoteScenicViewLauncher() {
    }

    public static void start() {
        final RemoteVMsUpdateStrategy strategy = new RemoteVMsUpdateStrategy();
        PlatformImpl.startup(new Runnable() {

            @Override public void run() {
                FXConnectorFactory.debug("Platform running");
                final Stage stage = new Stage();
                // workaround for RT-10714
                stage.setWidth(640);
                stage.setHeight(800);
                stage.setTitle("Scenic View v" + ScenicView.VERSION);
                FXConnectorFactory.debug("Launching ScenicView v" + ScenicView.VERSION);
                view = new ScenicView(strategy, stage);
                ScenicView.show(view, stage);
            }
        });
        FXConnectorFactory.debug("Startup done");
        while (view == null) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                ExceptionLogger.submitException(e);
            }
        }

        FXConnectorFactory.debug("Creating server");
        try {
            strategy.setFXConnector(FXConnectorFactory.getConnector());
        } catch (final RemoteException e1) {
            ExceptionLogger.submitException(e1);
        }
        FXConnectorFactory.debug("Server done");
    }
}
