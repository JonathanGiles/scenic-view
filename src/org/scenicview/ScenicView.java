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
package org.scenicview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.fxconnector.AppController;
import org.fxconnector.Configuration;
import org.fxconnector.ConnectorUtils;
import org.fxconnector.StageController;
import org.fxconnector.StageControllerImpl;
import org.fxconnector.StageID;
import org.fxconnector.details.Detail;
import org.fxconnector.event.AnimationsCountEvent;
import org.fxconnector.event.DetailsEvent;
import org.fxconnector.event.EvLogEvent;
import org.fxconnector.event.FXConnectorEvent;
import org.fxconnector.event.FXConnectorEvent.SVEventType;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.event.MousePosEvent;
import org.fxconnector.event.NodeAddRemoveEvent;
import org.fxconnector.event.NodeCountEvent;
import org.fxconnector.event.NodeSelectedEvent;
import org.fxconnector.event.SceneDetailsEvent;
import org.fxconnector.event.ShortcutEvent;
import org.fxconnector.event.WindowDetailsEvent;
import org.fxconnector.node.SVNode;
import org.scenicview.control.FilterTextField;
import org.scenicview.control.RulerConfigurationMenuItem;
import org.scenicview.dialog.AboutBox;
import org.scenicview.dialog.HelpBox;
import org.scenicview.tabs.AnimationsTab;
import org.scenicview.tabs.DetailsTab;
import org.scenicview.tabs.EventLogTab;
import org.scenicview.tabs.JavaDocTab;
import org.scenicview.tabs.details.APILoader;
import org.scenicview.tabs.details.GDetailPane.RemotePropertySetter;
import org.scenicview.update.AppsRepository;
import org.scenicview.update.UpdateStrategy;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.ScenicViewDebug;

/**
 * The base UI
 */
public class ScenicView {
    
    private static final String HELP_URL = "http://fxexperience.com/scenic-view/help";
    public static final String STYLESHEETS = ScenicView.class.getResource("scenicview.css").toExternalForm();
    public static final Image APP_ICON = DisplayUtils.getUIImage("mglass.png");

    public static final String VERSION = "8.0.0";
    
    private final Thread shutdownHook = new Thread() {
        @Override public void run() {
            // We can't use close() because we are not in FXThread
            saveProperties();
        }
    };

    // Scenic View UI
    private Stage scenicViewStage;
    private BorderPane rootBorderPane;
    private SplitPane splitPane;
    private ScenegraphTreeView treeView;
    private VBox leftPane;
    private StatusBar statusBar;

    FilterTextField propertyFilterField;

    /**
     * Menu Options
     */
    private MenuBar menuBar;
    private CheckMenuItem showFilteredNodesInTree;
    private CheckMenuItem showNodesIdInTree;
    private CheckMenuItem autoRefreshStyleSheets;
    private CheckMenuItem componentSelectOnClick;

    public final Configuration configuration = new Configuration();
    private final List<FXConnectorEvent> eventQueue = new LinkedList<FXConnectorEvent>();

