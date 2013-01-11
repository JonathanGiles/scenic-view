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
 package org.scenicview.control;

import javafx.animation.*;
import javafx.beans.value.*;
import javafx.concurrent.Worker.State;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class ProgressWebView extends StackPane {

    WebView wview;
    String loadedPage;

    public ProgressWebView() {
        wview = new WebView();
        final ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(300, 300);
        wview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            Animation anim;

            @Override public void changed(final ObservableValue<? extends State> arg0, final State old, final State newValue) {
                if (newValue == State.READY) {
                    anim = TimelineBuilder.create().keyFrames(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {
                        @Override public void handle(final ActionEvent arg0) {
                            if (wview.getEngine().getLoadWorker().getProgress() == -1) {
                                doLoad(loadedPage);
                            }
                        }
                    })).build();
                    anim.play();
                } else if (anim != null) {
                    anim.stop();
                    anim = null;
                }
            }
        });
        wview.getEngine().getLoadWorker().progressProperty().addListener(new ChangeListener<Number>() {
            private final double DURATION = 500;
            private final FadeTransition fadeIn = new FadeTransition(Duration.millis(DURATION));
            private final FadeTransition fadeOut = new FadeTransition(Duration.millis(DURATION));
            private final ParallelTransition fader = new ParallelTransition(fadeIn, fadeOut);

            {
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
            }

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number progress) {
                final double progressValue = progress.doubleValue();

                if (progressValue == 0) {
                    getChildren().setAll(progressIndicator);
                    doFade(wview, progressIndicator);
                } else if (progressValue == 1.0) {
                    getChildren().setAll(wview);
                    doFade(progressIndicator, wview);
                }
                progressIndicator.setProgress(progressValue);
            }

            private void doFade(final Node n1, final Node n2) {
                fader.stop();
                fadeOut.setNode(n1);
                fadeIn.setNode(n2);
                fader.play();
            }
        });

    }

    public void doLoad(final String page) {
        if (!wview.getEngine().getLocation().equals(page)) {
            loadedPage = page;
            wview.getEngine().load(page);
        }
    }

}
