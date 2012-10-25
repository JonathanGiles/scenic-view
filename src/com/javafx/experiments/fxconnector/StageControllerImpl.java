/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.javafx.experiments.fxconnector;

import java.util.*;

import javafx.animation.Animation;
import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.*;
import javafx.collections.ListChangeListener;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;

import com.javafx.experiments.fxconnector.details.*;
import com.javafx.experiments.fxconnector.event.*;
import com.javafx.experiments.fxconnector.event.FXConnectorEvent.SVEventType;
import com.javafx.experiments.fxconnector.gui.*;
import com.javafx.experiments.fxconnector.helper.StyleSheetRefresher;
import com.javafx.experiments.fxconnector.node.*;
import com.sun.scenario.ToolkitAccessor;

public class StageControllerImpl implements StageController {

    public final StageID stageID;

    private StyleSheetRefresher refresher;

    private Parent overlayParent;
    private Parent target;
    private Scene targetScene;
    public Window targetWindow;
    /**
     * Simplification for now, only a plain structure for now
     */
    final List<PopupWindow> popupWindows = new ArrayList<PopupWindow>();

    private final Rectangle boundsInParentRect;
    private final Rectangle layoutBoundsRect;
    private final Line baselineLine;
    private Node componentHighLighter;
    private RuleGrid grid;

    private FXConnectorEventDispatcher dispatcher;

    private SubWindowChecker windowChecker;

    private final InvalidationListener targetScenePropListener;
    private final InvalidationListener targetWindowPropListener;
    private final InvalidationListener targetWindowSceneListener;
    private final InvalidationListener targetSceneRootListener;

    private final InvalidationListener selectedNodePropListener;

    private final Map<Node, PropertyTracker> propertyTrackers = new HashMap<Node, PropertyTracker>();

    private final Configuration configuration = new Configuration();

    boolean remote;

    AllDetails details;

