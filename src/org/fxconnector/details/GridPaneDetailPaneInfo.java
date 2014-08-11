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
