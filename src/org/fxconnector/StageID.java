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

import java.io.Serializable;

public final class StageID implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1091256426733557091L;
    private final int appID;
    private final int stageID;
    private String name;

    public StageID(final int appID, final int stageID) {
        this.appID = appID;
        this.stageID = stageID;
    }

    public int getAppID() {
        return appID;
    }

    public int getStageID() {
        return stageID;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + appID;
        result = prime * result + stageID;
        return result;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StageID other = (StageID) obj;
        if (appID != other.appID)
            return false;
        if (stageID != other.stageID)
            return false;
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override public String toString() {
        return "StageID [appID=" + appID + ", stageID=" + stageID + ", name=" + name + "]";
    }

}
