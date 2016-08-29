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
package org.fxconnector.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.stage.Window;

import org.fxconnector.StageController;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.Logger;

public abstract class WindowChecker extends WorkerThread {

    private long maxWaitTime = -1;
    private final WindowFilter filter;

    public WindowChecker(final WindowFilter filter, final String name) {
        super(StageController.FX_CONNECTOR_BASE_ID + "SubWindowChecker." + name, 1000);
        this.filter = filter;
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
                Logger.print("No JavaFX window found - sleeping for " + sleepTime / 1000 + " seconds");
                Thread.sleep(sleepTime);
                if (maxWaitTime != -1) {
                    currentWait += sleepTime;
                }

                if (currentWait > maxWaitTime) {
                    finish();
                }
            } catch (final InterruptedException ex) {
                if (running) {
                    ExceptionLogger.submitException(ex);
                }
            }

            windows = getValidWindows(filter);
        }
    }

    protected abstract void onWindowsFound(List<Window> windows);

    public static List<Window> getValidWindows(final WindowFilter filter) {
        ObservableList<Window> windows = Window.getWindows();
        if (windows.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Window> validWindows = new ArrayList<>();
        for (Window window : windows) {
            if (filter.accept(window)) {
                validWindows.add(window);
            }
        }
        return validWindows;
    }

    public void setMaxWaitTime(final long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    @Override protected void work() {
        // TODO Auto-generated method stub

    }

}
