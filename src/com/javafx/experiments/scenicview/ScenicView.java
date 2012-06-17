/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import static com.javafx.experiments.scenicview.DisplayUtils.nodeClass;

import java.net.URL;
import java.util.*;

import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Callback;

import com.javafx.experiments.scenicview.details.AllDetailsPane;
import com.javafx.experiments.scenicview.dialog.*;

/**
 * 
 * @author aim
 */
public class ScenicView extends Region {

    private static final String HELP_URL = "http://fxexperience.com/scenic-view/help";
    public static final String STYLESHEETS = "com/javafx/experiments/scenicview/scenicview.css";
    static final String SCENIC_VIEW_BASE_ID = "ScenicView.";

    public static final Image APP_ICON = DisplayUtils.getUIImage("mglass.gif");
    static final Image FX_APP_ICON = DisplayUtils.getUIImage("fx.png");    
    private static final Image PANEL_NODE_IMAGE = new Image(DisplayUtils.getNodeIcon("Panel").toString());

    public static final String VERSION = "1.1";
    // the Stage used to show Scenic View
    private final Stage scenicViewStage;
    
    
    Thread shutdownHook = new Thread(){
        @Override
        public void run() {
            // We can't use close() because we are not in FXThread
            saveProperties();
        }
    };
    
    protected BorderPane borderPane;
    private SplitPane splitPane;
    private final TreeView<NodeInfo> treeView;
    private final List<TreeItem<NodeInfo>> treeViewData = new ArrayList<TreeItem<NodeInfo>>();
    private final ScrollPane scrollPane;
    private final AllDetailsPane allDetailsPane;
    private final EventLogPane eventLogPane;
    private static StatusBar statusBar;
    VBox leftPane;

    /**
     * Menu Options
     */
    protected MenuBar menuBar;
    private final CheckMenuItem showBoundsCheckbox;
    private final CheckMenuItem showBaselineCheckbox;
    private final CheckMenuItem showDefaultProperties;
    private final CheckMenuItem collapseControls;
    private final CheckMenuItem collapseContentControls;
    private final CheckMenuItem showFilteredNodesInTree;
    private final CheckMenuItem showNodesIdInTree;
    private final CheckMenuItem ignoreMouseTransparentNodes;
    private final CheckMenuItem autoRefreshStyleSheets;
    private final CheckMenuItem componentSelectOnClick;
    
    private Node selectedNode;
    private TreeItem<NodeInfo> previouslySelectedItem;
    
    List<NodeFilter> activeNodeFilters = new ArrayList<NodeFilter>();
    
    /**
     * Listeners and EventHandlers
     */
    private final EventHandler<? super Event> traceEventHandler = new EventHandler<Event>() {

        @Override public void handle(final Event event) {
            if(eventLogPane.isActive()) {
                eventLogPane.trace((Node)event.getSource(), event.getEventType().toString(), "");
            }
        }
    };
    
    private final Model2GUI stageModelListener = new Model2GUI() {

        private boolean isActive(final StageModel stageModel) {
            return activeStage == stageModel;
        }
        
        @Override public void updateWindowDetails(final StageModel stageModel, final Window targetWindow) {
            autoRefreshStyleSheets.setDisable(!stageModel.canStylesheetsBeRefreshed());

            if(isActive(stageModel))
                statusBar.updateWindowDetails(targetWindow);
        }

        @Override public void updateMousePosition(final StageModel stageModel, final String position) {
            if(isActive(stageModel))
                statusBar.updateMousePosition(position);
            
        }

        @Override public void overlayParentNotFound(final StageModel stageModel) {
            showBoundsCheckbox.setSelected(false);
            updateBoundsRects();
            showBoundsCheckbox.setDisable(true);
            showBaselineCheckbox.setSelected(false);
            updateBaseline();
            showBaselineCheckbox.setDisable(true);
        }

        @Override public void updateStageModel(final StageModel stageModel) {
            if(isActive(stageModel))
                ScenicView.this.updateStageModel(stageModel);
        }

        @Override public void selectOnClick(final StageModel stageModel, final TreeItem<NodeInfo> nodeData) {
            componentSelectOnClick.setSelected(false);
            if (nodeData != null) {
                treeView.getSelectionModel().select(nodeData);
            }
            scenicViewStage.toFront();
        }

        @Override public boolean isIgnoreMouseTransparent() {
            return ignoreMouseTransparentNodes.isSelected();
        }

        @Override public boolean isAutoRefreshStyles() {
            return autoRefreshStyleSheets.isSelected();
        }

        @Override public List<TreeItem<NodeInfo>> getTreeItems() {
            return treeViewData;
        }
    };

    
    /**
     * Special nodes included in target stages
     */
    private final ListChangeListener<Node> structureInvalidationListener;
    private final ChangeListener<Boolean> visibilityInvalidationListener;
    private final InvalidationListener selectedNodePropListener;

    
    private final Map<Node,PropertyTracker> propertyTrackers = new HashMap<Node, PropertyTracker>();

    
    private final List<StageModel> stages = new ArrayList<StageModel>();
    StageModel activeStage;

