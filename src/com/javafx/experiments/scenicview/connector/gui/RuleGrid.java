/*
 * Rule.fx
 *
 * Created on 18-sep-2009, 20:52:51
 */

package com.javafx.experiments.scenicview.connector.gui;

import java.util.*;

import javafx.scene.shape.*;

/**
 * @author Ander
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
        final List<PathElement> pElements = new ArrayList<PathElement>();
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
