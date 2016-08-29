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

import static org.fxconnector.ConnectorUtils.getBranchCount;
import javafx.scene.Node;
import javafx.scene.Parent;

import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class ParentDetailPaneInfo extends DetailPaneInfo {

    ParentDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.PARENT);
    }

    Detail needsLayoutDetail;
    Detail childCountDetail;
    Detail branchCountDetail;

    @Override Class<? extends Node> getTargetClass() {
        return Parent.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Parent;
    }

    @Override protected void createDetails() {
        childCountDetail = addDetail("child Count", "child count:");
        branchCountDetail = addDetail("branch Count", "branch count:");
        needsLayoutDetail = addDetail("needsLayout", "needsLayout:");
    }

    @Override protected void updateAllDetails() {
        final Parent parent = (Parent) getTarget();

        childCountDetail.setValue(parent != null ? Integer.toString(parent.getChildrenUnmodifiable().size()) : "-");
        branchCountDetail.setValue(parent != null ? Integer.toString(getBranchCount(parent)) : "-");

        // No property change events on these
        needsLayoutDetail.setValue(parent != null ? Boolean.toString(parent.isNeedsLayout()) : "-");
        needsLayoutDetail.setIsDefault(parent == null || !parent.isNeedsLayout());
        sendAllDetails();
    }

    @Override protected void updateDetail(final String propertyName) {
        // no descrete "properties"
    }
}