    private UpdateStrategy updateStrategy;
    private long lastMousePosition;

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
                        break;
                }

            } else {
                ScenicViewDebug.print("Unused event " + appEvent);
            }
        }

        private boolean isValid(final FXConnectorEvent appEvent) {
            for (int i = 0; i < appRepository.getApps().size(); i++) {
                if (appRepository.getApps().get(i).getID() == appEvent.getStageID().getAppID()) {
                    final List<StageController> stages = appRepository.getApps().get(i).getStages();
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

//    private final List<AppController> apps = new ArrayList<AppController>();
    private final AppsRepository appRepository; 
    
    public StageController activeStage;
    private SVNode selectedNode;
    
    private TabPane tabPane;
    private DetailsTab detailsTab;
    private EventLogTab eventsTab;
    private AnimationsTab animationsTab;
    private JavaDocTab javadocTab;

    public ScenicView(final UpdateStrategy updateStrategy, final Stage scenicViewStage) {
        Persistence.loadProperties();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        buildUI();
//        checkNewVersion(false);
        
        this.appRepository = new AppsRepository(this);
        this.updateStrategy = updateStrategy;
        this.updateStrategy.start(appRepository);
        
        // we update Scenic View on a separate thread, based on events coming
        // in from FX Connector. The events arrive into the eventQueue, and
        // are processed here
        TimelineBuilder.create().cycleCount(Animation.INDEFINITE).keyFrames(new KeyFrame(Duration.millis(64), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
             // No need to synchronize
                while (!eventQueue.isEmpty()) {
                    try {
                        doDispatchEvent(eventQueue.remove(0));
                    } catch (final Exception e) {
                        ExceptionLogger.submitException(e);
                    }
                }
            }
        })).build().play();
    }
    
    private void buildUI() {
//        setId(StageController.FX_CONNECTOR_BASE_ID + "scenic-view");
        
        rootBorderPane = new BorderPane();
        rootBorderPane.setId("main-borderpane");

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

        menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        // menuBar.setId("main-menubar");

        // ---- File Menu
//        final MenuItem classpathItem = new MenuItem("Configure Classpath");
//        classpathItem.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(final ActionEvent arg0) {
//                final Properties properties = PropertiesUtils.getProperties();
//
//                final String toolsPath = properties.getProperty(ScenicViewBooter.JDK_PATH_KEY);
//                final File jdkPathFile = new ClassPathDialog(toolsPath).show(scenicViewStage);
//                
//                if (jdkPathFile != null) {
//                    properties.setProperty(ScenicViewBooter.JDK_PATH_KEY, jdkPathFile.getAbsolutePath());
//                    PropertiesUtils.saveProperties();
//                }
//            }
//        });

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
//        if (updateStrategy.needsClassPathConfiguration()) {
//            fileMenu.getItems().addAll(classpathItem, new SeparatorMenuItem());
//        }
        fileMenu.getItems().add(exitItem);

        // ---- Options Menu
        final CheckMenuItem showBoundsCheckbox = buildCheckMenuItem("Show Bounds Overlays", "Show the bound overlays on selected",
                "Do not show bound overlays on selected", "showBounds", Boolean.TRUE);
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
        final CheckMenuItem collapseControls = buildCheckMenuItem("Collapse controls In Tree", "Controls will be collapsed", "Controls will be expanded",
                "collapseControls", Boolean.TRUE);
        collapseControls.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setCollapseControls(newValue);
                configurationUpdated();
            }
        });
        configuration.setCollapseControls(collapseControls.isSelected());

        final CheckMenuItem collapseContentControls = buildCheckMenuItem("Collapse container controls In Tree", "Container controls will be collapsed",
                "Container controls will be expanded", "collapseContainerControls", Boolean.FALSE);
        collapseContentControls.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setCollapseContentControls(newValue);
                configurationUpdated();
            }
        });
        collapseContentControls.disableProperty().bind(collapseControls.selectedProperty().not());
        configuration.setCollapseContentControls(collapseContentControls.isSelected());

        final CheckMenuItem showBaselineCheckbox = buildCheckMenuItem("Show Baseline Overlay", "Display a red line at the current node's baseline offset",
                "Do not show baseline overlay", "showBaseline", Boolean.FALSE);
        showBaselineCheckbox.setId("show-baseline-overlay");
        configuration.setShowBaseline(showBaselineCheckbox.isSelected());
        showBaselineCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setShowBaseline(newValue);
                configurationUpdated();
            }
        });

        final CheckMenuItem automaticScenegraphStructureRefreshing = buildCheckMenuItem("Auto-Refresh Scenegraph",
                "Scenegraph structure will be automatically updated on change", "Scenegraph structure will NOT be automatically updated on change",
                "automaticScenegraphStructureRefreshing", Boolean.TRUE);
        automaticScenegraphStructureRefreshing.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setAutoRefreshSceneGraph(automaticScenegraphStructureRefreshing.isSelected());
                configurationUpdated();

            }
        });
        configuration.setAutoRefreshSceneGraph(automaticScenegraphStructureRefreshing.isSelected());

        final CheckMenuItem showInvisibleNodes = buildCheckMenuItem("Show Invisible Nodes In Tree", "Invisible nodes will be faded in the scenegraph tree",
                "Invisible nodes will not be shown in the scenegraph tree", "showInvisibleNodes", Boolean.FALSE);
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

        showNodesIdInTree = buildCheckMenuItem("Show Node IDs", "Node IDs will be shown on the scenegraph tree",
                "Node IDs will not be shown the Scenegraph tree", "showNodesIdInTree", Boolean.FALSE);
        showNodesIdInTree.selectedProperty().addListener(menuTreeChecksListener);

        showFilteredNodesInTree = buildCheckMenuItem("Show Filtered Nodes In Tree", "Filtered nodes will be faded in the tree",
                "Filtered nodes will not be shown in tree (unless they are parents of non-filtered nodes)", "showFilteredNodesInTree", Boolean.TRUE);

        showFilteredNodesInTree.selectedProperty().addListener(visilityListener);
        configuration.setVisibilityFilteringActive(!showInvisibleNodes.isSelected() && !showFilteredNodesInTree.isSelected());

        /**
         * Filter invisible nodes only makes sense if showFilteredNodesInTree is not selected
         */
        showInvisibleNodes.disableProperty().bind(showFilteredNodesInTree.selectedProperty());

        componentSelectOnClick = buildCheckMenuItem("Component highlight/select on click", "Click on the scene to select a component", "", null, null);
        componentSelectOnClick.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                selectOnClick(newValue);
            }
        });

        final CheckMenuItem ignoreMouseTransparentNodes = buildCheckMenuItem("Ignore MouseTransparent Nodes", "Transparent nodes will not be selectable",
                "Transparent nodes can be selected", "ignoreMouseTransparentNodes", Boolean.TRUE);
        ignoreMouseTransparentNodes.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setIgnoreMouseTransparent(newValue);
                configurationUpdated();
            }
        });
        configuration.setIgnoreMouseTransparent(ignoreMouseTransparentNodes.isSelected());

        final CheckMenuItem registerShortcuts = buildCheckMenuItem("Register shortcuts", "SV Keyboard shortcuts will be registered on your app",
                "SV Keyboard shortcuts will be removed on your app", "registerShortcuts", Boolean.TRUE);
        registerShortcuts.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setRegisterShortcuts(newValue);
                configurationUpdated();
            }
        });
        configuration.setRegisterShortcuts(registerShortcuts.isSelected());

        autoRefreshStyleSheets = buildCheckMenuItem("Auto-Refresh StyleSheets",
                "A background thread will check modifications on the css files to reload them if needed", "StyleSheets autorefreshing disabled",
                "autoRefreshStyleSheets", Boolean.FALSE);
        autoRefreshStyleSheets.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setAutoRefreshStyles(newValue);
                configurationUpdated();
            }
        });
        configuration.setAutoRefreshStyles(autoRefreshStyleSheets.isSelected());

        final Menu scenegraphMenu = new Menu("Scenegraph");
        scenegraphMenu.getItems().addAll(automaticScenegraphStructureRefreshing, autoRefreshStyleSheets, registerShortcuts, new SeparatorMenuItem(),
                componentSelectOnClick, ignoreMouseTransparentNodes);

        final Menu displayOptionsMenu = new Menu("Display Options");

        final Menu ruler = new Menu("Ruler");
        final CheckMenuItem showRuler = buildCheckMenuItem("Show Ruler", "Show ruler in the scene for alignment purposes", "", null, null);
        showRuler.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                configuration.setShowRuler(newValue.booleanValue());
                configurationUpdated();
            }
        });

        final RulerConfigurationMenuItem rulerConfig = new RulerConfigurationMenuItem();
        rulerConfig.colorProperty().addListener(new ChangeListener<Color>() {

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
        rulerConfig.rulerSeparationProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                configuration.setRulerSeparation(newValue.intValue());
                configurationUpdated();
            }
        });
        configuration.setRulerSeparation(rulerConfig.rulerSeparationProperty().get());
        ruler.getItems().addAll(showRuler, rulerConfig);

        displayOptionsMenu.getItems().addAll(showBoundsCheckbox, showBaselineCheckbox, new SeparatorMenuItem(), ruler, new SeparatorMenuItem(),
                showFilteredNodesInTree, showInvisibleNodes, showNodesIdInTree, collapseControls, collapseContentControls);

        final Menu aboutMenu = new Menu("Help");

        final MenuItem help = new MenuItem("Help Contents");
        help.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                HelpBox.make("Help Contents", HELP_URL, scenicViewStage);
            }
        });

