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
package org.scenicview.view.control;

import org.fxconnector.StageController;
import org.fxconnector.ConnectorUtils;
import java.lang.reflect.Field;

import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import org.scenicview.utils.ExceptionLogger;
import com.sun.javafx.scene.control.skin.CustomColorDialog;

public class RulerConfigurationMenuItem extends MenuItem {

    ObjectProperty<Color> color = new SimpleObjectProperty<>();
    IntegerProperty rulerSeparation = new SimpleIntegerProperty(10);

    public RulerConfigurationMenuItem() {
        setColor(Color.BLACK);
        setText("Configure Ruler");
        setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                final CustomColorDialog dialog = new CustomColorDialog(null);
                dialog.setId(StageController.FX_CONNECTOR_BASE_ID + ".ColorPicker");
                dialog.setCurrentColor(color.get());
                try {
                    final Field field = CustomColorDialog.class.getDeclaredField("customColorProperty");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked") final ObjectProperty<Color> color = (ObjectProperty<Color>) field.get(dialog);
                    color.addListener(new ChangeListener<Color>() {

                        @Override public void changed(final ObservableValue<? extends Color> arg0, final Color arg1, final Color newValue) {
                            setColor(newValue);
                        }
                    });
                } catch (final Exception e) {
                    ExceptionLogger.submitException(e);
                }
                final TextField sliderValue = new TextField();
                final Slider slider = new Slider(5, 50, 10);
                slider.valueProperty().addListener(new ChangeListener<Number>() {

                    @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                        rulerSeparation.set((int) newValue.doubleValue());
                        sliderValue.setText(ConnectorUtils.format(newValue.doubleValue()));
                    }
                });
                slider.setMinWidth(100);
                slider.setMinHeight(20);
                final HBox box = new HBox();
                sliderValue.setMinWidth(40);
                sliderValue.setMinHeight(20);
                sliderValue.setText(ConnectorUtils.format(slider.getValue()));
                sliderValue.setOnAction(new EventHandler<ActionEvent>() {

                    @Override public void handle(final ActionEvent arg0) {
                        final double value = ConnectorUtils.parse(sliderValue.getText());
                        if (value >= slider.getMin() && value <= slider.getMax()) {
                            slider.setValue(value);
                            rulerSeparation.set((int) value);
                        } else if (value < slider.getMin()) {
                            sliderValue.setText(ConnectorUtils.format(slider.getMin()));
                            slider.setValue(slider.getMin());
                        } else {
                            sliderValue.setText(ConnectorUtils.format(slider.getMax()));
                            slider.setValue(slider.getMax());
                        }
                    }
                });
                final Label l = new Label("Ruler separation:");
                l.setMinWidth(100);
                l.setMinHeight(20);
                box.getChildren().addAll(l, slider, sliderValue);
                box.setManaged(false);
                box.setLayoutY(220);
                box.setLayoutX(10);
                dialog.getChildren().add(box);
                dialog.setLayoutX(400);
                dialog.setLayoutY(300);
                dialog.show();
            }
        });
    }

    public void setColor(final Color color) {
        this.color.set(color);
    }

    public Color getColor() {
        return this.color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return this.color;
    }

    public IntegerProperty rulerSeparationProperty() {
        return this.rulerSeparation;
    }

}
