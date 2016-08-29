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

import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;

public class AllDetails {

    private Node target;

    final DetailPaneInfo[] details;

    public AllDetails(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        details = new DetailPaneInfo[] { 
            new NodeDetailPaneInfo(dispatcher, stageID),
            new ShapeDetailPaneInfo(dispatcher, stageID),
            new ParentDetailPaneInfo(dispatcher, stageID), 
            new RegionDetailPaneInfo(dispatcher, stageID), 
            new GridPaneDetailPaneInfo(dispatcher, stageID), 
            new ControlDetailPaneInfo(dispatcher, stageID), 
            new TextDetailPaneInfo(dispatcher, stageID), 
            new LabeledDetailPaneInfo(dispatcher, stageID), 
            new FullPropertiesDetailPaneInfo(dispatcher, stageID)
        };
    }

    public void setTarget(final Node value) {
        if (target == value)
            return;

        target = value;
        updatePanes();
    }

    public void setShowCSSProperties(final boolean show) {
        for (final DetailPaneInfo detail : details) {
            detail.setShowCSSProperties(show);
        }

    }

    private void updatePanes() {
        for (final DetailPaneInfo detail : details) {
            if (detail.targetMatches(target)) {
                detail.setTarget(target);
            } else {
                detail.clear();
            }
        }
    }

    public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
        for (final DetailPaneInfo detail : details) {
            if (detail.getType() == detailType) {
                detail.setDetail(detailID, value);
                break;
            }
        }
    }
}
