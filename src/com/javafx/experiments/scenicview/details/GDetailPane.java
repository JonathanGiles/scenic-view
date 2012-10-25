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
package com.javafx.experiments.scenicview.details;

import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.value.WritableValue;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import com.javafx.experiments.fxconnector.details.*;
import com.javafx.experiments.fxconnector.details.Detail.EditionType;
import com.javafx.experiments.scenicview.*;

public class GDetailPane extends TitledPane {

    private static final int LABEL_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    public static float FADE = .50f;
    public static DecimalFormat f = new DecimalFormat("0.0#");

    private static final Image EDIT_IMAGE = DisplayUtils.getUIImage("editclear.png");
    private static final Image LOCK_IMAGE = DisplayUtils.getUIImage("lock.png");

    static final String DETAIL_LABEL_STYLE = "detail-label";

    DetailPaneType type;
    GridPane gridpane;
    static GDetail activeDetail;
    List<Node> paneNodes = new ArrayList<Node>();
    List<GDetail> details = new ArrayList<GDetail>();
    APILoader loader;

    public GDetailPane(final DetailPaneType type, final String name, final APILoader loader) {
        this.type = type;
        this.loader = loader;
        setText(name);
        getStyleClass().add("detail-pane");
        setManaged(false);
        setVisible(false);
        setExpanded(false);
        setMaxWidth(Double.MAX_VALUE);
        setId("title-label");
        setAlignment(Pos.CENTER_LEFT);

        createGridPane();

    }

