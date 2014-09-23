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
package org.scenicview.view.tabs.details;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import org.fxconnector.ConnectorUtils;
import org.fxconnector.details.Detail;
import org.fxconnector.details.Detail.EditionType;
import org.fxconnector.details.DetailPaneType;
import org.fxconnector.details.GridConstraintsDetail;
import org.scenicview.view.ScenicViewGui;
import org.scenicview.view.tabs.DetailsTab;

public class GDetail {
    public Label label;
    public Label valueLabel;
    public Node valueNode;
    private final Node value;
    private Node field;
    Detail detail;

    private boolean isDefault = true;

    WritableValue<String> serializer;
    String reason = Detail.STATUS_NOT_SUPPORTED;
    private String[] validItems;
    private double min;
    private double max;
    private String realValue;
    private EditionType editionType;
    private Consumer<String> loader;

    public GDetail(ScenicViewGui scenicView, final Label label, final Node value) {
        this.label = label;
        this.value = value;
        
        /**
         * Initial implementation...
         */
        final TextField field = new TextField();
        field.getStyleClass().add("detail-field");
        field.setOnAction(event -> {
            if (serializer != null) {
                serializer.setValue(field.getText());
            }
            recover();
        });
        
        final ComboBox<String> options = new ComboBox<>();
        options.getStyleClass().add("detail-field");
        options.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(realValue)) {
                serializer.setValue(newValue.toString());
                recover();
            }
        });
        
        options.setOnMouseClicked(ev -> {
            if (ev.isSecondaryButtonDown()) {
                recover();
            }
        });
        
        final Slider slider = new Slider();
        slider.getStyleClass().add("detail-field");
        slider.valueProperty().addListener((o, oldValue, newValue) -> serializer.setValue(newValue.toString()));
        
        final ColorPicker picker = new ColorPicker();
        picker.getStyleClass().add("detail-field");
        picker.valueProperty().addListener((o, oldValue, newValue) -> serializer.setValue(newValue.toString()));
        
        value.setOnMouseClicked(event -> {
            if (GDetailPane.activeDetail != null) {
                GDetailPane.activeDetail.recover();
            }

            // only allow editing in the paid version
            if (Detail.isEditionSupported(editionType)) {
                switch (editionType) {
                    case COMBO: {
                        options.setItems(FXCollections.observableArrayList(validItems));
                        options.getSelectionModel().select(realValue);
                        GDetail.this.field = options;
                        break;
                    }
                    case SLIDER: {
                        slider.setMax(max);
                        slider.setMin(min);
                        slider.setValue(Double.parseDouble(realValue));
                        GDetail.this.field = slider;
                        break;
                    }
                    case COLOR_PICKER: {
                        picker.setValue(Color.valueOf(realValue));
                        GDetail.this.field = picker;
                        break;
                    }
                    default: {
                        field.setText(realValue);
                        GDetail.this.field = field;
                        break;
                    }
                }
                final Group group = (Group) value.getParent();
                group.getChildren().clear();
                group.getChildren().add(GDetail.this.field);
                GDetail.this.field.requestFocus();
                GDetailPane.activeDetail = GDetail.this;
            } else {
                scenicView.setStatusText(reason, 4000);
            }
        });
        
        if (value instanceof Label) {
            this.valueLabel = (Label) value;
            valueLabel.setOnMouseEntered(event -> {
                if (!scenicView.hasStatusText()) {
                    if (Detail.isEditionSupported(editionType))
                        scenicView.setStatusText("Properties which can be changed will show this icon");
                    else if (editionType == EditionType.NONE_BOUND) {
                        scenicView.setStatusText("Bound Properties will show this icon");
                    }
                }
            });
            
            valueLabel.setOnMouseExited(event -> {
                if (editionType != EditionType.NONE) {
                    scenicView.clearStatusText();
                }
            });
        } else {
            this.valueNode = value;
        }
    }

    public void setSerializer(final WritableValue<String> serializer) {
        this.serializer = serializer;
    }

    public void setIsDefault(final boolean isDefault) {
        this.isDefault = isDefault;

        if (label != null) {
            label.setOpacity(isDefault ? GDetailPane.FADE : 1.0);
        }
        if (value != null) {
            value.setOpacity(isDefault ? GDetailPane.FADE : 1.0);
        }

        final boolean showDetail = (!DetailsTab.showDefaultProperties && !isDefault) || DetailsTab.showDefaultProperties;

        if (label != null) {
            label.setVisible(showDetail);
            label.setManaged(showDetail);
        }
        if (value != null && value.getParent()!=null) {
            value.getParent().setVisible(showDetail);
            value.getParent().setManaged(showDetail);
        }
    }

    public boolean isDefault() {
        return isDefault;
    }

    void recover() {
        final Group group = (Group) field.getParent();
        group.getChildren().clear();
        group.getChildren().add(value);
        GDetailPane.activeDetail = null;
    }

    public final void setReason(final String reason) {
        this.reason = reason;
    }

    public final void updated() {
        label.getStyleClass().remove(GDetailPane.DETAIL_LABEL_STYLE);
        label.getStyleClass().add("updated-detail-label");
        
        new Timeline(new KeyFrame(Duration.millis(5000), event -> {
            label.getStyleClass().remove("updated-detail-label");
            label.getStyleClass().add(GDetailPane.DETAIL_LABEL_STYLE);
        })).play();
    }

    public void setValidItems(final String[] validItems) {
        this.validItems = validItems;
    }

    public void setMinMax(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    public void setEditionType(final EditionType editionType) {
        this.editionType = editionType;
    }

    public void setRealValue(final String realValue) {
        this.realValue = realValue;
    }

    public void setDetail(final Detail detail) {
        this.detail = detail;
        if (detail.getDetailType() == DetailPaneType.FULL) {
            label.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2) {
                    loader.accept(detail.getProperty());
                }
            });
        }
    }

    public void setValue(final String value2) {
        switch (detail.getValueType()) {
            case COLOR:
            case NORMAL: {
                ((Label) value).setText(value2);
                break;
            }
            case INSETS: {
                ((InsetsDisplay) value).setInsetsTarget(ConnectorUtils.deserializeInsets(value2));
                break;
            }
            case CONSTRAINTS: {
                ((ConstraintsDisplay) value).setPropertiesMap(ConnectorUtils.deserializeMap(value2));
                break;
            }
            case GRID_CONSTRAINTS: {
                ((GridConstraintDisplay) value).setConstraints(detail.hasGridConstraints());
                final List<GridConstraintsDetail> constraints = detail.getGridConstraintsDetails();
                for (final Iterator<GridConstraintsDetail> iterator = constraints.iterator(); iterator.hasNext();) {
                    final GridConstraintsDetail d = iterator.next();
                    ((GridConstraintDisplay) value).addObject(d.getText(), d.getRowIndex(), d.getColIndex());
                }
                break;
            }
//            default: {
//                ScenicViewDebug.print("GDetail strange value for" + value2);
//                break;
//            }
        }
    }

    public void setAPILoader(final Consumer<String> loader) {
        this.loader = loader;
    }

    @Override public String toString() {
        return label.getText() + "=" + ((value instanceof Label) ? ((Label) value).getText() : value.toString());
    }
}