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
package org.fxconnector.event;

import org.fxconnector.details.Detail;
import org.fxconnector.details.DetailPaneType;
import java.util.List;

import org.fxconnector.StageID;


public class DetailsEvent extends FXConnectorEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -6272264701263599805L;
    private final DetailPaneType paneType;
    private final String paneName;
    final List<Detail> details;

    public DetailsEvent(final SVEventType type, final StageID id, final DetailPaneType dtype, final String paneName, final List<Detail> details) {
        super(type, id);
        this.paneType = dtype;
        this.paneName = paneName;
        this.details = details;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public DetailPaneType getPaneType() {
        return paneType;
    }

    public String getPaneName() {
        return paneName;
    }

}
