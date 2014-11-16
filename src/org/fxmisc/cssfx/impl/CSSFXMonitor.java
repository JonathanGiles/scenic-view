package org.fxmisc.cssfx.impl;

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


import static org.fxmisc.cssfx.impl.log.CSSFXLogger.logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.fxmisc.cssfx.api.URIToPathConverter;
import org.fxmisc.cssfx.impl.events.CSSFXEvent;
import org.fxmisc.cssfx.impl.events.CSSFXEvent.EventType;
import org.fxmisc.cssfx.impl.events.CSSFXEventListener;
import org.fxmisc.cssfx.impl.monitoring.PathsWatcher;

/**
 * CSSFXMonitor is the central controller of the CSS monitoring feature.   
 *   
 * @author Matthieu Brouillard
 */
public class CSSFXMonitor {
    private PathsWatcher pw;

    // keep insertion order
    private List<URIToPathConverter> knownConverters = new CopyOnWriteArrayList<>();
    private ObservableList<Stage> stages;
    private ObservableList<Scene> scenes;
    private ObservableList<Node> nodes;
    private List<CSSFXEventListener> eventListeners = new CopyOnWriteArrayList<>();

    public CSSFXMonitor() {
    }
    
    public void setStages(ObservableList<Stage> stages) {
        this.stages = stages;
    }

    public void setScenes(ObservableList<Scene> scenes) {
        this.scenes = scenes;
    }

    public void setNodes(ObservableList<Node> nodes) {
        this.nodes = nodes;
    }

    public void addAllConverters(Collection<URIToPathConverter> converters) {
        knownConverters.addAll(converters);
    }

    public void addAllConverters(URIToPathConverter... converters) {
        knownConverters.addAll(Arrays.asList(converters));
    }

    public void addConverter(URIToPathConverter newConverter) {
        knownConverters.add(newConverter);
    }

    public void removeConverter(URIToPathConverter converter) {
        knownConverters.remove(converter);
    }