    private void createGridPane() {
        gridpane = new GridPane();
        gridpane.getStyleClass().add("detail-grid");
        gridpane.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                if (activeDetail != null)
                    activeDetail.recover();
            }
        });
        gridpane.setHgap(4);
        gridpane.setVgap(2);
        gridpane.setSnapToPixel(true);
        final ColumnConstraints colInfo = new ColumnConstraints(180);
        gridpane.getColumnConstraints().addAll(colInfo, new ColumnConstraints());
        setContent(gridpane);
    }

    protected GDetail addDetail(final String propertyName, final String labelText, final Node labelGraphic, final Node valueNode, final int row) {
        final Label label = new Label(labelText);
        if (labelGraphic != null) {
            label.setGraphic(labelGraphic);
            label.setContentDisplay(ContentDisplay.LEFT);
        }
        final GDetail detail = new GDetail(label, valueNode);
        detail.setAPILoader(loader);
        GridPane.setConstraints(detail.label, LABEL_COLUMN, row);
        GridPane.setHalignment(detail.label, HPos.RIGHT);
        GridPane.setValignment(detail.label, VPos.TOP);
        detail.label.getStyleClass().add(DETAIL_LABEL_STYLE);

        if (valueNode instanceof Label) {
            final Group group = new Group(detail.valueLabel);
            GridPane.setConstraints(group, VALUE_COLUMN, row);
            GridPane.setHalignment(group, HPos.LEFT);
            GridPane.setValignment(group, VPos.TOP);
            detail.valueLabel.getStyleClass().add("detail-value");
            addToPane(detail.label, group);
        } else {
            // icky, but fine for now
            final Group group = new Group(detail.valueNode);
            GridPane.setConstraints(group, VALUE_COLUMN, row);
            GridPane.setHalignment(group, HPos.LEFT);
            GridPane.setValignment(group, VPos.TOP);
            addToPane(detail.label, group);
        }
        details.add(detail);
        return detail;
    }

    protected void clearPane() {
        gridpane.getChildren().clear();
        paneNodes.clear();
        details.clear();
    }

    protected void addToPane(final Node... nodes) {
        gridpane.getChildren().addAll(nodes);
        paneNodes.addAll(Arrays.asList(nodes));
    }

    private String currentFilter = null;

    public void filterProperties(final String text) {
        if (currentFilter != null && currentFilter.equals(text)) {
            return;
        }
        currentFilter = text;

        /**
         * Make this more clean
         */
        gridpane.getChildren().clear();
        final List<Node> nodes = paneNodes;
        int row = 0;
        for (int i = 0; i < nodes.size(); i++) {

            final Label label = (Label) nodes.get(i++);
            boolean valid = text.equals("") || label.getText().toLowerCase().indexOf(text.toLowerCase()) != -1;
            final Group g = (Group) nodes.get(i);
            final Node value = g.getChildren().get(0);

            if (!valid && value instanceof Label) {
                if (((Label) value).getText() == null) {
                    System.out.println("NullValue for " + label.getText());
                }
                valid |= ((Label) value).getText().toLowerCase().indexOf(text.toLowerCase()) != -1;
            }

            if (valid) {
                GridPane.setConstraints(label, LABEL_COLUMN, row);
                GridPane.setConstraints(g, VALUE_COLUMN, row);
                gridpane.getChildren().addAll(label, g);
                row++;
            }
        }
    }

    @Override protected double computeMinWidth(final double height) {
        return prefWidth(height);
    }

    @Override protected double computeMinHeight(final double width) {
        return prefHeight(width);
    }

    void updateDetails(final List<Detail> details, final RemotePropertySetter setter) {
        clearPane();
        for (int i = 0; i < details.size(); i++) {
            final Detail d = details.get(i);
            Node labelGraphic;
            switch (d.getLabelType()) {
            case LAYOUT_BOUNDS:
                final Rectangle layoutBoundsIcon = new Rectangle(12, 12);
                layoutBoundsIcon.setFill(null);
                layoutBoundsIcon.setStroke(Color.GREEN);
                layoutBoundsIcon.setOpacity(.8);
                layoutBoundsIcon.getStrokeDashArray().addAll(3.0, 3.0);
                layoutBoundsIcon.setStrokeWidth(1);
                labelGraphic = layoutBoundsIcon;
                break;

            case BOUNDS_PARENT:
                final Rectangle boundsInParentIcon = new Rectangle(12, 12);
                boundsInParentIcon.setFill(Color.YELLOW);
                boundsInParentIcon.setOpacity(.5);
                labelGraphic = boundsInParentIcon;
                break;

            case BASELINE:
                final Group baselineIcon = new Group();
                final Line line = new Line(0, 8, 14, 8);
                line.setStroke(Color.RED);
                line.setOpacity(.75);
                line.setStrokeWidth(1);
                baselineIcon.getChildren().addAll(new Rectangle(10, 10, Color.TRANSPARENT), line);
                labelGraphic = baselineIcon;
                break;

            default:
                labelGraphic = null;
                break;
            }
            Node value = null;
            switch (d.getValueType()) {
            case NORMAL:
                final Label valueLabel = new Label();
                if (Detail.isEditionSupported(d.getEditionType())) {
                    final ImageView graphic = new ImageView(GDetailPane.EDIT_IMAGE);
                    valueLabel.setGraphic(graphic);
                } else if (d.getEditionType() == EditionType.NONE_BOUND) {
                    final ImageView graphic = new ImageView(GDetailPane.LOCK_IMAGE);
                    valueLabel.setGraphic(graphic);
                }
                value = valueLabel;
                break;
            case INSETS:
                final InsetsDisplay display = new InsetsDisplay();
                value = display;
                break;

            case CONSTRAINTS:
                final ConstraintsDisplay display2 = new ConstraintsDisplay();
                value = display2;
                break;

            case GRID_CONSTRAINTS:
                final GridConstraintDisplay display3 = new GridConstraintDisplay();
                value = display3;
                break;
            }

            final GDetail detail = addDetail(d.getProperty(), d.getLabel(), labelGraphic, value, i);
            doUpdateDetail(detail, d);
            detail.setSerializer(new WritableValue<String>() {

                @Override public void setValue(final String value) {
                    try {
                        setter.set(d, value);
                    } catch (final Exception e) {
                        ScenicView.setStatusText(Detail.STATUS_EXCEPTION + e.getMessage(), 10000);
                    }
                }

                @Override public String getValue() {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
        }
    }

    public void updateDetail(final Detail detail) {
        GDetail pane = null;
        for (int i = 0; i < details.size(); i++) {
            if (details.get(i).detail.equals(detail)) {
                pane = details.get(i);
                break;
            }
        }
        if (pane != null) {
            doUpdateDetail(pane, detail);
            pane.updated();
        } else {
            System.out.println("Pane not found for detail:" + detail);
        }
    }

    private void doUpdateDetail(final GDetail detail, final Detail d) {
        detail.setDetail(d);
        detail.setIsDefault(d.isDefault());
        detail.setReason(d.getReason());
        detail.setValidItems(d.getValidItems());
        detail.setMinMax(d.getMinValue(), d.getMaxValue());
        detail.setEditionType(d.getEditionType());
        detail.setRealValue(d.getRealValue());
        detail.setValue(d.getValue());
    }

    public interface RemotePropertySetter {
        public void set(Detail detail, String value);
    }

    @Override public String toString() {
        if (details.isEmpty())
            return "";
        final StringBuilder sb = new StringBuilder();
        sb.append(type).append('\n');
        for (int i = 0; i < details.size(); i++) {
            sb.append(details.get(i)).append('\n');
        }
        return sb.toString();
    }
}
