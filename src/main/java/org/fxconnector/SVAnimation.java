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
package org.fxconnector;

import java.io.Serializable;

import javafx.animation.Animation;

public final class SVAnimation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -619258470875560329L;
    private final int id;
    private final String toString;
    private final double rate;
    private final double currentRate;
    private final String status;
    private final int cycleCount;
    private final String currentTime;
    private final String cycleDuration;
    private final String totalDuration;

    public SVAnimation(final int id, final Animation animation) {
        this.id = id;
        this.toString = animation.toString();
        this.rate = animation.getRate();
        this.currentRate = animation.getCurrentRate();
        this.status = animation.getStatus().toString();
        this.cycleCount = animation.getCycleCount();
        this.cycleDuration = animation.getCycleDuration().toString();
        this.currentTime = ((int) animation.getCurrentTime().toMillis()) + "ms";
        this.totalDuration = ((int) animation.getTotalDuration().toMillis()) + "ms";
    }

    @Override public String toString() {
        return "SVAnimation [toString=" + toString + ", rate=" + rate + ", currentRate=" + currentRate + ", status=" + status + ", cycleCount=" + cycleCount + ", currentTime=" + currentTime + ", cycleDuration=" + cycleDuration + ", totalDuration=" + totalDuration + "]";
    }

    public String getToString() {
        return toString;
    }

    public double getRate() {
        return rate;
    }

    public double getCurrentRate() {
        return currentRate;
    }

    public String getStatus() {
        return status;
    }

    public String getCycleCount() {
        return cycleCount == Animation.INDEFINITE ? "INDEFINITE" : Integer.toString(cycleCount);
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public String getCycleDuration() {
        return cycleDuration;
    }

    public String getTotalDuration() {
        return totalDuration;
    }

    public int getId() {
        return id;
    }

}