    public void addEventListener(CSSFXEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(CSSFXEventListener listener) {
        eventListeners.remove(listener);
    }

    public void start() {
        logger(CSSFXMonitor.class).info("CSS Monitoring is about to start");

        pw = new PathsWatcher();

        // start to monitor stage changes
        if (stages != null) {
            monitorStages(stages);
        } else if (scenes != null) {
            monitorScenes(scenes);
        } else if (nodes != null) {
            monitorChildren(nodes);
        }

        pw.watch();
        logger(CSSFXMonitor.class).info("CSS Monitoring started");
    }
    
    public void stop() {
        pw.stop();
    }

    private void monitorStages(ObservableList<Stage> observableStages) {
        // first listen for changes
        observableStages.addListener(new ListChangeListener<Stage>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Stage> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (Stage removedStage : c.getRemoved()) {
                            unregisterStage(removedStage);
                        }
                    }
                    if (c.wasAdded()) {
                        for (Stage addedStage : c.getAddedSubList()) {
                            registerStage(addedStage);
                        }
                    }
                }
            }
        });

        // then process already existing stages
        for (Stage stage : observableStages) {
            registerStage(stage);
        }

    }

    private void monitorStageScene(ReadOnlyObjectProperty<Scene> stageSceneProperty) {
        // first listen to changes
        stageSceneProperty.addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> ov, Scene o, Scene n) {
                if (o != null) {
                    unregisterScene(o);
                }
                if (n != null) {
                    registerScene(n);
                }
            }
        });

        if (stageSceneProperty.getValue() != null) {
            registerScene(stageSceneProperty.getValue());
        }
    }

    private void monitorRoot(ObjectProperty<Parent> rootProperty) {
        // register on modification
        rootProperty.addListener((ov, o, n) -> {
            if (o != null) {
                unregisterNode(o);
            }
            if (n != null) {
                registerNode(n);
            }
        });

        // check current value
        if (rootProperty.getValue() != null) {
            registerNode(rootProperty.getValue());
        }
    }

    private void unregisterNode(Node removedNode) {
        eventNotify(CSSFXEvent.newEvent(EventType.NODE_REMOVED, removedNode));
    }

    private void registerNode(Node node) {
        if (node instanceof Parent) {
            Parent p = (Parent) node;
            monitorStylesheets(p.getStylesheets());
            monitorChildren(p.getChildrenUnmodifiable());
        }
        eventNotify(CSSFXEvent.newEvent(EventType.NODE_ADDED, node));
    }
    
    private void monitorScenes(ObservableList<Scene> observableScenes) {
        // first listen for changes
        observableScenes.addListener(new ListChangeListener<Scene>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Scene> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (Scene removedScene : c.getRemoved()) {
                            unregisterScene(removedScene);
                        }
                    }
                    if (c.wasAdded()) {
                        for (Scene addedScene : c.getAddedSubList()) {
                            registerScene(addedScene);
                        }
                    }
                }
            }
        });
        
        // then add existing values
        for (Scene s : observableScenes) {
            registerScene(s);
        }
    }

    private void monitorChildren(ObservableList<Node> childrenUnmodifiable) {
        // first listen to changes
        childrenUnmodifiable.addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Node> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (Node removedNode : c.getRemoved()) {
                            unregisterNode(removedNode);
                        }
                    }
                    if (c.wasAdded()) {
                        for (Node addedNode : c.getAddedSubList()) {
                            registerNode(addedNode);
                        }
                    }
                }
            }
        });
        // then look already existing children
        for (Node node : childrenUnmodifiable) {
            registerNode(node);
        }
    }

    private void monitorStylesheets(ObservableList<String> stylesheets) {
        final URIRegistrar registrar = new URIRegistrar(knownConverters, pw);

        // first register for changes
        stylesheets.addListener(new StyleSheetChangeListener(registrar));

        // then look already set stylesheets uris
        for (String uri : stylesheets) {
            registrar.register(uri, stylesheets);
        }
    }

    private void registerScene(Scene scene) {
        eventNotify(CSSFXEvent.newEvent(EventType.SCENE_ADDED, scene));

        monitorStylesheets(scene.getStylesheets());
        monitorRoot(scene.rootProperty());
    }

    private void unregisterScene(Scene removedScene) {
        eventNotify(CSSFXEvent.newEvent(EventType.SCENE_REMOVED, removedScene));
    }

    private void registerStage(Stage stage) {
        eventNotify(CSSFXEvent.newEvent(EventType.STAGE_ADDED, stage));
        monitorStageScene(stage.sceneProperty());
    }

    private void unregisterStage(Stage removedStage) {
        if (removedStage.getScene() != null) {
            eventNotify(CSSFXEvent.newEvent(EventType.SCENE_REMOVED, removedStage.getScene()));
        }
        eventNotify(CSSFXEvent.newEvent(EventType.STAGE_REMOVED, removedStage));
    }

    private void eventNotify(CSSFXEvent<?> e) {
        for (CSSFXEventListener listener : eventListeners) {
            listener.onEvent(e);
        }
    }

    private static class URIRegistrar {
        final Map<String, Set<ObservableList<? extends String>>> stylesheetsContainingURI = new HashMap<>();
        final Map<String, Path> sourceURIs = new HashMap<>();
        final List<URIToPathConverter> converters;
        private PathsWatcher wp;

        URIRegistrar(List<URIToPathConverter> c, PathsWatcher wp) {
            converters = c;
            this.wp = wp;
        }

        private void register(String uri, ObservableList<? extends String> stylesheets) {
            if (!sourceURIs.containsKey(uri)) {
                logger(CSSFXMonitor.class).debug("searching source for css[%s]", uri);
                // we do not yet have a source mapping for the URI
                // let's register this URI
                Set<ObservableList<? extends String>> uriUsedIn = stylesheetsContainingURI.computeIfAbsent(uri, k -> new HashSet<>());
                uriUsedIn.add(stylesheets);

                evaluateSourceMappingForURI(uri);
            }
        }

        @SuppressWarnings("unchecked")
        private void evaluateSourceMappingForURI(String uri) {
            for (URIToPathConverter c : converters) {
                Path sourceFile = c.convert(uri);

                if (sourceFile != null) {
                    logger(CSSFXMonitor.class).info("css[%s] will be mapped to source[%s]", uri, sourceFile);
                    Path directory = sourceFile.getParent();

                    // let's see if other mappings were not waiting for a source mapping
                    final Set<ObservableList<? extends String>> set = stylesheetsContainingURI.get(uri);
                    for (Iterator<ObservableList<? extends String>> it = set.iterator(); it.hasNext();) {
                        ObservableList<? extends String> waitingStylesheets = it.next();
                        if (!waitingStylesheets.contains(uri)) {
                            it.remove();
                        } else {
                            wp.monitor(directory.toAbsolutePath().normalize(), sourceFile.toAbsolutePath().normalize(), new URIStyleUpdater(uri,
                                    sourceFile.toUri().toString(), (ObservableList<String>) waitingStylesheets));
                        }
                    }
                    stylesheetsContainingURI.remove(uri);
                    sourceURIs.put(sourceFile.toUri().toString(), sourceFile);
                    break;
                }
            }
        }

        private void unregister(String uri) {
        }

        /**
         * Reevaluate not mapped uris
         */
        @SuppressWarnings("unused")
        private void reevaluate() {
            for (String uri : stylesheetsContainingURI.keySet()) {
                evaluateSourceMappingForURI(uri);
            }
        }
    }

    private static class StyleSheetChangeListener implements ListChangeListener<String> {
        private URIRegistrar registrar;

        private StyleSheetChangeListener(URIRegistrar registrar) {
            this.registrar = registrar;
        }

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (String removedURI : c.getRemoved()) {
                        registrar.unregister(removedURI);
                    }
                }
                if (c.wasAdded()) {
                    for (String newURI : c.getAddedSubList()) {
                        registrar.register(newURI, c.getList());
                    }
                }
            }
        }
    }

    final ListChangeListener<String> styleSheetChangeListener = new ListChangeListener<String>() {
        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
        }
    };

    private static class URIStyleUpdater implements Runnable {
        private final String sourceURI;
        private final String originalURI;
        private final ObservableList<String> cssURIs;

        URIStyleUpdater(String originalURI, String sourceURI, ObservableList<String> cssURIs) {
            this.originalURI = originalURI;
            this.sourceURI = sourceURI;
            this.cssURIs = cssURIs;
        }

        @Override
        public void run() {
            IntegerProperty positionIndex = new SimpleIntegerProperty();

            Platform.runLater(() -> {
                positionIndex.set(cssURIs.indexOf(originalURI));
                if (positionIndex.get() != -1) {
                    cssURIs.remove(originalURI);
                }
                if (positionIndex.get() == -1) {
                    positionIndex.set(cssURIs.indexOf(sourceURI));
                }
                cssURIs.remove(sourceURI);
            });
            Platform.runLater(() -> {
                if (positionIndex.get() >= 0) {
                    cssURIs.add(positionIndex.get(), sourceURI);
                } else {
                    cssURIs.add(sourceURI);
                }
            });
        }
    }
}
