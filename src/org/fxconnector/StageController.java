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
package org.fxconnector;

import org.fxconnector.details.DetailPaneType;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.node.SVNode;

public interface StageController {

    static final String FX_CONNECTOR_BASE_ID = "FXConnector.";

    StageID getID();

    void update();

    void configurationUpdated(Configuration configuration);

    void close();

    void setEventDispatcher(FXConnectorEventDispatcher stageModelListener);

    boolean isOpened();

    void setSelectedNode(SVNode value);
    
    void removeSelectedNode();

    AppController getAppController();

    void setDetail(DetailPaneType detailType, int detailID, String value);

    void animationsEnabled(boolean enabled);

    void updateAnimations();

    void pauseAnimation(int animationID);

}
