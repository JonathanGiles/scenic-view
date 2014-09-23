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
package org.scenicview.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.fxconnector.AppController;
import org.fxconnector.Configuration;
import org.fxconnector.ConnectorUtils;
import org.fxconnector.StageController;
import org.fxconnector.StageControllerImpl;
import org.fxconnector.StageID;
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
import org.scenicview.view.control.FilterTextField;
import org.scenicview.view.dialog.AboutBox;
import org.scenicview.view.dialog.HelpBox;
import org.scenicview.view.tabs.AnimationsTab;
import org.scenicview.view.tabs.DetailsTab;
import org.scenicview.view.tabs.EventLogTab;
import org.scenicview.view.tabs.JavaDocTab;
import org.scenicview.model.Persistence;
import org.scenicview.model.update.AppsRepository;
import org.scenicview.model.update.UpdateStrategy;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.Logger;

/**
 * The base UI
 */
public class ScenicViewGui {
    
    private static final String HELP_URL = "http://fxexperience.com/scenic-view/help";
    public static final String STYLESHEETS = ScenicViewGui.class.getResource("scenicview.css").toExternalForm();
    public static final Image APP_ICON = DisplayUtils.getUIImage("mglass.png");

    public static final String VERSION = "8.0.0";
    
    private final Thread shutdownHook = new Thread() {
        @Override public void run() {
            // We can't use close() because we are not in FXThread
            saveProperties();
        }
    };

    // Scenic View UI
    private final Stage scenicViewStage;
    private BorderPane rootBorderPane;
    private SplitPane splitPane;
    
    // menu bar area
    private MenuBar menuBar;
    private CheckMenuItem showFilteredNodesInTree;
    private CheckMenuItem showNodesIdInTree;
    private CheckMenuItem autoRefreshStyleSheets;
    private CheckMenuItem componentSelectOnClick;
    private CheckMenuItem showInvisibleNodes;
    private CheckMenuItem showSearchBar;
    
    // filter area
//    private TitledPane filtersPane;
    private FilterTextField propertyFilterField;
    private List<NodeFilter> activeNodeFilters;
    
    // tree area
    private Node treeViewScanningPlaceholder;
    private ScenegraphTreeView treeView;
    
    // search bar area
    private GridPane searchBar;
    
    // status bar area
    private StatusBar statusBar;
    
    private VBox bottomVBox;

    

    public final Configuration configuration = new Configuration();
    private final List<FXConnectorEvent> eventQueue = new LinkedList<>();

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
                Logger.print("Unused event " + appEvent);
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

    public ScenicViewGui(final UpdateStrategy updateStrategy, final Stage scenicViewStage) {
        this.scenicViewStage = scenicViewStage;
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
        Timeline eventDispatcher = new Timeline(new KeyFrame(Duration.millis(60), event -> {
            // No need to synchronize
            while (!eventQueue.isEmpty()) {
                try {
                    doDispatchEvent(eventQueue.remove(0));
                } catch (final Exception e) {
                    ExceptionLogger.submitException(e);
                }
            }
        }));
        eventDispatcher.setCycleCount(Animation.INDEFINITE);
        eventDispatcher.play();
    }
    
    private void buildUI() {
        rootBorderPane = new BorderPane();
        rootBorderPane.setId(StageController.FX_CONNECTOR_BASE_ID + "scenic-view");
        
        // search bar
        buildFiltersBox();

        // menubar
        buildMenuBar();

        // main splitpane
        splitPane = new SplitPane();
        splitPane.setId("main-splitpane");

        
        // treeview
        treeView = new ScenegraphTreeView(activeNodeFilters, this);
        treeViewScanningPlaceholder = new VBox(10) {
            {
                ProgressIndicator progress = new ProgressIndicator();
                Label label = new Label("Scanning for JavaFX applications");
                label.getStyleClass().add("scanning-label");
                getChildren().addAll(progress, label);
                
                setAlignment(Pos.CENTER);
                
                treeView.expandedItemCountProperty().addListener(o -> {
                    setVisible(treeView.getExpandedItemCount() == 0);
                });
                
            }
        };
        
        StackPane treeViewStackPane = new StackPane(treeView, treeViewScanningPlaceholder);
        treeViewStackPane.setStyle(" -fx-padding: 0");

        treeView.setMaxHeight(Double.MAX_VALUE);
        
        // right side
        detailsTab = new DetailsTab(this, new Consumer<String>() {
            @Override public void accept(String property) {
                ScenicViewGui.this.loadAPI(property);
            }
        });

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

        // putting it all together
        splitPane.getItems().addAll(treeViewStackPane, tabPane);

        rootBorderPane.setCenter(splitPane);
        
        // status bar
        statusBar = new StatusBar();
        
        bottomVBox = new VBox(searchBar, statusBar);

        rootBorderPane.setBottom(bottomVBox);

        Persistence.loadProperty("stageWidth", scenicViewStage, 800);
        Persistence.loadProperty("stageHeight", scenicViewStage, 800);
    }
    
