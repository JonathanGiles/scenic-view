package org.fxmisc.cssfx;

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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.fxmisc.cssfx.api.URIToPathConverter;
import org.fxmisc.cssfx.impl.ApplicationStages;
import org.fxmisc.cssfx.impl.CSSFXMonitor;
import org.fxmisc.cssfx.impl.URIToPathConverters;
import org.fxmisc.cssfx.impl.log.CSSFXLogger;
import org.fxmisc.cssfx.impl.log.CSSFXLogger.LogLevel;

public class CSSFX {
    /**
     * Directly start monitoring the CSS of the application using defaults:
     * <ul>
     * <li>standard source file detectors: Maven, Gradle, execution from built JAR (details in {@link URIToPathConverters#DEFAULT_CONVERTERS})</li>
     * <li>detection activated on all stages of the application, including the ones that will appear later on</li>
     * </ul> 
     * @return a Runnable object to stop CSSFX monitoring
     */
    public static Runnable start() {
        return new CSSFXConfig().start();
    }
    
    /**
     * Directly start monitoring CSS for the given Stage.
     * <ul>
     * <li>standard source file detectors: Maven, Gradle, execution from built JAR (details in {@link URIToPathConverters#DEFAULT_CONVERTERS})</li>
     * <li>detection activated on the stage only (and its children)</li>
     * </ul> 
     * @param stage the stage that will be monitored
     * @return a Runnable object to stop CSSFX monitoring
     */
    public static Runnable start(Stage stage) {
        CSSFXConfig cfg = new CSSFXConfig();
        cfg.setRestrictedToStage(stage);
        return cfg.start();
    }
    
    /**
     * Directly start monitoring CSS for the given Scene.
     * <ul>
     * <li>standard source file detectors: Maven, Gradle, execution from built JAR (details in {@link URIToPathConverters#DEFAULT_CONVERTERS})</li>
     * <li>detection activated on the scene only (and its children)</li>
     * </ul> 
     * @param scene the scene that will be monitored
     * @return a Runnable object to stop CSSFX monitoring
     */
    public static Runnable start(Scene scene) {
        CSSFXConfig cfg = new CSSFXConfig();
        cfg.setRestrictedToScene(scene);
        return cfg.start();
    }
    
    /**
     * Directly start monitoring CSS for the given node.
     * <ul>
     * <li>standard source file detectors: Maven, Gradle, execution from built JAR (details in {@link URIToPathConverters#DEFAULT_CONVERTERS})</li>
     * <li>detection activated on the node only (and its children)</li>
     * </ul> 
     * @param node the node that will be monitored
     * @return a Runnable object to stop CSSFX monitoring
     */
    public static Runnable start(Node node) {
        CSSFXConfig cfg = new CSSFXConfig();
        cfg.setRestrictedToNode(node);
        return cfg.start();
    }

    /**
     * Restrict the source file detection for graphical sub-tree of the given {@link Stage}
     * 
     * @param s the stage to restrict the detection on, if null then no restriction will apply
     * @return a {@link CSSFXConfig} object as a builder to allow further configuration
     */
    public static CSSFXConfig onlyFor(Stage s) {
        CSSFXConfig cfg = new CSSFXConfig();
        cfg.setRestrictedToStage(s);
        return cfg;
    }
    /**
     * Restrict the source file detection for graphical sub-tree of the given {@link Scene}
     * 
     * @param s the scene to restrict the detection on, if null then no restriction will apply
     * @return a {@link CSSFXConfig} object as a builder to allow further configuration
     */
    public static CSSFXConfig onlyFor(Scene s) {
        CSSFXConfig cfg = new CSSFXConfig();
        cfg.setRestrictedToScene(s);
        return cfg;
    }
    /**
     * Restrict the source file detection for graphical sub-tree of the given {@link Node}
     * 
     * @param n the node to restrict the detection on, if null then no restriction will apply
     * @return a {@link CSSFXConfig} object as a builder to allow further configuration
     */
    public static CSSFXConfig onlyFor(Node n) {
        CSSFXConfig cfg = new CSSFXConfig();
        cfg.setRestrictedToNode(n);
        return cfg;
    }

