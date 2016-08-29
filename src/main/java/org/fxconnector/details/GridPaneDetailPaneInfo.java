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

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import org.fxconnector.StageID;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class GridPaneDetailPaneInfo extends DetailPaneInfo {
    GridPaneDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.GRID_PANE);
    }

    Detail gapDetail;
    Detail alignmentDetail;
    Detail gridLinesVisibleDetail;
    Detail rowConstraintsDetail;
    Detail columnConstraintsDetail;

    private ListChangeListener<RowConstraints> rowListener;

    // private ListChangeListener<ColumnConstraints> columnListener;

    @Override Class<? extends Node> getTargetClass() {
        return GridPane.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof GridPane;
    }

    @Override protected void createDetails() {
        gapDetail = addDetail("gap", "hgap/vgap:");
        alignmentDetail = addDetail("alignment", "alignment:");
        gridLinesVisibleDetail = addDetail("gridLinesVisible", "gridLinesVisible:");
        rowConstraintsDetail = addDetail("rows", "rowConstraints:", ValueType.GRID_CONSTRAINTS);
        columnConstraintsDetail = addDetail("columns", "columnConstraints:", ValueType.GRID_CONSTRAINTS);

        rowListener = new ListChangeListener<RowConstraints>() {
            @Override public void onChanged(final Change<? extends RowConstraints> change) {
                updateRowConstraints();
            }
        };
    }

    // need to generify
    @Override void setTarget(final Object target) {
        if (getTarget() != null) {
            final GridPane old = (GridPane) getTarget();
            old.getRowConstraints().removeListener(rowListener);
        }
        if (target != null) {
            // GridPane gridpane = (GridPane)target;
            // MH: Looks like a temporary solution, do not understand the
            // purpose
            // gridpane.getRowConstraints().addListener(null);
        }
        super.setTarget(target);
    }

    @Override protected void updateAllDetails() {

        updateRowConstraints();
        updateColumnConstraints();
        updateDetail("*");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final GridPane gridpane = (GridPane) getTarget();
        if (all || propertyName.equals("hgap") || propertyName.equals("vgap")) {
            gapDetail.setValue(gridpane != null ? (gridpane.getHgap() + " / " + gridpane.getVgap()) : "-");
            gapDetail.setIsDefault(gridpane == null || (gridpane.getHgap() == 0 && gridpane.getVgap() == 0));
            gapDetail.setSimpleSizeProperty(gridpane != null ? gridpane.hgapProperty() : null, gridpane != null ? gridpane.vgapProperty() : null);
            if (!all)
                gapDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("alignment")) {
            alignmentDetail.setValue(gridpane != null ? gridpane.getAlignment().toString() : "-");
            alignmentDetail.setIsDefault(gridpane == null || gridpane.getAlignment() == Pos.TOP_LEFT);
            alignmentDetail.setEnumProperty(gridpane != null ? gridpane.alignmentProperty() : null, Pos.class);
            if (!all)
                alignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("gridLinesVisible")) {
            gridLinesVisibleDetail.setValue(gridpane != null ? Boolean.toString(gridpane.isGridLinesVisible()) : "-");
            gridLinesVisibleDetail.setIsDefault(gridpane == null || !gridpane.isGridLinesVisible());
            gridLinesVisibleDetail.setSimpleProperty(gridpane != null ? gridpane.gridLinesVisibleProperty() : null);
            if (!all)
                gridLinesVisibleDetail.updated();
            if (!all)
                return;
        }
        if (all)
            sendAllDetails();

    }

    private void updateColumnConstraints() {
        final GridPane gridpane = (GridPane) getTarget();

        final ObservableList<ColumnConstraints> columns = gridpane != null ? gridpane.getColumnConstraints() : null;
        columnConstraintsDetail.setConstraints(columns);

        if (columns != null) {
            for (int rowIndex = 1; rowIndex <= columns.size(); rowIndex++) {
                final ColumnConstraints cc = columns.get(rowIndex - 1);
                int colIndex = 0;
                columnConstraintsDetail.add(Integer.toString(rowIndex - 1), colIndex++, rowIndex);
                columnConstraintsDetail.addSize(cc.getMinWidth(), rowIndex, colIndex++);
                columnConstraintsDetail.addSize(cc.getPrefWidth(), rowIndex, colIndex++);
                columnConstraintsDetail.addSize(cc.getMaxWidth(), rowIndex, colIndex++);
                columnConstraintsDetail.add(cc.getPercentWidth() != -1 ? f.format(cc.getPercentWidth()) : "-", colIndex++, rowIndex);
                columnConstraintsDetail.addObject(cc.getHgrow(), rowIndex, colIndex++);
                columnConstraintsDetail.addObject(cc.getHalignment(), rowIndex, colIndex++);
                columnConstraintsDetail.add(Boolean.toString(cc.isFillWidth()), colIndex, rowIndex);
            }
        }
    }

    private void updateRowConstraints() {
        final GridPane gridpane = (GridPane) getTarget();

        final ObservableList<RowConstraints> rows = gridpane != null ? gridpane.getRowConstraints() : null;
        rowConstraintsDetail.setConstraints(rows);

        if (rows != null) {
            for (int rowIndex = 1; rowIndex <= rows.size(); rowIndex++) {
                final RowConstraints rc = rows.get(rowIndex - 1);
                int colIndex = 0;
                rowConstraintsDetail.add(Integer.toString(rowIndex - 1), colIndex++, rowIndex);
                rowConstraintsDetail.addSize(rc.getMinHeight(), rowIndex, colIndex++);
                rowConstraintsDetail.addSize(rc.getPrefHeight(), rowIndex, colIndex++);
                rowConstraintsDetail.addSize(rc.getMaxHeight(), rowIndex, colIndex++);
                rowConstraintsDetail.add(rc.getPercentHeight() != -1 ? f.format(rc.getPercentHeight()) : "-", colIndex++, rowIndex);
                rowConstraintsDetail.addObject(rc.getVgrow(), rowIndex, colIndex++);
                rowConstraintsDetail.addObject(rc.getValignment(), rowIndex, colIndex++);
                rowConstraintsDetail.add(Boolean.toString(rc.isFillHeight()), colIndex, rowIndex);
            }
        }
    }
}
