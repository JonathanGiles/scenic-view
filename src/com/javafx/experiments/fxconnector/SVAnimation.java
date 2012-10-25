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
package com.javafx.experiments.fxconnector;

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