    public ScenicView(final Parent target, final Stage senicViewStage) {
        Persistence.loadProperties();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        setId("scenic-view");
        
        borderPane = new BorderPane();
        borderPane.setId("main-borderpane");

        /**
         * Create a filter for our own nodes
         */
        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return false;
            }

            @Override public boolean accept(final Node node) {
                // do not create tree nodes for our bounds rectangles
                return node.getId() == null || !node.getId().startsWith(SCENIC_VIEW_BASE_ID);
            }

            @Override public boolean ignoreShowFilteredNodesInTree() {
                return true;
            }

            @Override public boolean expandAllNodes() {
                return false;
            }
        });

        final TextField propertyFilterField = createFilterField("Property name or value", null);
        propertyFilterField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                filterProperties(propertyFilterField.getText());
            }
        });
        propertyFilterField.setDisable(true);

        eventLogPane = new EventLogPane();

        menuBar = new MenuBar();
        // menuBar.setId("main-menubar");

        // ---- File Menu
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
        final MenuItem findStageItem = new MenuItem("Find Stages");
        findStageItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                StageSelectionBox.make("Find Stages", ScenicView.this,stages);
            }
        });

        final Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(findStageItem, exitItem);

        // ---- Options Menu
        showBoundsCheckbox = buildCheckMenuItem("Show Bounds Overlays", "Show the bound overlays on selected", "Do not show bound overlays on selected", "showBounds", Boolean.TRUE);
        showBoundsCheckbox.setId("show-bounds-checkbox");
        // showBoundsCheckbox.setTooltip(new
        // Tooltip("Display a yellow highlight for boundsInParent and green outline for layoutBounds."));
        showBoundsCheckbox.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                updateBoundsRects();
            }
        });
        
        showDefaultProperties = buildCheckMenuItem("Show Default Properties", "Show default properties", "Hide default properties", "showDefaultProperties", Boolean.TRUE);
        showDefaultProperties.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                allDetailsPane.setShowDefaultProperties(showDefaultProperties.isSelected());
            }
        });
        final InvalidationListener menuTreeChecksListener = new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                updateStageModel(activeStage);
            }
        };
        collapseControls = buildCheckMenuItem("Collapse controls In Tree", "Controls will be collapsed", "Controls will be expanded", "collapseControls", Boolean.TRUE);
        collapseControls.selectedProperty().addListener(menuTreeChecksListener);

        collapseContentControls = buildCheckMenuItem("Collapse container controls In Tree", "Container controls will be collapsed", "Container controls will be expanded", "collapseContainerControls", Boolean.FALSE);
        collapseContentControls.selectedProperty().addListener(menuTreeChecksListener);
        collapseContentControls.disableProperty().bind(collapseControls.selectedProperty().not());
        
        final CheckMenuItem showCSSProperties = buildCheckMenuItem("Show CSS Properties", "Show CSS properties", "Hide CSS properties", "showCSSProperties", Boolean.FALSE);
        showCSSProperties.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                allDetailsPane.setShowCSSProperties(showCSSProperties.isSelected());
            }
        });

        showBaselineCheckbox = buildCheckMenuItem("Show Baseline Overlay", "Display a red line at the current node's baseline offset", "Do not show baseline overlay", "showBaseline", Boolean.FALSE);
        showBaselineCheckbox.setId("show-baseline-overlay");
        showBaselineCheckbox.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                updateBaseline();
            }
        });

        final CheckMenuItem automaticScenegraphStructureRefreshing = buildCheckMenuItem("Auto-Refresh Scenegraph", "Scenegraph structure will be automatically updated on change", "Scenegraph structure will NOT be automatically updated on change", "automaticScenegraphStructureRefreshing", Boolean.TRUE);
        final CheckMenuItem showInvisibleNodes = buildCheckMenuItem("Show Invisible Nodes In Tree", "Invisible nodes will be faded in the scenegraph tree", "Invisible nodes will not be shown in the scenegraph tree", "showInvisibleNodes", Boolean.FALSE);
        showInvisibleNodes.selectedProperty().addListener(menuTreeChecksListener);

        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return false;
            }

            @Override public boolean accept(final Node node) {
                return showInvisibleNodes.isSelected() || DisplayUtils.isNodeVisible(node);
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
        showFilteredNodesInTree.selectedProperty().addListener(menuTreeChecksListener);

        /**
         * Filter invisible nodes only makes sense if showFilteredNodesInTree is
         * not selected
         */
        showInvisibleNodes.disableProperty().bind(showFilteredNodesInTree.selectedProperty());

        componentSelectOnClick = buildCheckMenuItem("Component highlight/select on click", "Click on the scene to select a component", "", null, null);
        componentSelectOnClick.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                activeStage.componentSelectOnClick(newValue.booleanValue());
            }
        });

        ignoreMouseTransparentNodes = buildCheckMenuItem("Ignore MouseTransparent Nodes", "Transparent nodes will not be selectable", "Transparent nodes can be selected", "ignoreMouseTransparentNodes", Boolean.TRUE);

        autoRefreshStyleSheets = buildCheckMenuItem("Auto-Refresh StyleSheets", "A background thread will check modifications on the css files to reload them if needed", "StyleSheets autorefreshing disabled", "autoRefreshStyleSheets", Boolean.FALSE);
        autoRefreshStyleSheets.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                for (final Iterator<StageModel> iterator = stages.iterator(); iterator.hasNext();) {
                    final StageModel stage = iterator.next();
                    stage.styleRefresher(newValue);
                }
            }
        });

        

        final Menu scenegraphMenu = new Menu("Scenegraph");
        scenegraphMenu.getItems().addAll(automaticScenegraphStructureRefreshing, autoRefreshStyleSheets, /**
         * 
         * new SeparatorMenuItem(), showScenegraphTrace,
         */
        new SeparatorMenuItem(), componentSelectOnClick, ignoreMouseTransparentNodes);

        final Menu displayOptionsMenu = new Menu("Display Options");

        final Menu ruler = new Menu("Ruler");
        final Slider slider = new Slider(5, 50, 10);
        final Label sliderValue = new Label();
        slider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                activeStage.grid.updateSeparation(newValue.doubleValue());
                sliderValue.setText(DisplayUtils.format(newValue.doubleValue()));
            }
        });
        final HBox box = new HBox();
        sliderValue.setPrefWidth(40);
        sliderValue.setText(DisplayUtils.format(slider.getValue()));
        box.getChildren().addAll(sliderValue, slider);
        final CustomMenuItem rulerSlider = new CustomMenuItem(box);

        final CheckMenuItem showRuler = buildCheckMenuItem("Show Ruler", "Show ruler in the scene for alignment purposes", "", null, null);
        showRuler.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                activeStage.showGrid(newValue.booleanValue(), (int) slider.getValue());
            }
        });
        rulerSlider.disableProperty().bind(showRuler.selectedProperty().not());
        slider.disableProperty().bind(showRuler.selectedProperty().not());

        ruler.getItems().addAll(showRuler, rulerSlider);

        displayOptionsMenu.getItems().addAll(showDefaultProperties, showCSSProperties, new SeparatorMenuItem(), showBoundsCheckbox, showBaselineCheckbox, new SeparatorMenuItem(), ruler, new SeparatorMenuItem(), showFilteredNodesInTree, showInvisibleNodes, showNodesIdInTree, collapseControls, collapseContentControls);

        final Menu aboutMenu = new Menu("Help");

        final MenuItem help = new MenuItem("Help Contents");
        help.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                HelpBox.make("Help Contents", HELP_URL, scenicViewStage);
            }
        });

        final MenuItem about = new MenuItem("About");
        about.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                AboutBox.make("About", scenicViewStage);
            }
        });

        aboutMenu.getItems().addAll(help, about);

        menuBar.getMenus().addAll(fileMenu, displayOptionsMenu, scenegraphMenu, aboutMenu);

        borderPane.setTop(menuBar);

        splitPane = new SplitPane();
        splitPane.setId("main-splitpane");

        allDetailsPane = new AllDetailsPane();
        allDetailsPane.setShowCSSProperties(showCSSProperties.isSelected());
        scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(allDetailsPane);

        treeView = new TreeView<NodeInfo>();
        treeView.setId("main-treeview");
        treeView.setPrefSize(200, 500);
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<NodeInfo>>() {

            @Override public void changed(final ObservableValue<? extends TreeItem<NodeInfo>> arg0, final TreeItem<NodeInfo> arg1, final TreeItem<NodeInfo> newValue) {
                        final TreeItem<NodeInfo> selected = newValue;
                        setSelectedNode(selected != null ? selected.getValue().getNode() : null);
                        propertyFilterField.setText("");
                        propertyFilterField.setDisable(selected == null);
                        filterProperties(propertyFilterField.getText());
            }
        });
        treeView.setCellFactory(new Callback<TreeView<NodeInfo>, TreeCell<NodeInfo>>() {
            @Override public TreeCell<NodeInfo> call(final TreeView<NodeInfo> node) {
                return new CustomTreeCell();
            }
        });
        treeView.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (ev.isSecondaryButtonDown()) {
                    treeView.getSelectionModel().clearSelection();
                }
            }
        });

        leftPane = new VBox();
        leftPane.setId("main-nodeStructure");

        final TextField idFilterField = createFilterField("Node ID");
        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return true;
            }

            @Override public boolean accept(final Node node) {
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
        final TextField classNameFilterField = createFilterField("Node className");
        activeNodeFilters.add(new NodeFilter() {
            @Override public boolean allowChildrenOnRejection() {
                return true;
            }

            @Override public boolean accept(final Node node) {
                if (classNameFilterField.getText().equals(""))
                    return true;

                // Allow reduces or complete className
                return nodeClass(node).toLowerCase().indexOf(classNameFilterField.getText().toLowerCase()) != -1;
            }

            @Override public boolean ignoreShowFilteredNodesInTree() {
                return false;
            }

            @Override public boolean expandAllNodes() {
                return !classNameFilterField.getText().equals("");
            }
        });
        final Button b1 = new Button();
        b1.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                idFilterField.setText("");
                updateStageModel(activeStage);
            }
        });
        final Button b2 = new Button();
        b2.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                classNameFilterField.setText("");
                updateStageModel(activeStage);
            }
        });
        final Button b3 = new Button();
        b3.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b3.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                propertyFilterField.setText("");
                filterProperties(propertyFilterField.getText());
            }
        });

        final GridPane filtersGridPane = new GridPane();
        filtersGridPane.setVgap(5);
        filtersGridPane.setHgap(5);
        filtersGridPane.setSnapToPixel(true);
        filtersGridPane.setPadding(new Insets(0, 5, 5, 0));
        filtersGridPane.setId("main-filters-grid-pane");

        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b1, Priority.NEVER);
        GridPane.setHgrow(classNameFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b2, Priority.NEVER);
        GridPane.setHgrow(propertyFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b3, Priority.NEVER);

        filtersGridPane.add(new Label("ID Filter:"), 1, 1);
        filtersGridPane.add(idFilterField, 2, 1);
        filtersGridPane.add(b1, 3, 1);
        filtersGridPane.add(new Label("Class Filter:"), 1, 2);
        filtersGridPane.add(classNameFilterField, 2, 2);
        filtersGridPane.add(b2, 3, 2);
        filtersGridPane.add(new Label("Property Filter:"), 1, 3);
        filtersGridPane.add(propertyFilterField, 2, 3);
        filtersGridPane.add(b3, 3, 3);

        final TitledPane filtersPane = new TitledPane("Filters", filtersGridPane);
        filtersPane.setId("main-filters");
        filtersPane.setMinHeight(filtersGridPane.getPrefHeight());

        treeView.setMaxHeight(Double.MAX_VALUE);
        final TitledPane treeViewPane = new TitledPane("Scenegraph", treeView);
        treeViewPane.setMaxHeight(Double.MAX_VALUE);
        // This solves the resizing of filtersPane
        treeViewPane.setPrefHeight(50);
        leftPane.getChildren().addAll(filtersPane, treeViewPane);
        VBox.setVgrow(treeViewPane, Priority.ALWAYS);
        
        final TabPane tabPane = new TabPane();
        final Tab detailsTab = new Tab("Details");
        detailsTab.setGraphic(new ImageView(DisplayUtils.getUIImage("details.png")));
        detailsTab.setContent(scrollPane);
        detailsTab.setClosable(false);
        final Tab eventsTab = new Tab("Events");
        eventsTab.setContent(eventLogPane);
        eventsTab.setGraphic(new ImageView(DisplayUtils.getUIImage("flag_red.png")));
        eventsTab.setClosable(false);
        tabPane.getTabs().addAll(detailsTab, eventsTab);
        Persistence.loadProperty("splitPaneDividerPosition", splitPane, 0.3);
        
        splitPane.getItems().addAll(leftPane, tabPane);

        borderPane.setCenter(splitPane);

        statusBar = new StatusBar();

        borderPane.setBottom(statusBar);

        getChildren().add(borderPane);


        selectedNodePropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                updateBoundsRects();
            }
        };

        
        visibilityInvalidationListener = new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean arg1, final Boolean newValue) {
                if (automaticScenegraphStructureRefreshing.isSelected()) {
                    @SuppressWarnings("unchecked") final Node bean = (Node) ((Property<Boolean>) observable).getBean();
                    final boolean filteringActive = !showInvisibleNodes.isSelected() && !showFilteredNodesInTree.isSelected();
                    if (filteringActive && !newValue) {
                        removeTreeItem(bean, false, false);
                        statusBar.updateNodeCount(activeStage.targetScene);
                    } else if (filteringActive && newValue) {
                        addNewNode(bean);
                        statusBar.updateNodeCount(activeStage.targetScene);
                    } else {
                        /**
                         * This should be improved ideally we use request a
                         * repaint for the TreeItem
                         */
                        removeTreeItem(bean, false, false);
                        addNewNode(bean);
                    }
                }
            }
        };

        structureInvalidationListener = new ListChangeListener<Node>() {
            @Override public void onChanged(final Change<? extends Node> c) {
                if (automaticScenegraphStructureRefreshing.isSelected()) {
                    while (c.next()) {
                        for (final Node dead : c.getRemoved()) {
                            eventLogPane.trace(dead, EventLogPane.NODE_REMOVED, "");
                            
                            removeTreeItem(dead, true, false);
                        }
                        for (final Node alive : c.getAddedSubList()) {
                            eventLogPane.trace(alive, EventLogPane.NODE_ADDED, "");
                            
                            addNewNode(alive);
                        }
                    }
                    statusBar.updateNodeCount(activeStage.targetScene);
                }
            }
        };
        addNewStage(new StageModel(target));
        this.scenicViewStage = senicViewStage;
        Persistence.loadProperty("stageWidth", senicViewStage, 640);
        Persistence.loadProperty("stageHeight", senicViewStage, 800);
    }

    public void addNewStage(final StageModel stageModel)
    {
        if(stages.isEmpty()) {
            activeStage = stageModel;
        }
        else {
            stages.clear();
            activeStage = stageModel;
        }
        stages.add(stageModel);
        stageModel.setModel2gui(stageModelListener);
    }
 
    private void updateStageModel(final StageModel model) {
        final Parent value = model.target;

        final Node previouslySelected = selectedNode;
        treeViewData.clear();
        previouslySelectedItem = null;
        TreeItem<NodeInfo> root = createTreeItem(value, true);

        /**
         * If the target is the root node of the scene include subwindows
         */
        if (activeStage.targetScene != null && activeStage.targetScene.getRoot() == value) {
            String title = "App";
            Image targetStageImage = null;
            if(activeStage.targetScene.getWindow() instanceof Stage) {
                final Stage s = ((Stage)activeStage.targetScene.getWindow());
                if(!s.getIcons().isEmpty()) {
                    targetStageImage = ((Stage)activeStage.targetScene.getWindow()).getIcons().get(0);
                }
                title = s.getTitle()!=null?s.getTitle():"App";
            }
            if(targetStageImage==null) {
                targetStageImage = FX_APP_ICON;
            }
            
            final TreeItem<NodeInfo> app = new TreeItem<NodeInfo>(new DummyNodeInfo(title), new ImageView(targetStageImage));
            app.setExpanded(true);
            app.getChildren().add(root);
            if(!activeStage.popupWindows.isEmpty()) {
                final TreeItem<NodeInfo> subWindows = new TreeItem<NodeInfo>(new DummyNodeInfo("SubWindows"), new ImageView(DisplayUtils.getNodeIcon("Panel").toString()));
                for (int i = 0; i < activeStage.popupWindows.size(); i++) {
                    final PopupWindow window = activeStage.popupWindows.get(i);
                    final URL windowIcon = DisplayUtils.getNodeIcon(nodeClass(window));
                    Image targetWindowImage = PANEL_NODE_IMAGE;
                    if(windowIcon != null) {
                        targetWindowImage = new Image(windowIcon.toString());
                    }
                    final TreeItem<NodeInfo> subWindow = new TreeItem<NodeInfo>(new DummyNodeInfo("SubWindow -"+nodeClass(window)), new ImageView(targetWindowImage));
                    subWindow.getChildren().add(createTreeItem(window.getScene().getRoot(), true));
                    subWindows.getChildren().add(subWindow);
                }
                app.getChildren().add(subWindows);
            }
            root = app;
        }

        treeView.setRoot(root);
        if (previouslySelectedItem != null) {
            /**
             * TODO Why this is not working??
             */
            // treeView.getSelectionModel().clearSelection();
            // treeView.getSelectionModel().select(previouslySelectedItem);
            /**
             * TODO Remove
             */
            setSelectedNode(previouslySelected);

        }
    }

    private TreeItem<NodeInfo> createTreeItem(final Node node, final boolean updateCount) {
        /**
         * Strategy:
         * 
         * 1) Check is the node is valid for all the filters 2) If it is,
         * include the node and its valid children 3) If it is not and the
         * filter does not allow children to be included do not include the node
         * 4) If it is not and the filter allows children to be included check
         * whether there are valid childrens or not. In case there is at least
         * one, include this node as invalidForFilter, if it is not, do not
         * allow this node to be included
         */
        boolean nodeAccepted = true;
        boolean childrenAccepted = true;
        boolean ignoreShowFiltered = false;
        boolean expand = false;

        for (final NodeFilter filter : activeNodeFilters) {
            if (!filter.accept(node)) {
                nodeAccepted = false;
                ignoreShowFiltered |= filter.ignoreShowFilteredNodesInTree();
                childrenAccepted &= filter.allowChildrenOnRejection();
            }
            expand |= filter.expandAllNodes();
        }
        /**
         * Incredibly ugly workaround for the cursor in the TextField that
         * changes its visibility constantly
         */
        if (node.getId() == null || !node.getId().startsWith(SCENIC_VIEW_BASE_ID)) {
            node.visibleProperty().removeListener(visibilityInvalidationListener);
            node.visibleProperty().addListener(visibilityInvalidationListener);
            propertyTracker(node, true);
            
            node.removeEventFilter(Event.ANY, traceEventHandler);
            node.addEventFilter(Event.ANY, traceEventHandler);
        }
        final NodeData nodeData = new NodeData(node, showNodesIdInTree.isSelected());
        final TreeItem<NodeInfo> treeItem = new TreeItem<NodeInfo>(nodeData, new ImageView(nodeData.getIcon()));
        if (nodeData.node == selectedNode) {
            previouslySelectedItem = treeItem;
        }
        /**
         * TODO Improve this calculation THIS IS NOT CORRECT AS NEW NODES ARE INCLUDED ON TOP
         * a) updateCount=true: We are adding all the nodes
         * b) updateCount=false: We are adding only one node, find its position
         */
        treeViewData.add(treeItem);
        node.getProperties().put(SCENIC_VIEW_BASE_ID + "TreeItem", treeItem);

        if (node instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable().removeListener(structureInvalidationListener);
            ((Parent) node).getChildrenUnmodifiable().addListener(structureInvalidationListener);
            final List<TreeItem<NodeInfo>> childItems = new ArrayList<TreeItem<NodeInfo>>();
            for (final Node child : ((Parent) node).getChildrenUnmodifiable()) {
                childItems.add(createTreeItem(child, updateCount));
            }
            for (final TreeItem<NodeInfo> childItem : childItems) {
                // childItems[x] could be null because of bounds rectangles or
                // filtered nodes
                if (childItem != null) {
                    treeItem.getChildren().add(childItem);
                }
            }
            boolean mustBeExpanded = expand  || !(node instanceof Control) || !collapseControls.isSelected();
            if(!mustBeExpanded && !collapseContentControls.isSelected()) {
            	mustBeExpanded = node instanceof TabPane || node instanceof SplitPane || node instanceof ScrollPane || node instanceof Accordion || node instanceof TitledPane;
            }
            
            treeItem.setExpanded(mustBeExpanded);
        }
        if(updateCount)
            statusBar.updateNodeCount(activeStage.targetScene);

        if (nodeAccepted) {
            return treeItem;
        } else if (!nodeAccepted && !ignoreShowFiltered && showFilteredNodesInTree.isSelected()) {
            /**
             * Mark the node as invalidForFilter
             */
            nodeData.invalidForFilter = true;
            return treeItem;
        } else if (!nodeAccepted && childrenAccepted) {
            /**
             * Only return if the node has children
             */
            if (treeItem.getChildren().isEmpty()) {
                treeViewData.remove(treeItem);
                return null;
            } else {
                /**
                 * Mark the node as invalidForFilter
                 */
                nodeData.invalidForFilter = true;
                return treeItem;
            }
        } else {
            treeViewData.remove(treeItem);
            return null;
        }
    }

    private void addNewNode(final Node alive) {
        final TreeItem<NodeInfo> selected = treeView.getSelectionModel().getSelectedItem();
        final TreeItem<NodeInfo> TreeItem = createTreeItem(alive, false);
        // childItems[x] could be null because of bounds
        // rectangles or filtered nodes
        if (TreeItem != null) {
            final Parent parent = alive.getParent();
            @SuppressWarnings("unchecked") final TreeItem<NodeInfo> parentTreeItem = (TreeItem<NodeInfo>) parent.getProperties().get(SCENIC_VIEW_BASE_ID + "TreeItem");

            /**
             * In some situations node could be previously added
             */
            final List<TreeItem<NodeInfo>> actualNodes = parentTreeItem.getChildren();
            boolean found = false;
            for (final TreeItem<NodeInfo> node : actualNodes) {
                if (node.getValue().getNode() == alive) {
                    found = true;
                }

            }
            if (!found) {
                parentTreeItem.getChildren().add(TreeItem);
            }
        }
        if(selected != null) {
            // Ugly workaround
            treeView.getSelectionModel().select(selected);
        }
    }

    protected void filterProperties(final String text) {
        allDetailsPane.filterProperties(text);
    }
    
    private void removeTreeItem(final Node node, final boolean removeVisibilityListener, final boolean updateCount) {
        TreeItem<NodeInfo> selected = null;
        if (selectedNode == node) {
            treeView.getSelectionModel().clearSelection();
            setSelectedNode(null);
        }
        else {
            // Ugly workaround
            selected = treeView.getSelectionModel().getSelectedItem();
        }
        @SuppressWarnings("unchecked") final TreeItem<NodeInfo> treeItem = (TreeItem<NodeInfo>) node.getProperties().get(SCENIC_VIEW_BASE_ID + "TreeItem");
        final List<TreeItem<NodeInfo>> treeItemChildren = treeItem.getChildren();
        if (treeItemChildren != null) {
            /**
             * Do not use directly the list as it will suffer concurrent
             * modifications
             */
            @SuppressWarnings("unchecked") final TreeItem<NodeInfo> children[] = treeItemChildren.toArray(new TreeItem[treeItemChildren.size()]);
            for (int i = 0; i < children.length; i++) {
                removeTreeItem(children[i].getValue().getNode(), removeVisibilityListener, updateCount);
            }
        }
        final NodeInfo nodeData = treeItem.getValue();
        if (nodeData.getNode() instanceof Parent) {
            ((Parent) node).getChildrenUnmodifiable().removeListener(structureInvalidationListener);
        }
        if (nodeData.getNode() != null && removeVisibilityListener) {
            node.visibleProperty().removeListener(visibilityInvalidationListener);
            propertyTracker(node, false);
            node.removeEventFilter(Event.ANY, traceEventHandler);
        }
        // This does not seem to delete the TreeItem from the tree -- only moves
        // it up a level visually
        /**
         * I don't know why this protection is needed
         */
        if (treeItem.getParent() != null) {
            treeItem.getParent().getChildren().remove(treeItem);
        }
        treeViewData.remove(treeItem);
        if(selected!=null) {
         // Ugly workaround
            treeView.getSelectionModel().select(selected);
        }
        if(updateCount) {
            statusBar.updateNodeCount(activeStage.targetScene);
		}
    }

    private void setSelectedNode(final Node value) {
        if (value != selectedNode) {
            storeSelectedNode(value);
            eventLogPane.setSelectedNode(value);
        }
    }

    private void storeSelectedNode(final Node value) {
        final Node old = selectedNode;
        if (old != null) {
            old.boundsInParentProperty().removeListener(selectedNodePropListener);
            old.layoutBoundsProperty().removeListener(selectedNodePropListener);
        }
        selectedNode = value;
        allDetailsPane.setTarget(selectedNode);
        setStatusText("Label on the labels to modify its values. The panel could have different capabilities. When changed the values will be highlighted", 8000);

        if (selectedNode != null) {
            selectedNode.boundsInParentProperty().addListener(selectedNodePropListener);
            selectedNode.layoutBoundsProperty().addListener(selectedNodePropListener);
        }
        updateBoundsRects();
        updateBaseline();

    }
    
    private void updateBoundsRects() {
        if(showBoundsCheckbox.isSelected()) {
            activeStage.updateBoundsRects(selectedNode);
        }
        else {
            activeStage.updateBoundsRects(null);
        }
    }

    private void updateBaseline() {
        if (showBaselineCheckbox.isSelected() && selectedNode != null) {
            final double baseline = selectedNode.getBaselineOffset();
            final Bounds bounds = selectedNode.getLayoutBounds();
            activeStage.updateBaseline(true, selectedNode.localToScene(bounds.getMinX(), bounds.getMinY() + baseline), bounds.getWidth());
            
        } else {
            activeStage.updateBaseline(false, null, 0);
        }
    }



    private void updateSceneDetails() {
        statusBar.updateSceneDetails(activeStage.targetScene);
        activeStage.updateSceneDetails();
    }

    private CheckMenuItem buildCheckMenuItem(final String text, final String toolTipSelected, final String toolTipNotSelected, final String property, final Boolean value) {
        final CheckMenuItem menuItem = new CheckMenuItem(text);
        if (property != null)
            Persistence.loadProperty(property, menuItem, value);
        menuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                setStatusText(newValue ? toolTipSelected : toolTipNotSelected, 4000);
            }
        });
        return menuItem;
    }

    private TextField createFilterField(final String prompt) {
        return createFilterField(prompt, new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                updateStageModel(activeStage);
            }
        });
    }

    private TextField createFilterField(final String prompt, final EventHandler<KeyEvent> keyHandler) {
        final TextField filterField = new TextField();
        filterField.setPromptText(prompt);
        if (keyHandler != null)
            filterField.setOnKeyReleased(keyHandler);
        filterField.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue)
                    setStatusText("Type any text for filtering");
                else
                    clearStatusText();
            }
        });
        return filterField;
    }


    public void close() {
        for (final Iterator<StageModel> iterator = stages.iterator(); iterator.hasNext();) {
            final StageModel stage = iterator.next();
            stage.close();
        }
        
        saveProperties();
    }

    private void saveProperties() {
        final Properties properties = new Properties();
        properties.put("ignoreMouseTransparentNodes", Boolean.toString(ignoreMouseTransparentNodes.isSelected()));
        Persistence.saveProperties(properties);
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
    
    private void propertyTracker(final Node node, final boolean add) {
        PropertyTracker tracker = propertyTrackers.get(node);
        if(tracker != null) {
            tracker.clear();
        }
        if(add) {
            tracker = new PropertyTracker() {
                
                @Override protected void updateDetail(final String propertyName, @SuppressWarnings("rawtypes") final ObservableValue property) {
                    /**
                     * Remove the bean
                     */
                    eventLogPane.trace(node, EventLogPane.PROPERTY_CHANGED, propertyName+"="+property.getValue());
                   
                }
            };
            tracker.setTarget(node);
            propertyTrackers.put(node, tracker);
        }
    }
    

    public static void show(final Scene target) {
        show(target.getRoot());
    }

    public static void show(final Parent target) {
        final Stage stage = new Stage();
        // workaround for RT-10714
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View v"+VERSION);
        show(new ScenicView(target, stage), stage);
    }

    public static void show(final ScenicView scenicview, final Stage stage) {
        final Scene scene = new Scene(scenicview);
        scene.getStylesheets().addAll(STYLESHEETS);
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        if (scenicview.activeStage.targetWindow != null) {
            final Window targetWindow = scenicview.activeStage.targetWindow;
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
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            @Override public void handle(final WindowEvent arg0) {
                Runtime.getRuntime().removeShutdownHook(scenicview.shutdownHook);
                scenicview.close();
            }
        });
        stage.show();
    }

}
