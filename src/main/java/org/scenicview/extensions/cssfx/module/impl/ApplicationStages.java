/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler, Matthieu Brouillard
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
package org.scenicview.extensions.cssfx.module.impl;

import static org.scenicview.extensions.cssfx.module.impl.log.CSSFXLogger.logger;

import java.lang.reflect.Method;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ApplicationStages {
    @SuppressWarnings("unchecked")
    public static ObservableList<Stage> monitoredStages(Stage ...restrictedTo) {
        try {
            Class<?> sh = Class.forName("com.sun.javafx.stage.StageHelper");
            Method m = sh.getMethod("getStages");
            ObservableList<Stage> stages = (ObservableList<Stage>) m.invoke(null, new Object[0]);
            logger(ApplicationStages.class).debug("successfully retrieved JavaFX stages from com.sun.javafx.stage.StageHelper");
            return stages;
        } catch (Exception e) {
            logger(ApplicationStages.class).error("cannot observe stages changes by calling com.sun.javafx.stage.StageHelper.getStages()", e);
        }
        return FXCollections.emptyObservableList();
    }
}
