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

import java.io.Serializable;

public final class GridConstraintsDetail implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4944813187008963711L;
    private final String text;
    private final int colIndex;
    private final int rowIndex;

    GridConstraintsDetail(final String text, final int colIndex, final int rowIndex) {
        this.text = text;
        this.colIndex = colIndex;
        this.rowIndex = rowIndex;
    }

    public String getText() {
        return text;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override public String toString() {
        return "GridConstraintsDetail [text=" + text + ", colIndex=" + colIndex + ", rowIndex=" + rowIndex + "]";
    }
}
