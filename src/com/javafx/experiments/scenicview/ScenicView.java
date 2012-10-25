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
package com.javafx.experiments.scenicview;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.value.*;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;

import javax.swing.JOptionPane;

import com.javafx.experiments.fxconnector.*;
import com.javafx.experiments.fxconnector.details.Detail;
import com.javafx.experiments.fxconnector.event.*;
import com.javafx.experiments.fxconnector.event.FXConnectorEvent.SVEventType;
import com.javafx.experiments.fxconnector.node.SVNode;
import com.javafx.experiments.scenicview.ScenegraphTreeView.ConnectorController;
import com.javafx.experiments.scenicview.control.*;
import com.javafx.experiments.scenicview.details.*;
import com.javafx.experiments.scenicview.details.GDetailPane.RemotePropertySetter;
import com.javafx.experiments.scenicview.dialog.*;
import com.javafx.experiments.scenicview.update.*;
import com.javafx.experiments.scenicview.utils.*;

/**
 * 
 */
public class ScenicView extends Region implements ConnectorController, CParent {

    private static final String HELP_URL = "http://fxexperience.com/scenic-view/help";
    public static final String STYLESHEETS = "com/javafx/experiments/scenicview/scenicview.css";
    public static final Image APP_ICON = DisplayUtils.getUIImage("mglass.gif");

    public static final String VERSION = "1.3.0";
    // the Stage used to show Scenic View
    private final Stage scenicViewStage;

    private final Thread shutdownHook = new Thread() {
        @Override public void run() {
            // We can't use close() because we are not in FXThread
            saveProperties();
        }
    };

    private final BorderPane borderPane;
    private final SplitPane splitPane;
    private final ScenegraphTreeView treeView;
    private final AllDetailsPane allDetailsPane;
    private final EventLogPane eventLogPane;
    private final AnimationsPane animationsPane;
    private static StatusBar statusBar;
    private final VBox leftPane;

    FilterTextField propertyFilterField;

    /**
     * Menu Options
     */
    protected final MenuBar menuBar;
    private final CheckMenuItem showFilteredNodesInTree;
    private final CheckMenuItem showNodesIdInTree;
    private final CheckMenuItem autoRefreshStyleSheets;
    private final CheckMenuItem componentSelectOnClick;

    private final Configuration configuration = new Configuration();
    private final List<FXConnectorEvent> eventQueue = new LinkedList<FXConnectorEvent>();

    private UpdateStrategy updateStrategy;
    private long lastMousePosition;
    private static boolean debug = false;

    private final FXConnectorEventDispatcher stageModelListener = new FXConnectorEventDispatcher() {

        @Override public void dispatchEvent(final FXConnectorEvent appEvent) {
            if (isValid(appEvent)) {
                // doDispatchEvent(appEvent);
                switch (appEvent.getType()) {

                case ROOT_UPDATED:
                    doDispatchEvent(appEvent);
                    break;

                case MOUSE_POSITION:
                    if (System.currentTimeMillis() - lastMousePosition > 500) {
                        lastMousePosition = System.currentTimeMillis();
                        // No need to synchronize here
                        eventQueue.add(appEvent);
                    }
                    break;

                default:
                    // No need to synchronize here
                    eventQueue.add(appEvent);
                    // if (eventQueue.size() > 1)
                    // System.out.println("QUEUE SIZE:" + eventQueue.size());
                    break;
                }

            } else {
                debug("Unused event " + appEvent);
            }
        }

        private boolean isValid(final FXConnectorEvent appEvent) {
            for (int i = 0; i < apps.size(); i++) {
                if (apps.get(i).getID() == appEvent.getStageID().getAppID()) {
                    final List<StageController> stages = apps.get(i).getStages();
                    for (int j = 0; j < stages.size(); j++) {
                        if (stages.get(j).getID().getStageID() == appEvent.getStageID().getStageID()) {
                            return true;
                        }
                    }
                    break;
                }
            }
            return false;
        }
    };

    private final List<AppController> apps = new ArrayList<AppController>();
    StageController activeStage;
    private SVNode selectedNode;
    private ProgressWebView wview;
    private Tab javadocTab;
    private TabPane tabPane;
    private Tab detailsTab;
    private Tab eventsTab;

    public ScenicView(final UpdateStrategy updateStrategy, final Stage senicViewStage) {
        Persistence.loadProperties();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        setId(StageController.FX_CONNECTOR_BASE_ID + "scenic-view");

        borderPane = new BorderPane();
        borderPane.setId("main-borderpane");

        final List<NodeFilter> activeNodeFilters = new ArrayList<NodeFilter>();

        /**
         * Create a filter for our own nodes
         */
        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return false;
            }

            @Override public boolean accept(final SVNode node) {
                // do not create tree nodes for our bounds rectangles
                return ConnectorUtils.isNormalNode(node);
            }

            @Override public boolean ignoreShowFilteredNodesInTree() {
                return true;
            }

