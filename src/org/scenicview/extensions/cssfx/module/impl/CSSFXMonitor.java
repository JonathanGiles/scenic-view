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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

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
import javafx.stage.Window;

import org.scenicview.extensions.cssfx.module.api.CSSFXEvent;
import org.scenicview.extensions.cssfx.module.api.CSSFXEvent.EventType;
import org.scenicview.extensions.cssfx.module.api.MonitoredStylesheet;
import org.scenicview.extensions.cssfx.module.impl.monitoring.PathsWatcher;

/**
 * CSSFXMonitor is the central controller of the CSS monitoring feature.   
 *   
 * @author Matthieu Brouillard
 */
public class CSSFXMonitor implements Consumer<CSSFXEvent<?>> {
    private PathsWatcher pw;

    // keep insertion order
    private List<Function<String, Path>> knownConverters = new CopyOnWriteArrayList<>();
    private ObservableList<Window> windows;
    private ObservableList<Stage> stages;
    private ObservableList<Scene> scenes;
    private ObservableList<Node> nodes;
    private List<Consumer<CSSFXEvent<?>>> eventListeners = new CopyOnWriteArrayList<>();
    private URIRegistrar registrar;
    final Map<String, List<MonitoredStylesheet>> monitoredByURI = new HashMap<>();
    final List<MonitoredStylesheet> registered = new LinkedList<>();

    public CSSFXMonitor() {
    }
    
