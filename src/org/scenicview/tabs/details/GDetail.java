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
package org.scenicview.tabs.details;

import java.util.Iterator;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import org.fxconnector.ConnectorUtils;
import org.fxconnector.details.Detail;
import org.fxconnector.details.Detail.EditionType;
import org.fxconnector.details.DetailPaneType;
import org.fxconnector.details.GridConstraintsDetail;
import org.scenicview.ScenicView;
import org.scenicview.tabs.DetailsTab;
import org.scenicview.utils.ScenicViewDebug;

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
    private APILoader loader;

    public GDetail(ScenicView scenicView, final Label label, final Node value) {
        this.label = label;

        this.value = value;
        /**
         * Initial implementation...
         */
        final TextField field = new TextField();
        field.getStyleClass().add("detail-field");
        field.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                if (serializer != null) {
                    serializer.setValue(field.getText());
                }
                recover();
            }
        });
        final ComboBox<String> options = new ComboBox<String>();
        options.getStyleClass().add("detail-field");
        options.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(final ObservableValue<? extends String> arg0, final String arg1, final String newValue) {
                if (newValue != null && !newValue.equals(realValue)) {
                    serializer.setValue(newValue.toString());
                    recover();
                }

            }
        });
        options.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent ev) {
                if (ev.isSecondaryButtonDown()) {
                    recover();
                }
            }
        });
        final Slider slider = new Slider();
        slider.getStyleClass().add("detail-field");
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                serializer.setValue(newValue.toString());
            }
        });
        final ColorPicker picker = new ColorPicker();
        picker.getStyleClass().add("detail-field");
        picker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override public void changed(final ObservableValue<? extends Color> arg0, final Color arg1, final Color newValue) {
                serializer.setValue(newValue.toString());
            }
        });
        value.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                if (GDetailPane.activeDetail != null) {
                    GDetailPane.activeDetail.recover();
                }

                // only allow editing in the paid version
                if (Detail.isEditionSupported(editionType)) {
                    switch (editionType) {
                    case COMBO:

                        options.setItems(FXCollections.observableArrayList(validItems));
                        options.getSelectionModel().select(realValue);
                        GDetail.this.field = options;
                        break;
                    case SLIDER:

                        slider.setMax(max);
                        slider.setMin(min);
                        slider.setValue(Double.parseDouble(realValue));
                        GDetail.this.field = slider;

                        break;
                    case COLOR_PICKER:
                        picker.setValue(Color.valueOf(realValue));
                        GDetail.this.field = picker;
                        break;
                    default:
                        field.setText(realValue);
                        GDetail.this.field = field;
                        break;

                    }
                    final Group group = (Group) value.getParent();
                    group.getChildren().clear();
                    group.getChildren().add(GDetail.this.field);
                    GDetail.this.field.requestFocus();
                    GDetailPane.activeDetail = GDetail.this;
                } else {
                    scenicView.setStatusText(reason, 4000);
                }
            }

        });
        if (value instanceof Label) {
            this.valueLabel = (Label) value;
            valueLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent arg0) {
                    if (!scenicView.hasStatusText()) {
                        if (Detail.isEditionSupported(editionType))
                            scenicView.setStatusText("Properties which can be changed will show this icon");
                        else if (editionType == EditionType.NONE_BOUND) {
                            scenicView.setStatusText("Bound Properties will show this icon");
                        }
                    }
                }
            });
            valueLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent arg0) {
                    if (editionType != EditionType.NONE) {
                        scenicView.clearStatusText();
                    }
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
                    loader.loadAPI(detail.getProperty());
                }
            });
        }
    }

    public void setValue(final String value2) {
        switch (detail.getValueType()) {
        case NORMAL:
            ((Label) value).setText(value2);
            break;

        case INSETS:
            ((InsetsDisplay) value).setInsetsTarget(ConnectorUtils.deserializeInsets(value2));
            break;

        case CONSTRAINTS:
            ((ConstraintsDisplay) value).setPropertiesMap(ConnectorUtils.deserializeMap(value2));
            break;

        case GRID_CONSTRAINTS:
            ((GridConstraintDisplay) value).setConstraints(detail.hasGridConstraints());
            final List<GridConstraintsDetail> constraints = detail.getGridConstraintsDetails();
            for (final Iterator<GridConstraintsDetail> iterator = constraints.iterator(); iterator.hasNext();) {
                final GridConstraintsDetail d = iterator.next();
                ((GridConstraintDisplay) value).addObject(d.getText(), d.getRowIndex(), d.getColIndex());
            }
            break;
        default:
            ScenicViewDebug.print("GDetail strange value for" + value2);
            break;
        }

    }

    public void setAPILoader(final APILoader loader) {
        this.loader = loader;
    }

    @Override public String toString() {
        return label.getText() + "=" + ((value instanceof Label) ? ((Label) value).getText() : value.toString());
    }
}