    private void buildFiltersBox() {
        propertyFilterField = createFilterField("Type property names or values here", null);
        propertyFilterField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                filterProperties(propertyFilterField.getText());
            }
        });
        propertyFilterField.setDisable(true);
        
        final FilterTextField idFilterField = createFilterField("Type Node ID's here");
        idFilterField.setOnButtonClick(() -> {
            idFilterField.setText("");
            update();
        });
        
        final FilterTextField classNameFilterField = createFilterField("Type class names here");
        classNameFilterField.setOnButtonClick(() -> {
            classNameFilterField.setText("");
            update();
        });
        
        searchBar = new GridPane();
        searchBar.setVgap(5);
        searchBar.setHgap(5);
        searchBar.setSnapToPixel(true);
        searchBar.setPadding(new Insets(0, 5, 5, 0));
        searchBar.getStyleClass().add("search-bar");

        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(classNameFilterField, Priority.ALWAYS);
        GridPane.setHgrow(propertyFilterField, Priority.ALWAYS);

        int column = 1;
        
        Label nodeIdFilterLabel = new Label("Node ID Filter:");
        Label classNameFilterLabel = new Label("Class Name Filter:");
        Label propertyFilterLabel = new Label("Property Filter:");
        
        searchBar.add(nodeIdFilterLabel, column++, 1);
        searchBar.add(idFilterField, column++, 1);
        searchBar.add(classNameFilterLabel, column++, 1);
        searchBar.add(classNameFilterField, column++, 1);
        searchBar.add(propertyFilterLabel, column++, 1);
        searchBar.add(propertyFilterField, column++, 1);

        // create filters for nodes
        activeNodeFilters = new ArrayList<>();

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
    }
    
    private void buildMenuBar() {
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
        exitItem.setOnAction(event -> {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            close();
            // TODO Why closing the Stage does not dispatch
            // WINDOW_CLOSE_REQUEST??
            scenicViewStage.close();
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
        showBoundsCheckbox.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setShowBounds(newValue);
            configurationUpdated();
        });

        final CheckMenuItem collapseControls = buildCheckMenuItem("Collapse controls In Tree", "Controls will be collapsed", "Controls will be expanded",
                "collapseControls", Boolean.TRUE);
        collapseControls.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setCollapseControls(newValue);
            configurationUpdated();
        });
        configuration.setCollapseControls(collapseControls.isSelected());

        final CheckMenuItem collapseContentControls = buildCheckMenuItem("Collapse container controls In Tree", "Container controls will be collapsed",
                "Container controls will be expanded", "collapseContainerControls", Boolean.FALSE);
        collapseContentControls.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setCollapseContentControls(newValue);
            configurationUpdated();
        });
        collapseContentControls.disableProperty().bind(collapseControls.selectedProperty().not());
        configuration.setCollapseContentControls(collapseContentControls.isSelected());

        final CheckMenuItem showBaselineCheckbox = buildCheckMenuItem("Show Baseline Overlay", "Display a red line at the current node's baseline offset",
                "Do not show baseline overlay", "showBaseline", Boolean.FALSE);
        showBaselineCheckbox.setId("show-baseline-overlay");
        configuration.setShowBaseline(showBaselineCheckbox.isSelected());
        showBaselineCheckbox.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setShowBaseline(newValue);
            configurationUpdated();
        });

        final CheckMenuItem automaticScenegraphStructureRefreshing = buildCheckMenuItem("Auto-Refresh Scenegraph",
                "Scenegraph structure will be automatically updated on change", "Scenegraph structure will NOT be automatically updated on change",
                "automaticScenegraphStructureRefreshing", Boolean.TRUE);
        automaticScenegraphStructureRefreshing.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setAutoRefreshSceneGraph(automaticScenegraphStructureRefreshing.isSelected());
            configurationUpdated();
        });
        configuration.setAutoRefreshSceneGraph(automaticScenegraphStructureRefreshing.isSelected());
        
        
        // --- show search bar
        showSearchBar = buildCheckMenuItem("Show Search Bar", "Shows a search bar to allow you to filter the displayed information",
                "Shows a search bar to allow you to filter the displayed information", "showSearchBar", Boolean.TRUE);
        searchBar.visibleProperty().bind(showSearchBar.selectedProperty());
        searchBar.managedProperty().bind(showSearchBar.selectedProperty());

        
        // --- show invisible nodes
        showInvisibleNodes = buildCheckMenuItem("Show Invisible Nodes In Tree", "Invisible nodes will be faded in the scenegraph tree",
                "Invisible nodes will not be shown in the scenegraph tree", "showInvisibleNodes", Boolean.FALSE);
        final ChangeListener<Boolean> visilityListener = (o, oldValue, newValue) -> {
            configuration.setVisibilityFilteringActive(!showInvisibleNodes.isSelected() && !showFilteredNodesInTree.isSelected());
            configurationUpdated();
        };
        showInvisibleNodes.selectedProperty().addListener(visilityListener);

        
        // --- show node IDs in tree
        showNodesIdInTree = buildCheckMenuItem("Show Node IDs", "Node IDs will be shown on the scenegraph tree",
                "Node IDs will not be shown the Scenegraph tree", "showNodesIdInTree", Boolean.FALSE);
        showNodesIdInTree.selectedProperty().addListener(o -> update());

        
        // --- show filtered nodes in tree
        showFilteredNodesInTree = buildCheckMenuItem("Show Filtered Nodes In Tree", "Filtered nodes will be faded in the tree",
                "Filtered nodes will not be shown in tree (unless they are parents of non-filtered nodes)", "showFilteredNodesInTree", Boolean.TRUE);

        showFilteredNodesInTree.selectedProperty().addListener(visilityListener);
        configuration.setVisibilityFilteringActive(!showInvisibleNodes.isSelected() && !showFilteredNodesInTree.isSelected());

        /**
         * Filter invisible nodes only makes sense if showFilteredNodesInTree is not selected
         */
        showInvisibleNodes.disableProperty().bind(showFilteredNodesInTree.selectedProperty());

        componentSelectOnClick = buildCheckMenuItem("Component highlight/select on click", "Click on the scene to select a component", "", null, null);
        componentSelectOnClick.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> selectOnClick(newValue));

        final CheckMenuItem ignoreMouseTransparentNodes = buildCheckMenuItem("Ignore MouseTransparent Nodes", "Transparent nodes will not be selectable",
                "Transparent nodes can be selected", "ignoreMouseTransparentNodes", Boolean.TRUE);
        ignoreMouseTransparentNodes.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setIgnoreMouseTransparent(newValue);
            configurationUpdated();
        });
        configuration.setIgnoreMouseTransparent(ignoreMouseTransparentNodes.isSelected());

        final CheckMenuItem registerShortcuts = buildCheckMenuItem("Register shortcuts", "SV Keyboard shortcuts will be registered on your app",
                "SV Keyboard shortcuts will be removed on your app", "registerShortcuts", Boolean.TRUE);
        registerShortcuts.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setRegisterShortcuts(newValue);
            configurationUpdated();
        });
        configuration.setRegisterShortcuts(registerShortcuts.isSelected());

        autoRefreshStyleSheets = buildCheckMenuItem("Auto-Refresh StyleSheets",
                "A background thread will check modifications on the css files to reload them if needed", "StyleSheets autorefreshing disabled",
                "autoRefreshStyleSheets", Boolean.FALSE);
        autoRefreshStyleSheets.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setAutoRefreshStyles(newValue);
            configurationUpdated();
        });
        configuration.setAutoRefreshStyles(autoRefreshStyleSheets.isSelected());

        final Menu scenegraphMenu = new Menu("Scenegraph");
        scenegraphMenu.getItems().addAll(automaticScenegraphStructureRefreshing, autoRefreshStyleSheets, registerShortcuts, new SeparatorMenuItem(),
                componentSelectOnClick, ignoreMouseTransparentNodes);

        final Menu displayOptionsMenu = new Menu("Display Options");