    /**
     * Register a new converter that will be used to map CSS resources to local file.
     * @param converter an additional converter to use, ignored if null
     * @return a {@link CSSFXConfig} object as a builder to allow further configuration
     */
    public static CSSFXConfig addConverter(URIToPathConverter converter) {
        return new CSSFXConfig().addConverter(converter);
    }
    
    /**
     * Stores information before finally building/starting the CSS monitoring.
     *  
     * @author Matthieu Brouillard
     */
    public static class CSSFXConfig {
        // LinkedHashSet will preserve ordering
        private final Set<URIToPathConverter> converters = new LinkedHashSet<URIToPathConverter>(Arrays.asList(URIToPathConverters.DEFAULT_CONVERTERS));
        private Stage restrictedToStage = null;
        private Scene restrictedToScene = null;
        private Node restrictedToNode = null;
        
        CSSFXConfig() {
        }

        void setRestrictedToStage(Stage restrictedToStage) {
            this.restrictedToStage = restrictedToStage;
        }

        void setRestrictedToScene(Scene restrictedToScene) {
            this.restrictedToScene = restrictedToScene;
        }

        void setRestrictedToNode(Node restrictedToNode) {
            this.restrictedToNode = restrictedToNode;
        }
        
        /**
         * Empty the list of default converters.
         * Especially usefull for testing purposes where full control of the converters is required.
         * @return a {@link CSSFXConfig} object as a builder to allow further configuration
         */
        public CSSFXConfig noDefaultConverters() {
            converters.clear();
            return this;
        }

        /**
         * Register a new converter that will be used to map CSS resources to local file.
         * @param converter an additional converter to use, ignored if null
         * @return a {@link CSSFXConfig} object as a builder to allow further configuration
         */
        public CSSFXConfig addConverter(URIToPathConverter converter) {
            converters.add(converter);
            return this;
        }
        
        /**
         * Start monitoring CSS resources with the config parameters collected until now. 
         * @return a Runnable object to stop CSSFX monitoring
         */
        public Runnable start() {
            if (!CSSFXLogger.isInitialized()) {
                if (Boolean.getBoolean("cssfx.log")) {
                    LogLevel toActivate = LogLevel.INFO;
                    String levelStr = System.getProperty("cssfx.log.level", "INFO");
                    try {
                        toActivate = LogLevel.valueOf(levelStr);
                    } catch (Exception ignore) {
                        System.err.println("[CSSFX] invalid value for cssfx.log.level, '" + levelStr + "' is not allowed. Select one in: " + Arrays.asList(LogLevel.values()));
                    }
                    CSSFXLogger.setLogLevel(toActivate);
                    
                    String logType = System.getProperty("cssfx.log.type", "console");
                    switch (logType) {
                    case "noop":
                        CSSFXLogger.noop();
                        break;
                    case "console":
                        CSSFXLogger.console();
                        break;
                    case "jul":
                        CSSFXLogger.jul();
                        break;
                    default:
                        System.err.println("[CSSFX] invalid value for cssfx.log.type, '" + logType + "' is not allowed. Select one in: " + Arrays.asList("noop", "console", "jul"));
                        break;
                    }
                } else {
                    CSSFXLogger.noop();
                }
            }
            
            CSSFXMonitor m = new CSSFXMonitor();
            
            if (restrictedToStage != null) {
                m.setStages(FXCollections.singletonObservableList(restrictedToStage));
            } else if (restrictedToScene != null) {
                m.setScenes(FXCollections.singletonObservableList(restrictedToScene));
            } else if (restrictedToNode != null) {
                m.setNodes(FXCollections.singletonObservableList(restrictedToNode));
            } else {
                // we monitor all the stages
                ObservableList<Stage> monitoredStages = (restrictedToStage == null)?ApplicationStages.monitoredStages():FXCollections.singletonObservableList(restrictedToStage);
                m.setStages(monitoredStages);
            }
            
            return start(() -> m);
        }

        private Runnable start(Callable<CSSFXMonitor> monitorBuilder) {
            CSSFXMonitor mon;
            try {
                mon = monitorBuilder.call();
                mon.addAllConverters(converters);
                mon.start();
                return mon::stop;
            } catch (Exception e) {
                throw new RuntimeException("could not create CSSFXMonitor", e);
            }
        }
    }
}
