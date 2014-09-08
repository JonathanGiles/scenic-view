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

import java.util.*;

public class AppControllerImpl implements AppController {

    private static final String LOCAL_ID = "Local";

    private final String name;
    private final List<StageController> stages = new ArrayList<>();

    private final int id;

    public AppControllerImpl() {
        this(0, LOCAL_ID);
    }

    public AppControllerImpl(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() {
        return name;
    }

    @Override public List<StageController> getStages() {
        return stages;
    }

    @Override public void close() {
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).close();
        }
    }

    @Override public boolean isLocal() {
        return LOCAL_ID.equals(name);
    }

    @Override public int getID() {
        return id;
    }

}