            @Override public boolean expandAllNodes() {
                return false;
            }
        });

        propertyFilterField = createFilterField("Property name or value", null);
        propertyFilterField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                filterProperties(propertyFilterField.getText());
            }
        });
        propertyFilterField.setDisable(true);

        eventLogPane = new EventLogPane(this);
        eventLogPane.activeProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setEventLogEnabled(newValue);
                configurationUpdated();
            }
        });

        menuBar = new MenuBar();
        // menuBar.setId("main-menubar");

        // ---- File Menu
        final MenuItem classpathItem = new MenuItem("Configure Classpath");
        classpathItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                final Properties properties = PropertiesUtils.loadProperties();

                final String toolsPath = properties.getProperty(ScenicViewBooter.TOOLS_JAR_PATH_KEY);
                final String jfxPath = properties.getProperty(ScenicViewBooter.JFXRT_JAR_PATH_KEY);
                if (!ClassPathDialog.hasBeenInited()) {
                    ClassPathDialog.init();
                }
                ClassPathDialog.showDialog(toolsPath, jfxPath, false, new PathChangeListener() {
                    @Override public void onPathChanged(final Map<String, URI> map) {

                        final URI toolsPath = map.get(PathChangeListener.TOOLS_JAR_KEY);
                        final URI jfxPath = map.get(PathChangeListener.JFXRT_JAR_KEY);

                        properties.setProperty(ScenicViewBooter.TOOLS_JAR_PATH_KEY, toolsPath.toASCIIString());
                        properties.setProperty(ScenicViewBooter.JFXRT_JAR_PATH_KEY, jfxPath.toASCIIString());
                        PropertiesUtils.saveProperties();

                        JOptionPane.showMessageDialog(null, "Updated classpath will be used on next Scenic View boot", "Classpath Saved", JOptionPane.INFORMATION_MESSAGE);
                        ClassPathDialog.hideDialog();
                    }
                });
            }
        });

        final MenuItem exitItem = new MenuItem("E_xit Scenic View");
        exitItem.setAccelerator(KeyCombination.keyCombination("CTRL+Q"));
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                close();
                // TODO Why closing the Stage does not dispatch
                // WINDOW_CLOSE_REQUEST??
                scenicViewStage.close();
            }
        });

        final Menu fileMenu = new Menu("File");
        if (updateStrategy.needsClassPathConfiguration()) {
            fileMenu.getItems().addAll(classpathItem, new SeparatorMenuItem());
        }
        fileMenu.getItems().add(exitItem);

        // ---- Options Menu
        final CheckMenuItem showBoundsCheckbox = buildCheckMenuItem("Show Bounds Overlays", "Show the bound overlays on selected", "Do not show bound overlays on selected", "showBounds", Boolean.TRUE);
        showBoundsCheckbox.setId("show-bounds-checkbox");
        // showBoundsCheckbox.setTooltip(new
        // Tooltip("Display a yellow highlight for boundsInParent and green outline for layoutBounds."));
        configuration.setShowBounds(showBoundsCheckbox.isSelected());
        showBoundsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setShowBounds(newValue);
                configurationUpdated();
            }
        });

        final InvalidationListener menuTreeChecksListener = new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                update();
            }
        };
        final CheckMenuItem collapseControls = buildCheckMenuItem("Collapse controls In Tree", "Controls will be collapsed", "Controls will be expanded", "collapseControls", Boolean.TRUE);
        collapseControls.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setCollapseControls(newValue);
                configurationUpdated();
            }
        });
        configuration.setCollapseControls(collapseControls.isSelected());

        final CheckMenuItem collapseContentControls = buildCheckMenuItem("Collapse container controls In Tree", "Container controls will be collapsed", "Container controls will be expanded", "collapseContainerControls", Boolean.FALSE);
        collapseContentControls.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setCollapseContentControls(newValue);
                configurationUpdated();
            }
        });
        collapseContentControls.disableProperty().bind(collapseControls.selectedProperty().not());
        configuration.setCollapseContentControls(collapseContentControls.isSelected());

        final CheckMenuItem showBaselineCheckbox = buildCheckMenuItem("Show Baseline Overlay", "Display a red line at the current node's baseline offset", "Do not show baseline overlay", "showBaseline", Boolean.FALSE);
        showBaselineCheckbox.setId("show-baseline-overlay");
        configuration.setShowBaseline(showBaselineCheckbox.isSelected());
        showBaselineCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setShowBaseline(newValue);
                configurationUpdated();
            }
        });

        final CheckMenuItem automaticScenegraphStructureRefreshing = buildCheckMenuItem("Auto-Refresh Scenegraph", "Scenegraph structure will be automatically updated on change", "Scenegraph structure will NOT be automatically updated on change", "automaticScenegraphStructureRefreshing", Boolean.TRUE);
        automaticScenegraphStructureRefreshing.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setAutoRefreshSceneGraph(automaticScenegraphStructureRefreshing.isSelected());
                configurationUpdated();

            }
        });
        configuration.setAutoRefreshSceneGraph(automaticScenegraphStructureRefreshing.isSelected());

        final CheckMenuItem showInvisibleNodes = buildCheckMenuItem("Show Invisible Nodes In Tree", "Invisible nodes will be faded in the scenegraph tree", "Invisible nodes will not be shown in the scenegraph tree", "showInvisibleNodes", Boolean.FALSE);
        final ChangeListener<Boolean> visilityListener = new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean arg2) {
                configuration.setVisibilityFilteringActive(!showInvisibleNodes.isSelected() && !showFilteredNodesInTree.isSelected());
                configurationUpdated();
            }
        };
        showInvisibleNodes.selectedProperty().addListener(visilityListener);

        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return false;
            }

            @Override public boolean accept(final SVNode node) {
                return showInvisibleNodes.isSelected() || node.isVisible();
            }

            @Override public boolean ignoreShowFilteredNodesInTree() {
                return false;
            }

            @Override public boolean expandAllNodes() {
                return false;
            }
        });

        showNodesIdInTree = buildCheckMenuItem("Show Node IDs", "Node IDs will be shown on the scenegraph tree", "Node IDs will not be shown the Scenegraph tree", "showNodesIdInTree", Boolean.FALSE);
        showNodesIdInTree.selectedProperty().addListener(menuTreeChecksListener);

        showFilteredNodesInTree = buildCheckMenuItem("Show Filtered Nodes In Tree", "Filtered nodes will be faded in the tree", "Filtered nodes will not be shown in tree (unless they are parents of non-filtered nodes)", "showFilteredNodesInTree", Boolean.TRUE);

        showFilteredNodesInTree.selectedProperty().addListener(visilityListener);
        configuration.setVisibilityFilteringActive(!showInvisibleNodes.isSelected() && !showFilteredNodesInTree.isSelected());

        /**
         * Filter invisible nodes only makes sense if showFilteredNodesInTree is
         * not selected
         */
        showInvisibleNodes.disableProperty().bind(showFilteredNodesInTree.selectedProperty());

        componentSelectOnClick = buildCheckMenuItem("Component highlight/select on click", "Click on the scene to select a component", "", null, null);
        componentSelectOnClick.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                selectOnClick(newValue);
            }
        });

        final CheckMenuItem ignoreMouseTransparentNodes = buildCheckMenuItem("Ignore MouseTransparent Nodes", "Transparent nodes will not be selectable", "Transparent nodes can be selected", "ignoreMouseTransparentNodes", Boolean.TRUE);
        ignoreMouseTransparentNodes.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setIgnoreMouseTransparent(newValue);
                configurationUpdated();
            }
        });
        configuration.setIgnoreMouseTransparent(ignoreMouseTransparentNodes.isSelected());

        final CheckMenuItem registerShortcuts = buildCheckMenuItem("Register shortcuts", "SV Keyboard shortcuts will be registered on your app", "SV Keyboard shortcuts will be removed on your app", "registerShortcuts", Boolean.TRUE);
        registerShortcuts.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setRegisterShortcuts(newValue);
                configurationUpdated();
            }
        });
        configuration.setRegisterShortcuts(registerShortcuts.isSelected());

        autoRefreshStyleSheets = buildCheckMenuItem("Auto-Refresh StyleSheets", "A background thread will check modifications on the css files to reload them if needed", "StyleSheets autorefreshing disabled", "autoRefreshStyleSheets", Boolean.FALSE);
        autoRefreshStyleSheets.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setAutoRefreshStyles(newValue);
                configurationUpdated();
            }
        });
        configuration.setAutoRefreshStyles(autoRefreshStyleSheets.isSelected());

        final Menu scenegraphMenu = new Menu("Scenegraph");
        scenegraphMenu.getItems().addAll(automaticScenegraphStructureRefreshing, autoRefreshStyleSheets, registerShortcuts, new SeparatorMenuItem(), componentSelectOnClick, ignoreMouseTransparentNodes);

        final Menu displayOptionsMenu = new Menu("Display Options");

        final Menu ruler = new Menu("Ruler");
        final Slider slider = new Slider(5, 50, 10);
        final TextField sliderValue = new TextField();
        slider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                configuration.setRulerSeparation((int) newValue.doubleValue());
                configurationUpdated();
                sliderValue.setText(ConnectorUtils.format(newValue.doubleValue()));
            }
        });
        final HBox box = new HBox();
        sliderValue.setPrefWidth(40);
        sliderValue.setText(ConnectorUtils.format(slider.getValue()));
        sliderValue.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                final double value = ConnectorUtils.parse(sliderValue.getText());
                if (value >= slider.getMin() && value <= slider.getMax()) {
                    configuration.setRulerSeparation((int) slider.getValue());
                    configurationUpdated();
                    slider.setValue(value);
                } else if (value < slider.getMin()) {
                    sliderValue.setText(ConnectorUtils.format(slider.getMin()));
                    slider.setValue(slider.getMin());
                } else {
                    sliderValue.setText(ConnectorUtils.format(slider.getMax()));
                    slider.setValue(slider.getMax());
                }
            }
        });

        box.getChildren().addAll(slider, sliderValue);
        final CustomMenuItem rulerSlider = new CustomMenuItem(box);

        final CheckMenuItem showRuler = buildCheckMenuItem("Show Ruler", "Show ruler in the scene for alignment purposes", "", null, null);
        showRuler.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                configuration.setShowRuler(newValue.booleanValue());
                configuration.setRulerSeparation((int) slider.getValue());
                configurationUpdated();
            }
        });
        configuration.setRulerSeparation((int) slider.getValue());
        rulerSlider.disableProperty().bind(showRuler.selectedProperty().not());
        slider.disableProperty().bind(showRuler.selectedProperty().not());

        final ColorMenuItem color = new ColorMenuItem();
        color.colorProperty().addListener(new ChangeListener<Color>() {

            @Override public void changed(final ObservableValue<? extends Color> arg0, final Color arg1, final Color newValue) {
                final int red = (int) (newValue.getRed() * 255);
                final int green = (int) (newValue.getGreen() * 255);
                final int blue = (int) (newValue.getBlue() * 255);
                configuration.setRulerColor(toHexByte(red) + toHexByte(green) + toHexByte(blue));
                configurationUpdated();
            }

            private String toHexByte(final int value) {
                return (value < 16) ? "0" + Integer.toString(value, 16) : Integer.toString(value, 16);
            }
        });
        ruler.getItems().addAll(showRuler, rulerSlider, color);

        displayOptionsMenu.getItems().addAll(showBoundsCheckbox, showBaselineCheckbox, new SeparatorMenuItem(), ruler, new SeparatorMenuItem(), showFilteredNodesInTree, showInvisibleNodes, showNodesIdInTree, collapseControls, collapseContentControls);

        final Menu aboutMenu = new Menu("Help");

        final MenuItem help = new MenuItem("Help Contents");
        help.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                HelpBox.make("Help Contents", HELP_URL, scenicViewStage);
            }
        });

        final MenuItem newVersion = new MenuItem("Check New Version");
        newVersion.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                checkNewVersion(true);
            }
        });

        final MenuItem about = new MenuItem("About");
        about.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                AboutBox.make("About", scenicViewStage);
            }
        });

        aboutMenu.getItems().addAll(help, newVersion, about);

        menuBar.getMenus().addAll(fileMenu, displayOptionsMenu, scenegraphMenu, aboutMenu);

        borderPane.setTop(menuBar);

        splitPane = new SplitPane();
        splitPane.setId("main-splitpane");

        allDetailsPane = new AllDetailsPane(new APILoader() {

            @Override public void loadAPI(final String property) {
                ScenicView.this.loadAPI(property);
            }
        }) {
            Menu menu;

            @Override public Menu getMenu() {
                if (menu == null) {
                    menu = super.getMenu();
                    final CheckMenuItem showCSSProperties = buildCheckMenuItem("Show CSS Properties", "Show CSS properties", "Hide CSS properties", "showCSSProperties", Boolean.FALSE);
                    showCSSProperties.selectedProperty().addListener(new InvalidationListener() {
                        @Override public void invalidated(final Observable arg0) {
                            configuration.setCSSPropertiesDetail(showCSSProperties.isSelected());
                            configurationUpdated();
                        }
                    });
                    final CheckMenuItem showDefaultProperties = buildCheckMenuItem("Show Default Properties", "Show default properties", "Hide default properties", "showDefaultProperties", Boolean.TRUE);
                    showDefaultProperties.selectedProperty().addListener(new InvalidationListener() {
                        @Override public void invalidated(final Observable arg0) {
                            allDetailsPane.setShowDefaultProperties(showDefaultProperties.isSelected());
                        }
                    });
                    configuration.setCSSPropertiesDetail(showCSSProperties.isSelected());
                    menu.getItems().addAll(showDefaultProperties, showCSSProperties);
                }
                return menu;
            }
        };

        treeView = new ScenegraphTreeView(activeNodeFilters, this);

        leftPane = new VBox();
        leftPane.setId("main-nodeStructure");

        final FilterTextField idFilterField = createFilterField("Node ID");
        idFilterField.setOnButtonClick(new Runnable() {
            @Override public void run() {
                idFilterField.setText("");
                update();
            }
        });
        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return true;
            }

            @Override public boolean accept(final SVNode node) {
                if (idFilterField.getText().equals(""))
                    return true;
                return node.getId() != null && node.getId().toLowerCase().indexOf(idFilterField.getText().toLowerCase()) != -1;
            }

            @Override public boolean ignoreShowFilteredNodesInTree() {
                return false;
            }

            @Override public boolean expandAllNodes() {
                return !idFilterField.getText().equals("");
            }
        });

        final FilterTextField classNameFilterField = createFilterField("Node className");
        classNameFilterField.setOnButtonClick(new Runnable() {
            @Override public void run() {
                classNameFilterField.setText("");
                update();
            }
        });
        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return true;
            }

            @Override public boolean accept(final SVNode node) {
                if (classNameFilterField.getText().equals(""))
                    return true;

                // Allow reduces or complete className
                return node.getNodeClass().toLowerCase().indexOf(classNameFilterField.getText().toLowerCase()) != -1;
            }

            @Override public boolean ignoreShowFilteredNodesInTree() {
                return false;
            }

            @Override public boolean expandAllNodes() {
                return !classNameFilterField.getText().equals("");
            }
        });

        final GridPane filtersGridPane = new GridPane();
        filtersGridPane.setVgap(5);
        filtersGridPane.setHgap(5);
        filtersGridPane.setSnapToPixel(true);
        filtersGridPane.setPadding(new Insets(0, 5, 5, 0));
        filtersGridPane.setId("main-filters-grid-pane");

        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(classNameFilterField, Priority.ALWAYS);
        GridPane.setHgrow(propertyFilterField, Priority.ALWAYS);

        filtersGridPane.add(new Label("ID Filter:"), 1, 1);
        filtersGridPane.add(idFilterField, 2, 1);
        filtersGridPane.add(new Label("Class Filter:"), 1, 2);
        filtersGridPane.add(classNameFilterField, 2, 2);
        filtersGridPane.add(new Label("Property Filter:"), 1, 3);
        filtersGridPane.add(propertyFilterField, 2, 3);

        final TitledPane filtersPane = new TitledPane("Filters", filtersGridPane);
        filtersPane.setId("main-filters");
        filtersPane.setMinHeight(filtersGridPane.getPrefHeight());

        treeView.setMaxHeight(Double.MAX_VALUE);
        final TitledPane treeViewPane = new TitledPane("JavaFX Apps", treeView);
        treeViewPane.setCollapsible(false);
        treeViewPane.setMaxHeight(Double.MAX_VALUE);
        // This solves the resizing of filtersPane
        treeViewPane.setPrefHeight(50);
        leftPane.getChildren().addAll(filtersPane, treeViewPane);
        VBox.setVgrow(treeViewPane, Priority.ALWAYS);

        animationsPane = new AnimationsPane(this) {

            Menu menu;

            @Override public Menu getMenu() {
                if (menu == null) {
                    menu = new Menu("Animations");
                    final CheckMenuItem animationsEnabled = buildCheckMenuItem("Animations enabled", "Animations will run on the application", "Animations will be stopped", null, Boolean.TRUE);
                    animationsEnabled.selectedProperty().addListener(new ChangeListener<Boolean>() {

                        @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                            animationsEnabled(animationsEnabled.isSelected());
                        }
                    });
                    menu.getItems().add(animationsEnabled);
                }
                return menu;
            }

        };

        tabPane = new TabPane();
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

            @Override public void changed(final ObservableValue<? extends Tab> arg0, final Tab oldValue, final Tab newValue) {
                if (oldValue != null && oldValue.getContent() instanceof ContextMenuContainer) {
                    menuBar.getMenus().remove(((ContextMenuContainer) oldValue.getContent()).getMenu());
                }
                if (newValue != null && newValue.getContent() instanceof ContextMenuContainer) {
                    menuBar.getMenus().add(menuBar.getMenus().size() - 1, ((ContextMenuContainer) newValue.getContent()).getMenu());
                }
            }
        });
        detailsTab = new Tab("Details");
        detailsTab.setGraphic(new ImageView(DisplayUtils.getUIImage("details.png")));
        detailsTab.setContent(allDetailsPane);
        detailsTab.setClosable(false);
        javadocTab = new Tab("JavaDoc");

        wview = new ProgressWebView();
        wview.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        javadocTab.setContent(wview);

        javadocTab.setGraphic(new ImageView(DisplayUtils.getUIImage("javadoc.png")));
        javadocTab.setClosable(false);
        javadocTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue) {
                    DisplayUtils.showWebView(true);
                    loadAPI(null);
                } else {
                    DisplayUtils.showWebView(false);
                }
            }
        });
        eventsTab = new Tab("Events");
        eventsTab.setContent(eventLogPane);
        eventsTab.setGraphic(new ImageView(DisplayUtils.getUIImage("flag_red.png")));
        eventsTab.setClosable(false);
        final Tab animationsTab = new Tab("Animations");
        animationsTab.setContent(animationsPane);
        animationsTab.setGraphic(new ImageView(DisplayUtils.getUIImage("cinema.png")));
        animationsTab.setClosable(false);
        animationsTab.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean arg2) {
                updateAnimations();
            }
        });
        tabPane.getTabs().addAll(detailsTab, eventsTab, animationsTab, javadocTab);
        Persistence.loadProperty("splitPaneDividerPosition", splitPane, 0.3);

        splitPane.getItems().addAll(leftPane, tabPane);

        borderPane.setCenter(splitPane);

        statusBar = new StatusBar();

        borderPane.setBottom(statusBar);

        getChildren().add(borderPane);

        this.scenicViewStage = senicViewStage;
        Persistence.loadProperty("stageWidth", senicViewStage, 800);
        Persistence.loadProperty("stageHeight", senicViewStage, 800);
        checkNewVersion(false);
        setUpdateStrategy(updateStrategy);
        TimelineBuilder.create().cycleCount(Animation.INDEFINITE).keyFrames(new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                dispatchEvents();
            }
        })).build().play();
    }

    protected void selectOnClick(final boolean newValue) {
        configuration.setComponentSelectOnClick(newValue);
        configurationUpdated();
    }

    private void checkNewVersion(final boolean forced) {
        final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
        final String value = Persistence.loadProperty("lastVersionCheck", null);
        try {
            if (forced || value == null || ((System.currentTimeMillis() - format.parse(value).getTime()) > 86400000)) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        final String newVersion = VersionChecker.checkVersion(VERSION);
                        String versionNum = null;
                        if (newVersion != null) {
                            // For now the version is on the first line
                            versionNum = newVersion;
                            if (newVersion.indexOf('\n') != -1) {
                                versionNum = newVersion.substring(0, newVersion.indexOf('\n'));
                            }
                            // Now check whether our version is newer
                            if (versionNum.compareTo(ScenicView.VERSION) < 0) {
                                versionNum = null;
                            }
                        }

                        if (versionNum != null) {
                            InfoBox.make("Version check", "New version found:" + versionNum + " (Yours is:" + ScenicView.VERSION + ")", newVersion, 400, 200);
                        } else if (forced) {
                            InfoBox.make("Version check", "ScenicView is updated", null, 200, 100);
                        }

                        Persistence.saveProperty("lastVersionCheck", format.format(new Date()));
                    }
                });

            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    protected void configurationUpdated() {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).configurationUpdated(configuration);
                }
            }
        }
    }

    private void animationsEnabled(final boolean enabled) {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).animationsEnabled(enabled);
                }
            }
        }
    }

    private void updateAnimations() {
        animationsPane.clear();
        for (int i = 0; i < apps.size(); i++) {
            /**
             * Only first stage
             */
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).updateAnimations();
                    break;
                }
            }
        }
    }

    void pauseAnimation(final StageID id, final int animationID) {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).getID().equals(id)) {
                    stages.get(j).pauseAnimation(animationID);
                }
            }
        }
        updateAnimations();
    }

    private void loadAPI(final String property) {
        if (tabPane.getTabs().contains(javadocTab)) {
            if (property != null) {
                goTo(SVTab.JAVADOC);
            }
            if (javadocTab.isSelected()) {
                if (selectedNode == null || selectedNode.getNodeClassName() == null || !selectedNode.getNodeClassName().startsWith("javafx.")) {
                    wview.doLoad("http://docs.oracle.com/javafx/2/api/overview-summary.html");
                } else {
                    String baseClass = selectedNode.getNodeClassName();
                    if (property != null) {
                        baseClass = findProperty(baseClass, property);
                    }
                    final String page = "http://docs.oracle.com/javafx/2/api/" + baseClass.replace('.', '/') + ".html" + (property != null ? ("#" + property + "Property") : "");
                    wview.doLoad(page);
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) private String findProperty(final String className, final String property) {
        Class node = null;
        try {
            node = Class.forName(className);
            node.getDeclaredMethod(property + "Property");

            return className;
        } catch (final Exception e) {
            return findProperty(node.getSuperclass().getName(), property);
        }
    }

    void update() {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).update();
                }
            }
        }
    }

    StageController getStageController(final StageID id) {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).getID().equals(id)) {
                    return stages.get(j);
                }
            }
        }
        return apps.get(0).getStages().get(0);
        // return null;
    }

    protected void filterProperties(final String text) {
        allDetailsPane.filterProperties(text);
    }

    @Override public void setSelectedNode(final StageController controller, final SVNode value) {
        if (value != selectedNode) {
            if (controller != null && activeStage != controller) {
                /**
                 * Remove selected from previous active
                 */
                activeStage.setSelectedNode(null);
                activeStage = controller;
            }
            storeSelectedNode(value);
            eventLogPane.setSelectedNode(value);
            loadAPI(null);
            propertyFilterField.setText("");
            propertyFilterField.setDisable(value == null);
            filterProperties(propertyFilterField.getText());
        }
    }

    @Override public SVNode getSelectedNode() {
        return selectedNode;
    }

    private void storeSelectedNode(final SVNode value) {
        selectedNode = value;
        if (selectedNode != null && detailsTab.isSelected())
            setStatusText("Click on the labels to modify its values. The panel could have different capabilities. When changed the values will be highlighted", 8000);
        activeStage.setSelectedNode(value);
    }

    private CheckMenuItem buildCheckMenuItem(final String text, final String toolTipSelected, final String toolTipNotSelected, final String property, final Boolean value) {
        final CheckMenuItem menuItem = new CheckMenuItem(text);
        if (property != null) {
            Persistence.loadProperty(property, menuItem, value);
        } else if (value != null) {
            menuItem.setSelected(value);
        }
        menuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                setStatusText(newValue ? toolTipSelected : toolTipNotSelected, 4000);
            }
        });

        return menuItem;
    }

    private FilterTextField createFilterField(final String prompt) {
        return createFilterField(prompt, new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                update();
            }
        });
    }

    private FilterTextField createFilterField(final String prompt, final EventHandler<KeyEvent> keyHandler) {
        final FilterTextField filterField = new FilterTextField();
        filterField.setPromptText(prompt);
        if (keyHandler != null) {
            filterField.setOnKeyReleased(keyHandler);
        }
        filterField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue) {
                    setStatusText("Type any text for filtering");
                } else {
                    clearStatusText();
                }
            }
        });
        return filterField;
    }

    private void closeApps() {
        for (final Iterator<AppController> iterator = apps.iterator(); iterator.hasNext();) {
            final AppController stage = iterator.next();
            stage.close();
        }
    }

    public void close() {
        closeApps();
        saveProperties();
        updateStrategy.finish();
    }

    private void saveProperties() {
        Persistence.saveProperties();
    }

    public static void setStatusText(final String text) {
        statusBar.setStatusText(text);
    }

    public static void setStatusText(final String text, final long timeout) {
        statusBar.setStatusText(text, timeout);
    }

    public static void clearStatusText() {
        statusBar.clearStatusText();
    }

    public static boolean hasStatusText() {
        return statusBar.hasStatus();
    }

    @Override protected double computePrefWidth(final double height) {
        return 600;
    }

    @Override protected double computePrefHeight(final double width) {
        return 600;
    }

    @Override protected void layoutChildren() {
        layoutInArea(borderPane, getPadding().getLeft(), getPadding().getTop(), getWidth() - getPadding().getLeft() - getPadding().getRight(), getHeight() - getPadding().getTop() - getPadding().getBottom(), 0, HPos.LEFT, VPos.TOP);
    }

    public final BorderPane getBorderPane() {
        return borderPane;
    }

    public final VBox getLeftPane() {
        return leftPane;
    }

    /**
     * For autoTesting purposes
     */
    @Override public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    protected static List<AppController> buildAppController(final Parent target) {
        final List<AppController> controllers = new ArrayList<AppController>();
        if (target != null) {
            final AppController aController = new AppControllerImpl();
            final StageControllerImpl sController = new StageControllerImpl(target, aController, false);

            aController.getStages().add(sController);
            controllers.add(aController);
        }
        return controllers;
    }

    public static void show(final Scene target) {
        show(target.getRoot());
    }

    public static void show(final Parent target) {
        final Stage stage = new Stage();
        // workaround for RT-10714
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View v" + VERSION);
        final DummyUpdateStrategy updateStrategy = new DummyUpdateStrategy(buildAppController(target));
        show(new ScenicView(updateStrategy, stage), stage);
    }

    public static void show(final ScenicView scenicview, final Stage stage) {
        final Scene scene = new Scene(scenicview);
        scene.getStylesheets().addAll(STYLESHEETS);
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        if (scenicview.activeStage != null && scenicview.activeStage instanceof StageControllerImpl)
            ((StageControllerImpl) scenicview.activeStage).placeStage(stage);

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            @Override public void handle(final WindowEvent arg0) {
                Runtime.getRuntime().removeShutdownHook(scenicview.shutdownHook);
                scenicview.close();
            }
        });
        stage.show();
    }

    public void setUpdateStrategy(final UpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
        this.updateStrategy.start(new AppsRepository() {

            private void dumpStatus(final String operation, final int id) {
                debug(operation + ":" + id);
                for (int i = 0; i < apps.size(); i++) {
                    debug("App:" + apps.get(i).getID());
                    final List<StageController> scs = apps.get(i).getStages();
                    for (int j = 0; j < scs.size(); j++) {
                        debug("\tStage:" + scs.get(j).getID().getStageID());
                    }
                }
            }

            int findAppControllerIndex(final int appID) {
                for (int i = 0; i < apps.size(); i++) {
                    if (apps.get(i).getID() == appID) {
                        return i;
                    }
                }
                return -1;
            }

            int findStageIndex(final List<StageController> stages, final int stageID) {
                for (int i = 0; i < stages.size(); i++) {
                    if (stages.get(i).getID().getStageID() == stageID) {
                        return i;
                    }
                }
                return -1;
            }

            @Override public void stageRemoved(final StageController stageController) {
                Platform.runLater(new Runnable() {

                    @Override public void run() {
                        dumpStatus("stageRemovedStart", stageController.getID().getStageID());
                        final List<StageController> stages = apps.get(findAppControllerIndex(stageController.getID().getAppID())).getStages();
                        // Remove and close
                        stages.remove(findStageIndex(stages, stageController.getID().getStageID())).close();
                        treeView.clearStage(stageController);
                        dumpStatus("stageRemovedStop", stageController.getID().getStageID());
                    }
                });
            }

            @Override public void stageAdded(final StageController stageController) {
                Platform.runLater(new Runnable() {

                    @Override public void run() {
                        dumpStatus("stageAddedStart", stageController.getID().getStageID());
                        apps.get(findAppControllerIndex(stageController.getID().getAppID())).getStages().add(stageController);
                        stageController.setEventDispatcher(stageModelListener);
                        configurationUpdated();
                        dumpStatus("stageAddedStop", stageController.getID().getStageID());
                    }
                });

            }

            @Override public void appRemoved(final AppController appController) {
                Platform.runLater(new Runnable() {

                    @Override public void run() {
                        dumpStatus("appRemovedStart", appController.getID());
                        // Remove and close
                        apps.remove(findAppControllerIndex(appController.getID())).close();
                        treeView.clearApp(appController);
                        dumpStatus("appRemovedStop", appController.getID());
                    }
                });

            }

            @Override public void appAdded(final AppController appController) {
                Platform.runLater(new Runnable() {

                    @Override public void run() {
                        dumpStatus("appAddedStart", appController.getID());
                        if (!apps.contains(appController)) {
                            if (apps.isEmpty() && !appController.getStages().isEmpty()) {
                                activeStage = appController.getStages().get(0);
                            }
                            apps.add(appController);
                        }
                        final List<StageController> stages = appController.getStages();
                        for (int j = 0; j < stages.size(); j++) {
                            stages.get(j).setEventDispatcher(stageModelListener);
                        }
                        configurationUpdated();
                        dumpStatus("appAddedStop", appController.getID());
                    }
                });

            }
        });
    }

    @Override public void openStage(final StageController controller) {
        controller.setEventDispatcher(stageModelListener);
        controller.configurationUpdated(configuration);
    }

    public static void setDebug(final boolean debug) {
        ScenicView.debug = debug;
    }

    public static void debug(final String debug) {
        if (ScenicView.debug) {
            System.out.println(debug);
        }
    }

    @Override public void forceUpdate() {
        update();
    }

    @Override public void goTo(final SVTab tab) {
        switch (tab) {
        case JAVADOC:
            tabPane.getSelectionModel().select(javadocTab);
            break;

        case DETAILS:
            tabPane.getSelectionModel().select(detailsTab);
            break;

        case EVENTS:
            tabPane.getSelectionModel().select(eventsTab);
            break;

        default:
            break;
        }
    }

    private void dispatchEvents() {
        // No need to synchronize
        while (!eventQueue.isEmpty()) {
            try {
                doDispatchEvent(eventQueue.remove(0));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doDispatchEvent(final FXConnectorEvent appEvent) {
        switch (appEvent.getType()) {
        case EVENT_LOG:
            eventLogPane.trace((EvLogEvent) appEvent);
            break;
        case MOUSE_POSITION:
            if (isActive(appEvent.getStageID()))
                statusBar.updateMousePosition(((MousePosEvent) appEvent).getPosition());
            break;

        case SHORTCUT:
            final KeyCode c = ((ShortcutEvent) appEvent).getCode();
            switch (c) {
            case S:
                selectOnClick(!configuration.isComponentSelectOnClick());
                break;
            case R:
                configuration.setShowRuler(!configuration.isShowRuler());
                configurationUpdated();
                break;
            default:
                break;
            }
            break;

        case WINDOW_DETAILS:
            final WindowDetailsEvent wevent = (WindowDetailsEvent) appEvent;
            autoRefreshStyleSheets.setDisable(!wevent.isStylesRefreshable());

            if (isActive(wevent.getStageID()))
                statusBar.updateWindowDetails(wevent.getWindowType(), wevent.getBounds(), wevent.isFocused());
            break;
        case NODE_SELECTED:
            componentSelectOnClick.setSelected(false);
            treeView.nodeSelected(((NodeSelectedEvent) appEvent).getNode());
            scenicViewStage.toFront();
            break;

        case NODE_COUNT:
            statusBar.updateNodeCount(((NodeCountEvent) appEvent).getNodeCount());
            break;

        case SCENE_DETAILS:
            if (isActive(appEvent.getStageID())) {
                final SceneDetailsEvent sEvent = (SceneDetailsEvent) appEvent;
                statusBar.updateSceneDetails(sEvent.getSize(), sEvent.getNodeCount());
            }
            break;

        case ROOT_UPDATED:
            treeView.updateStageModel(getStageController(appEvent.getStageID()), ((NodeAddRemoveEvent) appEvent).getNode(), showNodesIdInTree.isSelected(), showFilteredNodesInTree.isSelected());
            break;

        case NODE_ADDED:
            /**
             * First check if a we have a NODE_REMOVED in the queue
             */
            final int removedPos = indexOfNode(((NodeAddRemoveEvent) appEvent).getNode(), true);
            if (removedPos == -1) {
                treeView.addNewNode(((NodeAddRemoveEvent) appEvent).getNode(), showNodesIdInTree.isSelected(), showFilteredNodesInTree.isSelected());
            } else {
                eventQueue.remove(removedPos);
            }
            break;

        case NODE_REMOVED:
            final int addedPos = indexOfNode(((NodeAddRemoveEvent) appEvent).getNode(), false);
            if (addedPos == -1) {
                treeView.removeNode(((NodeAddRemoveEvent) appEvent).getNode());
            } else {
                eventQueue.remove(addedPos);
            }

            break;

        case DETAILS:
            final DetailsEvent ev = (DetailsEvent) appEvent;
            allDetailsPane.updateDetails(ev.getPaneType(), ev.getPaneName(), ev.getDetails(), new RemotePropertySetter() {

                @Override public void set(final Detail detail, final String value) {
                    getStageController(appEvent.getStageID()).setDetail(detail.getDetailType(), detail.getDetailID(), value);
                }
            });
            break;

        case DETAIL_UPDATED:
            final DetailsEvent ev2 = (DetailsEvent) appEvent;
            allDetailsPane.updateDetail(ev2.getPaneType(), ev2.getPaneName(), ev2.getDetails().get(0));
            break;

        case ANIMATIONS_UPDATED:
            animationsPane.update(appEvent.getStageID(), ((AnimationsCountEvent) appEvent).getAnimations());
            break;

        default:
            debug("Unused event for type " + appEvent);
            break;
        }
    }

    private int indexOfNode(final SVNode node, final boolean add) {
        for (int i = 0; i < eventQueue.size(); i++) {
            final FXConnectorEvent ev = eventQueue.get(i);
            if ((add && ev.getType() == SVEventType.NODE_REMOVED) || (!add && ev.getType() == SVEventType.NODE_ADDED)) {
                final NodeAddRemoveEvent ev2 = (NodeAddRemoveEvent) ev;
                if (ev2.getNode().equals(node)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isActive(final StageID stageID) {
        return activeStage.getID().equals(stageID);
    }

}
