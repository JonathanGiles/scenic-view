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
package com.javafx.experiments.scenicview.connector.details;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;

public class SizeSerializer implements WritableValue<String> {

    private final DoubleProperty x;
    private final DoubleProperty y;

    public SizeSerializer(final DoubleProperty x, final DoubleProperty y) {
        this.x = x;
        this.y = y;
    }

    @Override public String getValue() {
        return x.getValue() + " x " + y.getValue();
    }

    @Override public void setValue(final String value) {
        try {
            final int pos = value.indexOf(" x ");
            if (pos == -1) {
                throw new IllegalArgumentException("Invalid size format should be xValue x yValue");
            }
            final String x = value.substring(0, pos);
            final String y = value.substring(pos + 3);
            final double xValue = Double.parseDouble(x);
            final double yValue = Double.parseDouble(y);
            if ((this.x.isBound() && this.x.get() != xValue) || (this.y.isBound() && this.y.get() != yValue)) {
                throw new IllegalArgumentException("Bound value cannot be changed");
            }
            if (!this.x.isBound())
                this.x.set(xValue);
            if (!this.y.isBound())
                this.y.set(yValue);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