//        final Menu ruler = new Menu("Ruler");
        final CheckMenuItem showRuler = buildCheckMenuItem("Show Ruler", "Show ruler in the scene for alignment purposes", "", null, null);
        showRuler.selectedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            configuration.setShowRuler(newValue.booleanValue());
            configurationUpdated();
        });
//
//        final RulerConfigurationMenuItem rulerConfig = new RulerConfigurationMenuItem();
//        rulerConfig.colorProperty().addListener(new ChangeListener<Color>() {
//
//            @Override public void changed(final ObservableValue<? extends Color> arg0, final Color arg1, final Color newValue) {
//                final int red = (int) (newValue.getRed() * 255);
//                final int green = (int) (newValue.getGreen() * 255);
//                final int blue = (int) (newValue.getBlue() * 255);
//                configuration.setRulerColor(toHexByte(red) + toHexByte(green) + toHexByte(blue));
//                configurationUpdated();
//            }
//
//            private String toHexByte(final int value) {
//                return (value < 16) ? "0" + Integer.toString(value, 16) : Integer.toString(value, 16);
//            }
//        });
//        rulerConfig.rulerSeparationProperty().addListener((ChangeListener<Number>) (o, oldValue, newValue) -> {
//            configuration.setRulerSeparation(newValue.intValue());
//            configurationUpdated();
//        });
//        configuration.setRulerSeparation(rulerConfig.rulerSeparationProperty().get());
//        ruler.getItems().addAll(showRuler, rulerConfig);

        displayOptionsMenu.getItems().addAll(showBoundsCheckbox, 
                                             showBaselineCheckbox, 
                                             showRuler,
                                             showSearchBar, 
                                             showFilteredNodesInTree, 
                                             showInvisibleNodes, 
                                             showNodesIdInTree, 
                                             collapseControls, 
                                             collapseContentControls);

        final Menu aboutMenu = new Menu("Help");

        final MenuItem help = new MenuItem("Help Contents");
        help.setOnAction(event -> HelpBox.make("Help Contents", HELP_URL, scenicViewStage));

