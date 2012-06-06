/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.util.Map;

import javafx.collections.ObservableMap;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * 
 * @author aim
 */
public class ConstraintsDisplay extends GridPane {
    @SuppressWarnings("rawtypes") private Map propMap;

    public ConstraintsDisplay() {
        super();
        getStyleClass().add("constraints-display");
        setVgap(2);
        setHgap(4);
        setSnapToPixel(true);
        setPropertiesMap(null);
    }

    public boolean isShowingConstraints() {
        return getChildren().size() > 1;
    }

    public void setPropertiesMap(@SuppressWarnings("rawtypes") final ObservableMap value) {
        getChildren().clear();

        propMap = value;

        if (propMap != null) {
            final Object keys[] = propMap.keySet().toArray();
            int row = 0;
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] instanceof String) {
                    final String propkey = (String) keys[i];
                    if (propkey.contains("pane-") || propkey.contains("box-")) {
                        final Object keyvalue = propMap.get(propkey);
                        final Label label = new Label(propkey + ":");
                        GridPane.setConstraints(label, 0, row);
                        GridPane.setValignment(label, VPos.TOP);
                        GridPane.setHalignment(label, HPos.RIGHT);
                        getChildren().add(label);
                        if (propkey.endsWith("margin")) {
                            final InsetsDisplay marginDisplay = new InsetsDisplay();
                            marginDisplay.setInsetsTarget((Insets) keyvalue);
                            GridPane.setConstraints(marginDisplay, 1, row++);
                            GridPane.setHalignment(marginDisplay, HPos.LEFT);
                            getChildren().add(marginDisplay);
                        } else {
                            final Label valueLabel = new Label(keyvalue.toString());
                            GridPane.setConstraints(valueLabel, 1, row++);
                            GridPane.setHalignment(valueLabel, HPos.LEFT);
                            getChildren().add(valueLabel);
                        }
                    }
                }
            }
        } else {
            final Text novalue = new Text("-");
            GridPane.setConstraints(novalue, 0, 0);
            getChildren().add(novalue);
        }

        // FIXME without this we have ghost text appearing where the layout
        // constraints should be
        if (getChildren().isEmpty()) {
            getChildren().add(new Rectangle(1, 1, Color.TRANSPARENT));
        }

        requestLayout();
    }
}
