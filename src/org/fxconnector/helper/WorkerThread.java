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

import org.scenicview.utils.ExceptionLogger;

public abstract class WorkerThread extends Thread {

    protected boolean running = true;
    protected final int sleepTime;

    public WorkerThread(final String name, final int sleepTime) {
        super(name);
        this.sleepTime = sleepTime;
    }

    public void finish() {
        this.running = false;
        interrupt();
    }

    @Override public void run() {
        long sleepTime = 0;
        while (running) {
            try {
                Thread.sleep(sleepTime);
                work();
            } catch (final Exception e) {
                if (running) {
                    ExceptionLogger.submitException(e);
                }
            }
            sleepTime = this.sleepTime;
        }
    }

    protected abstract void work();
}