//        final MenuItem newVersion = new MenuItem("Check For New Version");
//        newVersion.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(final ActionEvent arg0) {
//                checkNewVersion(true);
//            }
//        });

        final MenuItem about = new MenuItem("About");
        about.setOnAction(event -> AboutBox.make("About", scenicViewStage));

        aboutMenu.getItems().addAll(help/*, newVersion*/, about);

        menuBar.getMenus().addAll(fileMenu, displayOptionsMenu, scenegraphMenu, aboutMenu);

        rootBorderPane.setTop(menuBar);
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
        return createFilterField(prompt, event -> update());
    }

    private FilterTextField createFilterField(final String prompt, final EventHandler<KeyEvent> keyHandler) {
        final FilterTextField filterField = new FilterTextField();
        filterField.setPromptText(prompt);
        if (keyHandler != null) {
            filterField.setOnKeyReleased(keyHandler);
        }
        filterField.focusedProperty().addListener((ChangeListener<Boolean>) (o, oldValue, newValue) -> {
            if (newValue) {
                setStatusText("Type any text for filtering");
            } else {
                clearStatusText();
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

//    /**
//     * For autoTesting purposes
//     */
//    @Override public ObservableList<Node> getChildren() {
//        return super.getChildren();
//    }

    public static void show(final ScenicViewGui scenicview, final Stage stage) {
        final Scene scene = new Scene(scenicview.rootBorderPane);
        scene.getStylesheets().addAll(STYLESHEETS);
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        if (scenicview.activeStage != null && scenicview.activeStage instanceof StageControllerImpl)
            ((StageControllerImpl) scenicview.activeStage).placeStage(stage);

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            Runtime.getRuntime().removeShutdownHook(scenicview.shutdownHook);
            scenicview.close();
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
                detailsTab.updateDetails(ev.getPaneType(), ev.getPaneName(), ev.getDetails(), (detail, value) -> {
                    getStageController(appEvent.getStageID()).setDetail(detail.getDetailType(), detail.getDetailID(), value);   
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
                Logger.print("Unused event for type " + appEvent);
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
