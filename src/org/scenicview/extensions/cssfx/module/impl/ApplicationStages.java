package org.scenicview.extensions.cssfx.module.impl;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 CSSFX by Matthieu Brouillard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static org.scenicview.extensions.cssfx.module.impl.log.CSSFXLogger.logger;

import java.lang.reflect.Method;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ApplicationStages {
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