    private final EventHandler<? super MouseEvent> sceneHoverListener = new EventHandler<MouseEvent>() {

        @Override public void handle(final MouseEvent ev) {
            try {
                highlightHovered(ev.getX(), ev.getY());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

    };

    private final EventHandler<? super MouseEvent> scenePressListener = new EventHandler<MouseEvent>() {

        @Override public void handle(final MouseEvent ev) {
            dispatchEvent(new NodeSelectedEvent(getID(), createNode(getHoveredNode(ev.getX(), ev.getY()))));
        }

    };

    /**
     * Listeners and EventHandlers
     */
    private final EventHandler<? super Event> traceEventHandler = new EventHandler<Event>() {

        @Override public void handle(final Event event) {
            if (configuration.isEventLogEnabled()) {
                dispatchEvent(new EvLogEvent(getID(), createNode((Node) event.getSource()), event.getEventType().toString(), ""));
            }
        }
    };

    private final ListChangeListener<Node> structureInvalidationListener;
    private final ChangeListener<Boolean> visibilityInvalidationListener;

    private Node previousHightLightedData;
    final AppController appController;
    int nodeCount;

    private final EventHandler<? super MouseEvent> mousePosListener = new EventHandler<MouseEvent>() {

        @Override public void handle(final MouseEvent ev) {
            dispatchEvent(new MousePosEvent(getID(), (int) ev.getSceneX() + "x" + (int) ev.getSceneY()));
        }
    };

    private final EventHandler<? super KeyEvent> shortcutsHandler;

    public StageControllerImpl(final Stage stage, final AppController appController) {
        this(stage.getScene().getRoot(), appController, true);
    }

    public StageControllerImpl(final Parent target, final AppController appController, final boolean realStageController) {
        this.appController = appController;
        this.stageID = new StageID(appController.getID(), ConnectorUtils.getNodeUniqueID(target));
        targetScenePropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable value) {
                updateSceneDetails();
            }
        };

        targetWindowPropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable value) {
                updateWindowDetails();
            }
        };
        targetWindowSceneListener = new InvalidationListener() {

            @Override public void invalidated(final Observable arg0) {
                if (realStageController) {
                    setTarget(targetWindow.getScene().getRoot());
                    update();
                }
            }
        };
        targetSceneRootListener = new InvalidationListener() {

            @Override public void invalidated(final Observable arg0) {
                if (realStageController) {
                    setTarget(targetScene.getRoot());
                    update();
                }
            }
        };
        selectedNodePropListener = new InvalidationListener() {

            boolean recursive;

            @Override public void invalidated(final Observable arg0) {
                /**
                 * Prevent stackOverflow
                 */
                if (!recursive) {
                    recursive = true;
                    updateBoundsRects();
                    recursive = false;
                }
            }
        };
        shortcutsHandler = new EventHandler<KeyEvent>() {

            @Override public void handle(final KeyEvent ev) {
                if (isSVEvent(ev)) {
                    dispatchEvent(new ShortcutEvent(getID(), ev.getCode()));
                }

            }

            private boolean isSVEvent(final KeyEvent ev) {
                return ev.isControlDown() && ev.isShiftDown();
            }
        };

        visibilityInvalidationListener = new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean arg1, final Boolean newValue) {
                try {
                    if (configuration.isAutoRefreshSceneGraph()) {
                        @SuppressWarnings("unchecked") final Node bean = (Node) ((Property<Boolean>) observable).getBean();
                        final boolean filteringActive = configuration.isVisibilityFilteringActive();
                        if (filteringActive && !newValue) {
                            removeNode(bean, false);
                        } else if (filteringActive && newValue) {
                            addNewNode(bean);
                        } else {
                            /**
                             * This should be improved ideally we use request a
                             * repaint for the TreeItem
                             */
                            removeNode(bean, false);
                            addNewNode(bean);
                        }
                    }
                } catch (final Exception e) {
                    // Protect the application from ScenicView problems
                    e.printStackTrace();
                }
            }
        };

        structureInvalidationListener = new ListChangeListener<Node>() {
            @Override public void onChanged(final Change<? extends Node> c) {
                try {
                    if (configuration.isAutoRefreshSceneGraph()) {
                        int difference = 0;
                        while (c.next()) {
                            for (final Node dead : c.getRemoved()) {
                                final SVNode node = createNode(dead);
                                dispatchEvent(new EvLogEvent(getID(), node, EvLogEvent.NODE_REMOVED, ""));
                                removeNode(dead, true);
                                if (SCUtils.isNormalNode(dead)) {
                                    difference -= ConnectorUtils.getBranchCount(dead);
                                }
                            }
                            for (final Node alive : c.getAddedSubList()) {
                                final SVNode node = createNode(alive);
                                dispatchEvent(new EvLogEvent(getID(), node, EvLogEvent.NODE_ADDED, ""));

                                addNewNode(alive);
                                if (SCUtils.isNormalNode(alive)) {
                                    difference += ConnectorUtils.getBranchCount(alive);
                                }
                            }
                        }
                        if (difference != 0) {
                            setNodeCount(nodeCount + difference);
                            dispatchEvent(new NodeCountEvent(getID(), nodeCount));
                        }
                    }
                } catch (final Exception e) {
                    // Protect the application from ScenicView problems
                    e.printStackTrace();
                }
            }
        };

        boundsInParentRect = new Rectangle();
        boundsInParentRect.setId(StageController.FX_CONNECTOR_BASE_ID + "boundsInParentRect");
        boundsInParentRect.setFill(Color.YELLOW);
        boundsInParentRect.setOpacity(.5);
        boundsInParentRect.setManaged(false);
        boundsInParentRect.setMouseTransparent(true);
        layoutBoundsRect = new Rectangle();
        layoutBoundsRect.setId(StageController.FX_CONNECTOR_BASE_ID + "layoutBoundsRect");
        layoutBoundsRect.setFill(null);
        layoutBoundsRect.setStroke(Color.GREEN);
        layoutBoundsRect.setStrokeType(StrokeType.INSIDE);
        layoutBoundsRect.setOpacity(.8);
        layoutBoundsRect.getStrokeDashArray().addAll(3.0, 3.0);
        layoutBoundsRect.setStrokeWidth(1);
        layoutBoundsRect.setManaged(false);
        layoutBoundsRect.setMouseTransparent(true);
        baselineLine = new Line();
        baselineLine.setId(StageController.FX_CONNECTOR_BASE_ID + "baselineLine");
        baselineLine.setStroke(Color.RED);
        baselineLine.setOpacity(.75);
        baselineLine.setStrokeWidth(1);
        baselineLine.setManaged(false);
        this.target = target;
    }

    @Override public void close() {
        SCUtils.removeScenicViewComponents(target);
        if (targetScene != null) {
            targetScene.removeEventFilter(MouseEvent.MOUSE_MOVED, sceneHoverListener);
            targetScene.removeEventFilter(MouseEvent.MOUSE_MOVED, mousePosListener);
            targetScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, scenePressListener);
        }
        /**
         * Remove the window listeners
         */
        setTargetWindow(null);
        updateListeners(target, false, true);
        if (refresher != null)
            refresher.finish();
        if (windowChecker != null)
            windowChecker.finish();
        dispatcher = null;
    }

    @Override public void setEventDispatcher(final FXConnectorEventDispatcher model2gui) {
        this.dispatcher = model2gui;
        windowChecker = new SubWindowChecker(this);
        windowChecker.start();
        details = new AllDetails(model2gui, getID());
        setTarget(target);
        update();
    }

    @Override public boolean isOpened() {
        return dispatcher != null;
    }

    private void startRefresher() {
        refresher = new StyleSheetRefresher(targetScene);
    }

    private void updateBoundsRects(final Node selectedNode) {
        /**
         * By node layout bounds only on main scene not on popups
         */
        if (selectedNode != null && selectedNode.getScene() == targetScene) {
            SCUtils.updateRect(overlayParent, selectedNode, selectedNode.getBoundsInParent(), 0, 0, boundsInParentRect);
            SCUtils.updateRect(overlayParent, selectedNode, selectedNode.getLayoutBounds(), selectedNode.getLayoutX(), selectedNode.getLayoutY(), layoutBoundsRect);
            boundsInParentRect.setVisible(true);
            layoutBoundsRect.setVisible(true);
        } else {
            boundsInParentRect.setVisible(false);
            layoutBoundsRect.setVisible(false);
        }
    }

    @Override public void update() {
        updateListeners(target, true, false);
        SVNode root = createNode(target);
        /**
         * If the target is the root node of the scene include subwindows
         */
        if (targetScene != null && targetScene.getRoot() == target) {
            String title = "App";
            Image targetStageImage = null;
            if (targetScene.getWindow() instanceof Stage) {
                final Stage s = ((Stage) targetScene.getWindow());
                if (!s.getIcons().isEmpty()) {
                    targetStageImage = ((Stage) targetScene.getWindow()).getIcons().get(0);
                }
                title = s.getTitle() != null ? s.getTitle() : "App";
            }
            final SVDummyNode app = new SVDummyNode(title, "Stage", getID().getStageID(), NodeType.STAGE);
            app.setIcon(targetStageImage);
            app.setRemote(remote);
            app.setExpanded(true);
            app.getChildren().add(root);
            if (!popupWindows.isEmpty()) {
                final SVNode subWindows = new SVDummyNode("SubWindows", "Popup", getID().getStageID(), NodeType.SUBWINDOWS_ROOT);
                for (int i = 0; i < popupWindows.size(); i++) {
                    final PopupWindow window = popupWindows.get(i);
                    final SVNode subWindow = new SVDummyNode("SubWindow -" + ConnectorUtils.nodeClass(window), ConnectorUtils.nodeClass(window), window.hashCode(), NodeType.SUBWINDOW);
                    subWindow.getChildren().add(createNode(window.getScene().getRoot()));
                    subWindows.getChildren().add(subWindow);
                }
                app.getChildren().add(subWindows);
            }
            root = app;
        }
        dispatchEvent(new NodeAddRemoveEvent(SVEventType.ROOT_UPDATED, getID(), root));
        updateSceneDetails();
    }

    private void updateSceneDetails() {
        // hack, since we can't listen for a STAGE prop change on scene
        setNodeCount(ConnectorUtils.getBranchCount(target));
        if (dispatcher != null) {
            dispatchEvent(new SceneDetailsEvent(getID(), nodeCount, targetScene != null ? ConnectorUtils.format(targetScene.getWidth()) + " x " + ConnectorUtils.format(targetScene.getHeight()) : ""));
        }
        if (targetScene != null && targetWindow == null) {
            setTargetWindow(targetScene.getWindow());
        }
    }

    private void setTargetWindow(final Window value) {
        if (targetWindow != null) {
            targetWindow.xProperty().removeListener(targetWindowPropListener);
            targetWindow.yProperty().removeListener(targetWindowPropListener);
            targetWindow.widthProperty().removeListener(targetWindowPropListener);
            targetWindow.heightProperty().removeListener(targetWindowPropListener);
            targetWindow.focusedProperty().removeListener(targetWindowPropListener);
            targetWindow.sceneProperty().removeListener(targetWindowSceneListener);
        }
        targetWindow = value;
        if (targetWindow != null) {
            targetWindow.xProperty().addListener(targetWindowPropListener);
            targetWindow.yProperty().addListener(targetWindowPropListener);
            targetWindow.widthProperty().addListener(targetWindowPropListener);
            targetWindow.heightProperty().addListener(targetWindowPropListener);
            targetWindow.focusedProperty().addListener(targetWindowPropListener);
            targetWindow.sceneProperty().addListener(targetWindowSceneListener);
            if (targetWindow instanceof Stage)
                stageID.setName(((Stage) targetWindow).getTitle());
        }
        updateWindowDetails();
    }

    private void updateWindowDetails() {
        if (dispatcher != null) {
            if (targetWindow != null) {
                dispatchEvent(new WindowDetailsEvent(getID(), targetWindow.getClass().getSimpleName(), ConnectorUtils.boundsToString(targetWindow.getX(), targetWindow.getY(), targetWindow.getWidth(), targetWindow.getHeight()), targetWindow.isFocused(), canStylesheetsBeRefreshed()));
            } else {
                dispatchEvent(new WindowDetailsEvent(getID(), null, "", false, false));
            }
        }
    }

    private void setTarget(final Parent value) {
        // Find parent we can use to hang bounds rectangles
        this.target = value;
        if (overlayParent != null) {
            SCUtils.removeFromNode(overlayParent, boundsInParentRect);
            SCUtils.removeFromNode(overlayParent, layoutBoundsRect);
            SCUtils.removeFromNode(overlayParent, baselineLine);
        }
        overlayParent = SCUtils.findFertileParent(value);
        if (overlayParent == null) {
            System.out.println("warning: could not find writable parent to add overlay nodes; overlays disabled.");
            /**
             * This should be improved
             */
            configuration.setShowBounds(false);
            updateBoundsRects();
            configuration.setShowBounds(true);
            configuration.setShowBaseline(false);
            updateBaseline();
            configuration.setShowBaseline(true);

        } else {
            SCUtils.addToNode(overlayParent, boundsInParentRect);
            SCUtils.addToNode(overlayParent, layoutBoundsRect);
            SCUtils.addToNode(overlayParent, baselineLine);
        }
        setTargetScene(target.getScene());
    }

    private void componentSelectOnClick(final boolean newValue) {
        if (newValue) {
            targetScene.addEventFilter(MouseEvent.MOUSE_MOVED, sceneHoverListener);
            targetScene.addEventFilter(MouseEvent.MOUSE_PRESSED, scenePressListener);

            ((Stage) targetWindow).toFront();
        } else {
            targetScene.removeEventFilter(MouseEvent.MOUSE_MOVED, sceneHoverListener);
            targetScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, scenePressListener);
            if (componentHighLighter != null) {
                SCUtils.removeFromNode(target, componentHighLighter);
            }
        }
    }

    private void showGrid(final boolean newValue, final int gap, final Color color) {
        if (newValue) {
            grid = new RuleGrid(gap, targetScene.getWidth(), targetScene.getHeight());
            grid.setId(StageController.FX_CONNECTOR_BASE_ID + "ruler");
            grid.setManaged(false);
            grid.setStroke(color);
            SCUtils.addToNode(target, grid);
        } else {
            if (grid != null) {
                SCUtils.removeFromNode(target, grid);
                grid = null;
            }
        }
    }

    private void updateBaseline(final boolean show, final Point2D orig, final double width) {
        if (show) {
            final Point2D pt = overlayParent.sceneToLocal(orig);
            baselineLine.setStartX(pt.getX());
            baselineLine.setStartY(pt.getY());
            baselineLine.setEndX(pt.getX() + width);
            baselineLine.setEndY(pt.getY());
            baselineLine.setVisible(true);
        } else {
            baselineLine.setVisible(false);
        }
    }

    private void setTargetScene(final Scene value) {

        if (targetScene != null) {
            targetScene.widthProperty().removeListener(targetScenePropListener);
            targetScene.heightProperty().removeListener(targetScenePropListener);
            targetScene.rootProperty().removeListener(targetSceneRootListener);
        }
        targetScene = value;
        if (targetScene != null) {
            setTargetWindow(targetScene.getWindow());
            targetScene.widthProperty().addListener(targetScenePropListener);
            targetScene.heightProperty().addListener(targetScenePropListener);
            targetScene.rootProperty().addListener(targetSceneRootListener);
            targetScene.removeEventFilter(MouseEvent.MOUSE_MOVED, mousePosListener);
            targetScene.addEventFilter(MouseEvent.MOUSE_MOVED, mousePosListener);
            final boolean canBeRefreshed = StyleSheetRefresher.canStylesBeRefreshed(targetScene);

            if (refresher == null || refresher.getScene() != value) {
                if (refresher != null)
                    refresher.finish();
                if (canBeRefreshed && configuration.isAutoRefreshStyles())
                    startRefresher();
            }
        }
        updateSceneDetails();
    }

    private void highlightHovered(final double x, final double y) {
        final Node nodeData = getHoveredNode(x, y);
        if (previousHightLightedData != nodeData) {
            previousHightLightedData = null;
            if (componentHighLighter != null) {
                SCUtils.removeFromNode(target, componentHighLighter);
            }
            if (nodeData != null) {
                // TODO Change this
                componentHighLighter = new ComponentHighLighter(createNode(nodeData), targetWindow != null ? targetWindow.getWidth() : -1, targetWindow != null ? targetWindow.getHeight() : -1, SCUtils.toSceneBounds(overlayParent, nodeData, nodeData.getBoundsInParent(), 0, 0));
                SCUtils.addToNode(target, componentHighLighter);
            }
        }

    }

    private Node getHoveredNode(final double x, final double y) {
        return SCUtils.getHoveredNode(configuration, target, x, y);
    }

    private boolean canStylesheetsBeRefreshed() {
        return StyleSheetRefresher.canStylesBeRefreshed(targetScene);
    }

    @Override public void configurationUpdated(final Configuration configuration) {
        if (configuration.isRegisterShortcuts() != this.configuration.isRegisterShortcuts()) {
            this.configuration.setRegisterShortcuts(configuration.isRegisterShortcuts());
            updateShortcuts();
        }
        if (configuration.isShowBaseline() != this.configuration.isShowBaseline()) {
            this.configuration.setShowBaseline(configuration.isShowBaseline());
            updateBaseline();
        }
        if (configuration.isShowBounds() != this.configuration.isShowBounds()) {
            this.configuration.setShowBounds(configuration.isShowBounds());
            updateBoundsRects();
        }
        if (configuration.isShowRuler() != this.configuration.isShowRuler()) {
            final Color color = Color.web(configuration.getRulerColor());
            showGrid(configuration.isShowRuler(), configuration.getRulerSeparation(), color);
            this.configuration.setShowRuler(configuration.isShowRuler());
            this.configuration.setRulerSeparation(configuration.getRulerSeparation());
        } else if (configuration.getRulerSeparation() != this.configuration.getRulerSeparation() && grid != null) {
            grid.updateSeparation(configuration.getRulerSeparation());
            this.configuration.setRulerSeparation(configuration.getRulerSeparation());
        }
        if (configuration.isAutoRefreshStyles() != this.configuration.isAutoRefreshStyles()) {
            this.configuration.setAutoRefreshStyles(configuration.isAutoRefreshStyles());
            if (this.configuration.isAutoRefreshStyles()) {
                startRefresher();
            } else {
                refresher.finish();
            }
        }
        if (configuration.isComponentSelectOnClick() != this.configuration.isComponentSelectOnClick()) {
            this.configuration.setComponentSelectOnClick(configuration.isComponentSelectOnClick());
            componentSelectOnClick(configuration.isComponentSelectOnClick());
        }
        if (!configuration.getRulerColor().equals(this.configuration.getRulerColor())) {
            this.configuration.setRulerColor(configuration.getRulerColor());
            if (grid != null)
                grid.setStroke(Color.web(configuration.getRulerColor()));
        }
        this.configuration.setAutoRefreshSceneGraph(configuration.isAutoRefreshSceneGraph());
        this.configuration.setEventLogEnabled(configuration.isEventLogEnabled());
        this.configuration.setIgnoreMouseTransparent(configuration.isIgnoreMouseTransparent());
        this.configuration.setCollapseContentControls(configuration.isCollapseContentControls());
        this.configuration.setCollapseControls(configuration.isCollapseControls());
        this.configuration.setVisibilityFilteringActive(configuration.isVisibilityFilteringActive());
        this.configuration.setCSSPropertiesDetail(configuration.isCSSPropertiesDetail());

        details.setShowCSSProperties(this.configuration.isCSSPropertiesDetail());
        update();
    }

    private void updateShortcuts() {
        if (configuration.isRegisterShortcuts()) {
            targetScene.addEventFilter(KeyEvent.KEY_PRESSED, shortcutsHandler);
        } else {
            targetScene.removeEventFilter(KeyEvent.KEY_PRESSED, shortcutsHandler);
        }
    }

    @Override public void setSelectedNode(final SVNode svNode) {
        final Node old = selectedNode;
        if (old != null) {
            old.boundsInParentProperty().removeListener(selectedNodePropListener);
            old.layoutBoundsProperty().removeListener(selectedNodePropListener);
        }
        if (svNode != null) {
            if (svNode.getNodeType() == NodeType.REAL_NODE) {
                this.selectedNode = svNode.getImpl();
            } else {
                this.selectedNode = SCUtils.findNode(target, svNode.getNodeId());
            }
            if (selectedNode != null) {
                selectedNode.boundsInParentProperty().addListener(selectedNodePropListener);
                selectedNode.layoutBoundsProperty().addListener(selectedNodePropListener);
            }
        } else {
            selectedNode = null;
        }
        updateBoundsRects();
        updateBaseline();
        details.setTarget(this.selectedNode);
    }

    private Node selectedNode;

    private void updateBaseline() {
        if (this.configuration.isShowBaseline() && selectedNode != null) {
            final double baseline = selectedNode.getBaselineOffset();
            final Bounds bounds = selectedNode.getLayoutBounds();
            updateBaseline(true, selectedNode.localToScene(bounds.getMinX(), bounds.getMinY() + baseline), bounds.getWidth());

        } else {
            updateBaseline(false, null, 0);
        }
    }

    private void updateBoundsRects() {
        if (this.configuration.isShowBounds()) {
            updateBoundsRects(selectedNode);
        } else {
            updateBoundsRects(null);
        }
    }

    void propertyTracker(final Node node, final boolean add) {
        PropertyTracker tracker = propertyTrackers.remove(node);
        if (tracker != null) {
            tracker.clear();
        }
        if (add && configuration.isEventLogEnabled()) {
            tracker = new PropertyTracker() {

                @Override protected void updateDetail(final String propertyName, @SuppressWarnings("rawtypes") final ObservableValue property) {
                    /**
                     * Remove the bean
                     */
                    dispatchEvent(new EvLogEvent(getID(), createNode(node), EvLogEvent.PROPERTY_CHANGED, propertyName + "=" + property.getValue()));
                }
            };
            tracker.setTarget(node);
            propertyTrackers.put(node, tracker);
        }
    }

    @Override public StageID getID() {
        return stageID;
    }

    public void placeStage(final Stage stage) {
        if (targetWindow != null) {
            stage.setX(targetWindow.getX() + targetWindow.getWidth());
            stage.setY(targetWindow.getY());
            try {
                // Prevents putting the stage out of the screen
                final Screen primary = Screen.getPrimary();
                if (primary != null) {
                    final Rectangle2D rect = primary.getVisualBounds();
                    if (stage.getX() + stage.getWidth() > rect.getMaxX()) {
                        stage.setX(rect.getMaxX() - stage.getWidth());
                    }
                    if (stage.getY() + stage.getHeight() > rect.getMaxY()) {
                        stage.setX(rect.getMaxY() - stage.getHeight());
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addNewNode(final Node node) {
        if (SCUtils.isNormalNode(node)) {
            updateListeners(node, true, false);
            final SVNode svNode = createNode(node);
            dispatchEvent(new NodeAddRemoveEvent(SVEventType.NODE_ADDED, getID(), svNode));
        }
    }

    private void removeNode(final Node node, final boolean removeVisibilityListener) {
        if (SCUtils.isNormalNode(node)) {
            updateListeners(node, false, removeVisibilityListener);
            dispatchEvent(new NodeAddRemoveEvent(SVEventType.NODE_REMOVED, getID(), createNode(node)));
        }
    }

    private void updateListeners(final Node node, final boolean add, final boolean removeVisibilityListener) {
        if (add) {
            if (SCUtils.isNormalNode(node)) {
                node.visibleProperty().removeListener(visibilityInvalidationListener);
                node.visibleProperty().addListener(visibilityInvalidationListener);
                propertyTracker(node, true);

                node.removeEventFilter(Event.ANY, traceEventHandler);
                if (configuration.isEventLogEnabled())
                    node.addEventFilter(Event.ANY, traceEventHandler);
                if (node instanceof Parent) {
                    ((Parent) node).getChildrenUnmodifiable().removeListener(structureInvalidationListener);
                    ((Parent) node).getChildrenUnmodifiable().addListener(structureInvalidationListener);
                    final List<Node> childrens = ((Parent) node).getChildrenUnmodifiable();
                    for (int i = 0; i < childrens.size(); i++) {
                        updateListeners(childrens.get(i), add, removeVisibilityListener);
                    }
                }
            }
        } else {
            if (node instanceof Parent) {
                final List<Node> childrens = ((Parent) node).getChildrenUnmodifiable();
                for (int i = 0; i < childrens.size(); i++) {
                    /**
                     * If we are removing a node: 1) If it is a real node
                     * removal removeVisibilityListener is true 2) If it is a
                     * visibility remove we should remove the visibility
                     * listeners of its childrens because the visibility is
                     * reduced by their parent
                     */
                    updateListeners(childrens.get(i), add, true);
                }
                ((Parent) node).getChildrenUnmodifiable().removeListener(structureInvalidationListener);
            }
            if (node != null && removeVisibilityListener) {
                node.visibleProperty().removeListener(visibilityInvalidationListener);
                propertyTracker(node, false);
                node.removeEventFilter(Event.ANY, traceEventHandler);
            }
        }
    }

    private SVNode createNode(final Node node) {
        return SVNodeFactory.createNode(node, configuration, remote);
    }

    @Override public AppController getAppController() {
        return appController;
    }

    public void setRemote(final boolean remote) {
        this.remote = remote;
    }

    private void setNodeCount(final int value) {
        this.nodeCount = value;
    }

    @Override public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
        details.setDetail(detailType, detailID, value);
    }

    @Override public void animationsEnabled(final boolean enabled) {
        if (enabled) {
            ToolkitAccessor.getMasterTimer().resume();
        } else {
            ToolkitAccessor.getMasterTimer().pause();
        }
    }

    @Override public void updateAnimations() {
        final List<Animation> animations = ConnectorUtils.getAnimations();
        final List<SVAnimation> svAnimations = new ArrayList<SVAnimation>();
        for (int i = 0; i < animations.size(); i++) {
            final Animation a = animations.get(i);
            svAnimations.add(new SVAnimation(ConnectorUtils.getAnimationUniqueID(a), animations.get(i)));
        }
        dispatchEvent(new AnimationsCountEvent(getID(), svAnimations));
    }

    @Override public void pauseAnimation(final int animationID) {
        final List<Animation> animations = ConnectorUtils.getAnimations();
        for (final Iterator<Animation> iterator = animations.iterator(); iterator.hasNext();) {
            final Animation animation = iterator.next();
            if (ConnectorUtils.getAnimationUniqueID(animation) == animationID) {
                animation.pause();
            }
        }

    }

    private final void dispatchEvent(final FXConnectorEvent event) {
        if (dispatcher != null) {
            dispatcher.dispatchEvent(event);
        }
    }

}
