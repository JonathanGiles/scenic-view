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
package org.fxconnector.helper;

import java.util.*;

import org.fxconnector.StageController;
import org.fxconnector.remote.FXConnectorFactory;
import org.fxconnector.remote.util.ScenicViewExceptionLogger;

import javafx.stage.Window;

import org.fxconnector.StageController;
import org.fxconnector.remote.util.ScenicViewExceptionLogger;

public abstract class WindowChecker extends WorkerThread {

    private long maxWaitTime = -1;
    private final WindowFilter filter;
    private boolean verbose = false;

    public WindowChecker(final WindowFilter filter, final String name) {
        super(StageController.FX_CONNECTOR_BASE_ID + "SubWindowChecker." + name, 1000);
        this.filter = filter;
    }

    public void verbose() {
        verbose = true;
    }

    public interface WindowFilter {
        public boolean accept(Window window);
    }

    @Override public void run() {
        // Keep iterating until we have a any windows.
        // If we past the maximum wait time, we'll exit
        long currentWait = -1;
        List<Window> windows = getValidWindows(filter);
        while (running) {
            onWindowsFound(windows);
            try {
                if (verbose) {
                    FXConnectorFactory.debug("No JavaFX window found - sleeping for " + sleepTime / 1000 + " seconds");
                }
                Thread.sleep(sleepTime);
                if (maxWaitTime != -1) {
                    currentWait += sleepTime;
                }

                if (currentWait > maxWaitTime) {
                    finish();
                }
            } catch (final InterruptedException ex) {
                if (running) {
                    ScenicViewExceptionLogger.submitException(ex);
                }
            }

            windows = getValidWindows(filter);
        }

    }

    protected abstract void onWindowsFound(List<Window> windows);

    public static List<Window> getValidWindows(final WindowFilter filter) {
        @SuppressWarnings("deprecation") final Iterator<Window> windows = Window.impl_getWindows();
        if (!windows.hasNext())
            return Collections.emptyList();
        final List<Window> list = new ArrayList<Window>();
        while (windows.hasNext()) {
            final Window window = windows.next();
            if (filter.accept(window)) {
                list.add(window);
            }
        }
        return list;
    }

    public void setMaxWaitTime(final long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    @Override protected void work() {
        // TODO Auto-generated method stub

    }

}
