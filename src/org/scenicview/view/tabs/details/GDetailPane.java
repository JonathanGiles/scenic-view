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

import org.scenicview.utils.Logger;
import org.scenicview.view.DisplayUtils;
import org.scenicview.view.ScenicViewGui;
import org.fxconnector.details.Detail;
import org.fxconnector.details.DetailPaneType;

import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.value.WritableValue;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import org.fxconnector.details.Detail.EditionType;

public class GDetailPane extends TitledPane {

    private static final int LABEL_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    public static float FADE = .50f;
    public static DecimalFormat f = new DecimalFormat("0.0#");

    private static final Image EDIT_IMAGE = DisplayUtils.getUIImage("editclear.png");
    private static final Image LOCK_IMAGE = DisplayUtils.getUIImage("lock.png");

    static final String DETAIL_LABEL_STYLE = "detail-label";
    
    private final ScenicViewGui scenicView;

    public DetailPaneType type;
    public GridPane gridpane;
    static GDetail activeDetail;
    List<Node> paneNodes = new ArrayList<>();
    List<GDetail> details = new ArrayList<>();
    APILoader loader;

    public GDetailPane(ScenicViewGui scenicView, final DetailPaneType type, final String name, final APILoader loader) {
        this.scenicView = scenicView;
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

        gridpane = new GridPane();
        gridpane.getStyleClass().add("detail-grid");
        gridpane.setOnMousePressed(event -> {
            if (activeDetail != null)
                activeDetail.recover();
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
        
        final GDetail detail = new GDetail(scenicView, label, valueNode);
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
        // if (currentFilter != null && currentFilter.equals(text)) {
        // return;
        // }
        currentFilter = text;

        /**
         * Make this more clean
         */
        gridpane.getChildren().clear();
        final List<Node> nodes = paneNodes;
        int row = 0;
        for (int i = 0; i < nodes.size(); i++) {

            final Label label = (Label) nodes.get(i++);
            boolean valid = text == null || text.equals("") || label.getText().toLowerCase().indexOf(text.toLowerCase()) != -1;
            final Group g = (Group) nodes.get(i);
            final Node value = g.getChildren().get(0);

            if (!valid && value instanceof Label) {
                valid |= ((Label) value).getText().toLowerCase().indexOf(text.toLowerCase()) != -1;
            }

            if (valid && label.isVisible()) {
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

    public void updateDetails(final List<Detail> details, final RemotePropertySetter setter) {
        clearPane();
        for (int i = 0; i < details.size(); i++) {
            final Detail d = details.get(i);
            Node labelGraphic;
            switch (d.getLabelType()) {
                case LAYOUT_BOUNDS: {
                    final Rectangle layoutBoundsIcon = new Rectangle(12, 12);
                    layoutBoundsIcon.setFill(null);
                    layoutBoundsIcon.setStroke(Color.GREEN);
                    layoutBoundsIcon.setOpacity(.8);
                    layoutBoundsIcon.getStrokeDashArray().addAll(3.0, 3.0);
                    layoutBoundsIcon.setStrokeWidth(1);
                    labelGraphic = layoutBoundsIcon;
                    break;
                }
                case BOUNDS_PARENT: {
                    final Rectangle boundsInParentIcon = new Rectangle(12, 12);
                    boundsInParentIcon.setFill(Color.YELLOW);
                    boundsInParentIcon.setOpacity(.5);
                    labelGraphic = boundsInParentIcon;
                    break;
                }
                case BASELINE: {
                    final Group baselineIcon = new Group();
                    final Line line = new Line(0, 8, 14, 8);
                    line.setStroke(Color.RED);
                    line.setOpacity(.75);
                    line.setStrokeWidth(1);
                    baselineIcon.getChildren().addAll(new Rectangle(10, 10, Color.TRANSPARENT), line);
                    labelGraphic = baselineIcon;
                    break;
                }
                default: {
                    labelGraphic = null;
                    break;
                }
            }
            
            Node value = null;
            final boolean isEditingSupported = Detail.isEditionSupported(d.getEditionType());
            final boolean isPropertyBound = d.getEditionType() == EditionType.NONE_BOUND;
            final Node graphic = isEditingSupported ? new ImageView(GDetailPane.EDIT_IMAGE) :
                                 isPropertyBound    ? new ImageView(GDetailPane.LOCK_IMAGE) :
                                 null;
                
            switch (d.getValueType()) {
                case NORMAL: {
                    final Label valueLabel = new Label();
                    valueLabel.setGraphic(graphic);
                    value = valueLabel;
                    break;
                }
                case INSETS: {
                    final InsetsDisplay display = new InsetsDisplay();
                    value = display;
                    break;
                }
                case CONSTRAINTS: {
                    final ConstraintsDisplay display2 = new ConstraintsDisplay();
                    value = display2;
                    break;
                }
                case GRID_CONSTRAINTS: {
                    final GridConstraintDisplay display3 = new GridConstraintDisplay();
                    value = display3;
                    break;
                }
                case COLOR: {
                    final Label valueLabel = new Label();
                    Color c = Color.valueOf(d.getValue());
                    Rectangle rect = new Rectangle(10, 10, c);
                    HBox hbox = new HBox(5, graphic, rect);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    valueLabel.setGraphic(hbox);
                    valueLabel.setText(d.getValue());
                    value = valueLabel;
                    break;
                }
            }

            final GDetail detail = addDetail(d.getProperty(), d.getLabel(), labelGraphic, value, i);
            doUpdateDetail(detail, d);
            detail.setSerializer(new WritableValue<String>() {
                @Override public void setValue(final String value) {
                    try {
                        setter.set(d, value);
                    } catch (final Exception e) {
                        scenicView.setStatusText(Detail.STATUS_EXCEPTION + e.getMessage(), 10000);
                    }
                }

                @Override public String getValue() {
                    // TODO Auto-generated method stub
                    return null;
                }
            });
        }
        filterProperties(currentFilter);
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
            filterProperties(currentFilter);
        } else {
            Logger.print("Pane not found for detail:" + detail);
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
        if (details.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(type).append('\n');
        for (int i = 0; i < details.size(); i++) {
            sb.append(details.get(i)).append('\n');
        }
        return sb.toString();
    }
}
