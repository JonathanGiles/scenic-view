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
package org.fxconnector.gui;

import java.util.*;

import javafx.scene.shape.*;

/**
 * 
 */
public class RuleGrid extends Path {
    double width;
    double height;

    public RuleGrid(final double separation, final double width, final double height) {
        this.width = width;
        this.height = height;
        updateSeparation(separation);
    }

    public void updateSeparation(final double separation) {
        double x = separation;
        double y = 0;
        final List<PathElement> pElements = new ArrayList<>();
        getElements().clear();
        while (y < height) {
            pElements.add(new MoveTo(0, y));
            pElements.add(new LineTo(width, y));
            y += separation;
        }
        while (x < width) {
            pElements.add(new MoveTo(x, 0));
            pElements.add(new LineTo(x, height));
            x += separation;
        }
        getElements().addAll(pElements);
        setOpacity(0.3);
    }

}