//        final MenuItem newVersion = new MenuItem("Check For New Version");
//        newVersion.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(final ActionEvent arg0) {
//                checkNewVersion(true);
//            }
//        });

        final MenuItem about = new MenuItem("About");
        about.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                AboutBox.make("About", scenicViewStage);
            }
        });

        aboutMenu.getItems().addAll(help/*, newVersion*/, about);

        menuBar.getMenus().addAll(fileMenu, displayOptionsMenu, scenegraphMenu, aboutMenu);

        rootBorderPane.setTop(menuBar);

        splitPane = new SplitPane();
        splitPane.setId("main-splitpane");

        detailsTab = new DetailsTab(this, new APILoader() {
            @Override public void loadAPI(final String property) {
                ScenicView.this.loadAPI(property);
            }
        });

        treeView = new ScenegraphTreeView(activeNodeFilters, this);

        leftPane = new VBox();
        leftPane.setId("main-nodeStructure");

        final FilterTextField idFilterField = createFilterField("Node ID");
        idFilterField.setOnButtonClick(() -> {
            idFilterField.setText("");
            update();
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
        classNameFilterField.setOnButtonClick(() -> {
            classNameFilterField.setText("");
            update();
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

        animationsTab = new AnimationsTab(this);

        tabPane = new TabPane();
        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> updateMenuBar(oldValue, newValue));

        javadocTab = new JavaDocTab(this); 
        
        eventsTab = new EventLogTab(this);
        eventsTab.activeProperty().addListener((ov, oldValue, newValue) -> {
            configuration.setEventLogEnabled(newValue);
            configurationUpdated();
        });
        
        tabPane.getTabs().addAll(detailsTab, eventsTab, /*animationsTab,*/ javadocTab);
        
        Persistence.loadProperty("splitPaneDividerPosition", splitPane, 0.3);

        splitPane.getItems().addAll(leftPane, tabPane);

        rootBorderPane.setCenter(splitPane);

        statusBar = new StatusBar();

        rootBorderPane.setBottom(statusBar);

        Persistence.loadProperty("stageWidth", scenicViewStage, 800);
        Persistence.loadProperty("stageHeight", scenicViewStage, 800);
    }
    
    private void updateMenuBar(final Tab oldValue, final Tab newValue) {
        if (oldValue != null && oldValue instanceof ContextMenuContainer) {
            Menu menu = ((ContextMenuContainer) oldValue).getMenu();
            menuBar.getMenus().remove(menu);
        }
        if (newValue != null && newValue instanceof ContextMenuContainer) {
            Menu newMenu = ((ContextMenuContainer) newValue).getMenu();
            if (newMenu != null) {
                menuBar.getMenus().add(menuBar.getMenus().size() - 1, newMenu);
            }
        }
    }

    protected void selectOnClick(final boolean newValue) {
        if (configuration.isComponentSelectOnClick() != newValue) {
            configuration.setComponentSelectOnClick(newValue);
            configurationUpdated();
        }
    }

//    private void checkNewVersion(final boolean forced) {
//        final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
//        final String value = Persistence.loadProperty("lastVersionCheck", null);
//        try {
//            if (forced || value == null || ((System.currentTimeMillis() - format.parse(value).getTime()) > 86400000)) {
//                Platform.runLater(new Runnable() {
//                    @Override public void run() {
//                        final String newVersion = VersionChecker.checkVersion(VERSION);
//                        String versionNum = null;
//                        if (newVersion != null) {
//                            // For now the version is on the first line
//                            versionNum = newVersion;
//                            if (newVersion.indexOf('\n') != -1) {
//                                versionNum = newVersion.substring(0, newVersion.indexOf('\n'));
//                            }
//                            // Now check whether our version is newer
//                            if (versionNum.compareTo(ScenicView.VERSION) < 0) {
//                                versionNum = null;
//                            }
//                        }
//
//                        if (versionNum != null) {
//                            new InfoBox("Version check", "New version found:" + versionNum + " (Yours is:" + ScenicView.VERSION + ")", newVersion, 400, 200);
//                        } else if (forced) {
//                            new InfoBox("Version check", "You already have the latest version of Scenic View.", null, 400, 150);
//                        }
//
//                        Persistence.saveProperty("lastVersionCheck", format.format(new Date()));
//                    }
//                });
//
//            }
//        } catch (final Exception e) {
//            ExceptionLogger.submitException(e);
//        }
//    }
    
    public void removeApp(final AppController appController) {
        treeView.removeApp(appController);
    }
    
    public void removeStage(final StageController stageController) {
        treeView.removeStage(stageController);
    }
    
    public void setActiveStage(final StageController activeStage) {
        this.activeStage = activeStage;
    }
    
    public FXConnectorEventDispatcher getStageModelListener() {
        return stageModelListener;
    }

    public void configurationUpdated() {
        for (int i = 0; i < appRepository.getApps().size(); i++) {
            final List<StageController> stages = appRepository.getApps().get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).configurationUpdated(configuration);
                }
            }
        }
    }

    public void animationsEnabled(final boolean enabled) {
        for (int i = 0; i < appRepository.getApps().size(); i++) {
            final List<StageController> stages = appRepository.getApps().get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).animationsEnabled(enabled);
                }
            }
        }
    }

    public void updateAnimations() {
        animationsTab.clear();
        for (int i = 0; i < appRepository.getApps().size(); i++) {
            /**
             * Only first stage
             */
            final List<StageController> stages = appRepository.getApps().get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).updateAnimations();
                    break;
                }
            }
        }
    }

    public void pauseAnimation(final StageID id, final int animationID) {
        for (int i = 0; i < appRepository.getApps().size(); i++) {
            final List<StageController> stages = appRepository.getApps().get(i).getStages();
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
                goToTab(JavaDocTab.TAB_NAME);
            }
            if (javadocTab.isSelected()) {
                javadocTab.loadAPI(property);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) 
    public String findProperty(final String className, final String property) {
        Class node = null;
        try {
            node = Class.forName(className);
            node.getDeclaredMethod(property + "Property");

            return className;
        } catch (final Exception e) {
            return findProperty(node.getSuperclass().getName(), property);
        }
    }

    public void update() {
        for (int i = 0; i < appRepository.getApps().size(); i++) {
            final List<StageController> stages = appRepository.getApps().get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).isOpened()) {
                    stages.get(j).update();
                }
            }
        }
    }

    StageController getStageController(final StageID id) {
        for (int i = 0; i < appRepository.getApps().size(); i++) {
            final List<StageController> stages = appRepository.getApps().get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                if (stages.get(j).getID().equals(id)) {
                    return stages.get(j);
                }
            }
        }
        return appRepository.getApps().get(0).getStages().get(0);
        // return null;
    }

    protected void filterProperties(final String text) {
        detailsTab.filterProperties(text);
    }

    public void setSelectedNode(final StageController controller, final SVNode value) {
        if (value != selectedNode) {
            if (controller != null && activeStage != controller) {
                /**
                 * Remove selected from previous active
                 */
                activeStage.setSelectedNode(null);
                activeStage = controller;
            }
            storeSelectedNode(value);
            eventsTab.setSelectedNode(value);
            loadAPI(null);
            propertyFilterField.setText("");
            propertyFilterField.setDisable(value == null);
            filterProperties(propertyFilterField.getText());
        }
    }

    public SVNode getSelectedNode() {
        return selectedNode;
    }
    
    public void removeNode() {
        activeStage.removeSelectedNode();
    }

    private void storeSelectedNode(final SVNode value) {
        selectedNode = value;
        if (selectedNode != null && detailsTab.isSelected())
            setStatusText("Click on the labels to modify its values. The panel could have different capabilities. When changed the values will be highlighted",
                    8000);
        activeStage.setSelectedNode(value);
    }

    public CheckMenuItem buildCheckMenuItem(final String text, final String toolTipSelected, final String toolTipNotSelected, final String property,
            final Boolean value) {
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
        for (final Iterator<AppController> iterator = appRepository.getApps().iterator(); iterator.hasNext();) {
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

    public void setStatusText(final String text) {
        statusBar.setStatusText(text);
    }

    public void setStatusText(final String text, final long timeout) {
        statusBar.setStatusText(text, timeout);
    }

    public void clearStatusText() {
        statusBar.clearStatusText();
    }

    public boolean hasStatusText() {
        return statusBar.hasStatus();
    }

//    @Override protected double computePrefWidth(final double height) {
//        return 600;
//    }
//
//    @Override protected double computePrefHeight(final double width) {
//        return 600;
//    }
//
//    @Override protected void layoutChildren() {
//        layoutInArea(borderPane, getPadding().getLeft(), getPadding().getTop(), getWidth() - getPadding().getLeft() - getPadding().getRight(), getHeight()
//                - getPadding().getTop() - getPadding().getBottom(), 0, HPos.LEFT, VPos.TOP);
//    }

    public final BorderPane getBorderPane() {
        return rootBorderPane;
    }

    public final VBox getLeftPane() {
        return leftPane;
    }

//    /**
//     * For autoTesting purposes
//     */
//    @Override public ObservableList<Node> getChildren() {
//        return super.getChildren();
//    }

    public static void show(final ScenicView scenicview, final Stage stage) {
        final Scene scene = new Scene(scenicview.rootBorderPane);
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

    public void openStage(final StageController controller) {
        controller.setEventDispatcher(stageModelListener);
        controller.configurationUpdated(configuration);
    }

    public void forceUpdate() {
        update();
    }
    
    public void goToTab(String tabName) {
        Tab switchToTab = null;
        for (Tab tab : tabPane.getTabs()) {
            if (tabName == tab.getText()) {
                switchToTab = tab;
                break;
            }
        }
        
        if (switchToTab != null) {
            tabPane.getSelectionModel().select(switchToTab);
        }
    }
    
    private void doDispatchEvent(final FXConnectorEvent appEvent) {
        switch (appEvent.getType()) {
            case EVENT_LOG: {
                eventsTab.trace((EvLogEvent) appEvent);
                break;
            }                
            case MOUSE_POSITION: {
                if (isActive(appEvent.getStageID()))
                    statusBar.updateMousePosition(((MousePosEvent) appEvent).getPosition());
                break;
            }
            case SHORTCUT: {
                final KeyCode c = ((ShortcutEvent) appEvent).getCode();
                switch (c) {
                    case S:
                        componentSelectOnClick.setSelected(!configuration.isComponentSelectOnClick());
                        break;
                    case R:
                        configuration.setShowRuler(!configuration.isShowRuler());
                        configurationUpdated();
                        break;
                    case D:
                        treeView.getSelectionModel().clearSelection();
                        break;
                    default:
                        break;
                }
                break;
            }
            case WINDOW_DETAILS: {
                final WindowDetailsEvent wevent = (WindowDetailsEvent) appEvent;
                autoRefreshStyleSheets.setDisable(!wevent.isStylesRefreshable());

                if (isActive(wevent.getStageID())) {
                    statusBar.updateWindowDetails(wevent.getWindowType(), wevent.getBounds(), wevent.isFocused());
                }
                break;
            }
            case NODE_SELECTED: {
                componentSelectOnClick.setSelected(false);
                treeView.nodeSelected(((NodeSelectedEvent) appEvent).getNode());
                scenicViewStage.toFront();
                break;
            }
            case NODE_COUNT: {
                statusBar.updateNodeCount(((NodeCountEvent) appEvent).getNodeCount());
                break;
            }
            case SCENE_DETAILS: {
                if (isActive(appEvent.getStageID())) {
                    final SceneDetailsEvent sEvent = (SceneDetailsEvent) appEvent;
                    statusBar.updateSceneDetails(sEvent.getSize(), sEvent.getNodeCount());
                }
                break;
            }
            case ROOT_UPDATED: {
                treeView.updateStageModel(getStageController(appEvent.getStageID()), 
                                         ((NodeAddRemoveEvent) appEvent).getNode(), 
                                         showNodesIdInTree.isSelected(),
                                         showFilteredNodesInTree.isSelected());
                break;
            }
            case NODE_ADDED: {
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
            }
            case NODE_REMOVED: {
                final int addedPos = indexOfNode(((NodeAddRemoveEvent) appEvent).getNode(), false);
                if (addedPos == -1) {
                    treeView.removeNode(((NodeAddRemoveEvent) appEvent).getNode());
                } else {
                    eventQueue.remove(addedPos);
                }

                break;
            }
            case DETAILS: {
                final DetailsEvent ev = (DetailsEvent) appEvent;
                detailsTab.updateDetails(ev.getPaneType(), ev.getPaneName(), ev.getDetails(), new RemotePropertySetter() {

                    @Override public void set(final Detail detail, final String value) {
                        getStageController(appEvent.getStageID()).setDetail(detail.getDetailType(), detail.getDetailID(), value);
                    }
                });
                break;
            }
            case DETAIL_UPDATED: {
                final DetailsEvent ev2 = (DetailsEvent) appEvent;
                detailsTab.updateDetail(ev2.getPaneType(), ev2.getPaneName(), ev2.getDetails().get(0));
                break;
            }
            case ANIMATIONS_UPDATED: {
                animationsTab.update(appEvent.getStageID(), ((AnimationsCountEvent) appEvent).getAnimations());
                break;
            }
            default: {
                ScenicViewDebug.print("Unused event for type " + appEvent);
                break;
            }
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
