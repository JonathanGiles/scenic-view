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

import javafx.scene.Node;
import javafx.scene.shape.Shape;

import org.fxconnector.StageID;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class ShapeDetailPaneInfo extends DetailPaneInfo {

    Detail fillDetail;

    ShapeDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.SHAPE);
    }

    @Override Class<? extends Node> getTargetClass() {
        return Shape.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Shape;
    }

    @Override protected void createDetails() {
        fillDetail = addDetail("fill", "fill:", ValueType.COLOR);
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Shape shapeNode = (Shape) getTarget();
        if (all || propertyName.equals("fill")) {
            fillDetail.setValue(shapeNode != null ? shapeNode.getFill().toString() : "-");
            fillDetail.setIsDefault(shapeNode == null || shapeNode.getFill() == null);
            fillDetail.setSimpleProperty((shapeNode != null) ? shapeNode.fillProperty() : null);
            if (!all)
                fillDetail.updated();
            if (!all)
                return;
        }
        if (all)
            sendAllDetails();
    }
}
