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
package org.fxconnector.details;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;

class SizeSerializer implements WritableValue<String> {

    private final DoubleProperty x;
    private final DoubleProperty y;

    SizeSerializer(final DoubleProperty x, final DoubleProperty y) {
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