    public void setWindows(ObservableList<Window> windows) {
        this.windows = windows;
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

    public void addAllConverters(Collection<Function<String, Path>> converters) {
        knownConverters.addAll(converters);
    }

    @SuppressWarnings("unchecked")
    public void addAllConverters(Function<String, Path> ... converters) {
        knownConverters.addAll(Arrays.asList(converters));
    }

    public void addConverter(Function<String, Path> newConverter) {
        knownConverters.add(newConverter);
    }

    public void removeConverter(Function<String, Path> converter) {
        knownConverters.remove(converter);
    }

    public void addEventListener(Consumer<CSSFXEvent<?>> listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(Consumer<CSSFXEvent<?>> listener) {
        eventListeners.remove(listener);
    }
    
    public Collection<MonitoredStylesheet> allKnownStylesheets() {
        Collection<MonitoredStylesheet> c = new LinkedList<MonitoredStylesheet>();
        for (List<MonitoredStylesheet> lms : monitoredByURI.values()) {
            c.addAll(lms);
        }
        return c;
    }

    public void start() {
        logger(CSSFXMonitor.class).info("CSS Monitoring is about to start");

        pw = new PathsWatcher();
        registrar = new URIRegistrar(knownConverters, pw, this);
        
        // start to monitor stage changes
        if (windows != null) {
            monitorStages(windows);
        } else if (stages != null) {
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

    private void monitorStages(ObservableList<? extends Window> observableStages) {
        // first listen for changes
        observableStages.addListener(new ListChangeListener<Window>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Window> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (Window removedStage : c.getRemoved()) {
                            unregisterStage(removedStage);
                        }
                    }
                    if (c.wasAdded()) {
                        for (Window addedStage : c.getAddedSubList()) {
                            registerStage(addedStage);
                        }
                    }
                }
            }
        });

        // then process already existing stages
        for (Window stage : observableStages) {
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
        accept(CSSFXEvent.newEvent(EventType.NODE_REMOVED, removedNode));
    }

    private void registerNode(Node node) {
        if (node instanceof Parent) {
            Parent p = (Parent) node;
            monitorStylesheets(p, p.getStylesheets());
            monitorChildren(p.getChildrenUnmodifiable());
        }
        accept(CSSFXEvent.newEvent(EventType.NODE_ADDED, node));
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

    private void monitorStylesheets(Object origin, ObservableList<String> stylesheets) {
        // first register for changes
        stylesheets.addListener(new StyleSheetChangeListener(registrar, origin));

        // then look already set stylesheets uris
        for (String uri : stylesheets) {
            // we register the stylesheet only if it is not one that CSSFX added in replacement of a monitored one
            if (!isAManagedSourceURI(uri)) {
                registrar.register(origin, uri, stylesheets);
            }
        }
    }

    private void registerScene(Scene scene) {
        accept(CSSFXEvent.newEvent(EventType.SCENE_ADDED, scene));

        monitorStylesheets(scene, scene.getStylesheets());
        monitorRoot(scene.rootProperty());
    }

    private void unregisterScene(Scene removedScene) {
        accept(CSSFXEvent.newEvent(EventType.SCENE_REMOVED, removedScene));
    }

    private void registerStage(Window stage) {
        accept(CSSFXEvent.newEvent(EventType.STAGE_ADDED, stage));
        monitorStageScene(stage.sceneProperty());
    }

    private void unregisterStage(Window removedStage) {
        if (removedStage.getScene() != null) {
            accept(CSSFXEvent.newEvent(EventType.SCENE_REMOVED, removedStage.getScene()));
        }
        accept(CSSFXEvent.newEvent(EventType.STAGE_REMOVED, removedStage));
    }

    /* (non-Javadoc)
     * @see org.scenicview.extensions.cssfx.module.impl.CSSFXEventNotifer#eventNotify(org.scenicview.extensions.cssfx.module.impl.events.CSSFXEvent)
     */
    @Override
    public void accept(CSSFXEvent<?> e) {
        for (Consumer<CSSFXEvent<?>> listener : eventListeners) {
            listener.accept(e);
        }
    }
    
    private boolean isAManagedSourceURI(String uri) {
        return allKnownStylesheets().stream().anyMatch(ms -> (ms.getSource()==null)?false:uri.equals(ms.getSource().toUri().toString()));
    }

    private class URIRegistrar {
        final Map<String, Set<ObservableList<? extends String>>> stylesheetsContainingURI = new HashMap<>();
        final Map<String, Path> sourceURIs = new HashMap<>();
        final List<Function<String, Path>> converters;
        private PathsWatcher wp;
        private Consumer<CSSFXEvent<?>> notifier;

        URIRegistrar(List<Function<String, Path>> c, PathsWatcher wp, Consumer<CSSFXEvent<?>> notifier) {
            converters = c;
            this.wp = wp;
            this.notifier = notifier;
        }

        private void register(Object origin, String uri, ObservableList<? extends String> stylesheets) {
            if (!sourceURIs.containsKey(uri)) {
                logger(CSSFXMonitor.class).debug("searching source for css[%s]", uri);
                // we do not yet have a source mapping for the URI
                // let's register this URI
                Set<ObservableList<? extends String>> uriUsedIn = stylesheetsContainingURI.computeIfAbsent(uri, k -> new HashSet<>());
                uriUsedIn.add(stylesheets);
                
                List<MonitoredStylesheet> monitored = monitoredByURI.computeIfAbsent(uri, (u) -> new ArrayList<>());
                final MonitoredStylesheet ms = createMonitoredStylesheet(origin, uri);
                monitored.add(ms);

                evaluateSourceMappingForURI(origin, uri);
            }
        }

        private MonitoredStylesheet createMonitoredStylesheet(Object origin, String uri) {
            MonitoredStylesheet ms = new MonitoredStylesheet();
            if (origin instanceof Scene) {
                ms.setScene((Scene) origin);
            }
            if (origin instanceof Parent) {
                ms.setParent((Parent) origin);
            }
            ms.setOriginalURI(uri);
            return ms;
        }

        private void evaluateSourceMappingForURI(Object origin, String uri) {
            for (Function<String, Path> c : converters) {
                Path sourceFile = c.apply(uri);

                if (sourceFile != null) {
                    logger(CSSFXMonitor.class).info("css[%s] will be mapped to source[%s] for [%s]", uri, sourceFile, origin);
                    Path directory = sourceFile.getParent();

                    // let's see if other mappings were not waiting for a source mapping
                    List<MonitoredStylesheet> monitored = monitoredByURI.get(uri);

                    for (MonitoredStylesheet ms : monitored) {
                        if (!registered.contains(ms)) {
                            ms.setSource(sourceFile);
                            Runnable msUpdater = new MonitoredStylesheetUpdater(ms, notifier);
                            wp.monitor(directory.toAbsolutePath().normalize(), sourceFile.toAbsolutePath().normalize(), msUpdater);
                            registered.add(ms);
                        }
                        
                        notifier.accept(CSSFXEvent.newEvent(EventType.STYLESHEET_MONITORED, ms));
                    } 
                    
                    break;
                }
            }
        }

        private void unregister(Object origin, String uri) {
            if (origin == null) {
                return;
            }
            List<MonitoredStylesheet> monitored = monitoredByURI.get(uri);
            for (MonitoredStylesheet ms : monitored) {
                if (origin.equals(ms.getParent()) || origin.equals(ms.getScene())) {
                    notifier.accept(CSSFXEvent.newEvent(EventType.STYLESHEET_REMOVED, ms));
                }
            }
        }

        /**
         * Reevaluate not mapped uris
         */
        @SuppressWarnings("unused")
        private void reevaluate() {
            for (String uri : stylesheetsContainingURI.keySet()) {
                List<MonitoredStylesheet> monitored = monitoredByURI.get(uri);
                for (MonitoredStylesheet ms : monitored) {
                    evaluateSourceMappingForURI((ms.getParent()!=null)?ms.getParent():ms.getScene(),  uri);
                }
            }
        }
    }

    private class StyleSheetChangeListener implements ListChangeListener<String> {
        private URIRegistrar registrar;
        private Object origin;

        private StyleSheetChangeListener(URIRegistrar registrar, Object o) {
            this.registrar = registrar;
            this.origin = o;
        }
        
        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (String removedURI : c.getRemoved()) {
                        if (!isAManagedSourceURI(removedURI)) {
                            registrar.unregister(origin, removedURI);
                        }
                    }
                }
                if (c.wasAdded()) {
                    for (String newURI : c.getAddedSubList()) {
                        if (!isAManagedSourceURI(newURI)) {
                            registrar.register(origin, newURI, c.getList());
                        }
                    }
                }
            }
        }
    }

    private static class MonitoredStylesheetUpdater implements Runnable {
        private MonitoredStylesheet ms;
        private ObservableList<String> cssURIs;
        private Consumer<CSSFXEvent<?>> notifier;

        MonitoredStylesheetUpdater(MonitoredStylesheet ms, Consumer<CSSFXEvent<?>> notifier) {
            this.ms = ms;
            this.notifier = notifier;
            if (ms.getParent() != null) {
                cssURIs = ms.getParent().getStylesheets();
            } else if (ms.getScene() != null) {
                cssURIs = ms.getScene().getStylesheets();
            }
        }
        
        @Override
        public void run() {
            IntegerProperty positionIndex = new SimpleIntegerProperty();
            String originalURI = ms.getOriginalURI();
            String sourceURI = ms.getSource().toUri().toString();
            
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
                notifier.accept(CSSFXEvent.newEvent(EventType.STYLESHEET_REPLACED, ms));
            });            
        }
    }
}
