/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.util.*;

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
import javafx.stage.*;

import com.javafx.experiments.scenicview.ScenegraphTreeView.SelectedNodeContainer;
import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.event.*;
import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.javafx.experiments.scenicview.details.AllDetailsPane;
import com.javafx.experiments.scenicview.dialog.*;

/**
 * 
 * @author aim
 */
public class ScenicView extends Region implements SelectedNodeContainer {

    private static final String HELP_URL = "http://fxexperience.com/scenic-view/help";
    public static final String STYLESHEETS = "com/javafx/experiments/scenicview/scenicview.css";
    public static final Image APP_ICON = DisplayUtils.getUIImage("mglass.gif");

    public static final String VERSION = "1.1";
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
    private static StatusBar statusBar;
    private final VBox leftPane;

    /**
     * Menu Options
     */
    protected final MenuBar menuBar;
    private final CheckMenuItem showFilteredNodesInTree;
    private final CheckMenuItem showNodesIdInTree;
    private final CheckMenuItem autoRefreshStyleSheets;
    private final CheckMenuItem componentSelectOnClick;

    private final Configuration configuration = new Configuration();

    private final AppEventDispatcher stageModelListener = new AppEventDispatcher() {

        private boolean isActive(final StageID stageID) {
            return activeStage.getID().equals(stageID);
        }

        @Override public void dispatchEvent(final AppEvent appEvent) {
            switch (appEvent.getType()) {
            case EVENT_LOG:
                eventLogPane.trace((EvLogEvent) appEvent);
                break;
            case MOUSE_POSITION:
                if (isActive(appEvent.getStageID()))
                    statusBar.updateMousePosition(((MousePosEvent) appEvent).getPosition());
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

            case NODE_ADDED:
                treeView.addNewNode(((NodeAddRemoveEvent) appEvent).getNode(), showNodesIdInTree.isSelected(), showFilteredNodesInTree.isSelected());
                break;

            case NODE_REMOVED:
                treeView.removeNode(((NodeAddRemoveEvent) appEvent).getNode());
                break;

            case ROOT_UPDATED:
                treeView.updateStageModel(((NodeAddRemoveEvent) appEvent).getNode(), showNodesIdInTree.isSelected(), showFilteredNodesInTree.isSelected());
                break;

            default:
                System.out.println("Unused event");
                break;
            }
        }
    };

    private final List<AppController> apps = new ArrayList<AppController>();
    StageController activeStage;
    private SVNode selectedNode;

    public ScenicView(final List<AppController> controllers, final Stage senicViewStage) {
        Persistence.loadProperties();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        setId("scenic-view");

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
                return node.getId() == null || !node.getId().startsWith(StageController.SCENIC_VIEW_BASE_ID);
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

        eventLogPane = new EventLogPane(this);

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
                StageSelectionBox.make("Find Stages", ScenicView.this, apps);
            }
        });

        final Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(findStageItem, exitItem);

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

        final CheckMenuItem showDefaultProperties = buildCheckMenuItem("Show Default Properties", "Show default properties", "Hide default properties", "showDefaultProperties", Boolean.TRUE);
        showDefaultProperties.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                allDetailsPane.setShowDefaultProperties(showDefaultProperties.isSelected());
            }
        });
        final InvalidationListener menuTreeChecksListener = new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                activeStage.update();
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

        final CheckMenuItem showCSSProperties = buildCheckMenuItem("Show CSS Properties", "Show CSS properties", "Hide CSS properties", "showCSSProperties", Boolean.FALSE);
        showCSSProperties.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                allDetailsPane.setShowCSSProperties(showCSSProperties.isSelected());
            }
        });

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
                activeStage.componentSelectOnClick(newValue.booleanValue());
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

        autoRefreshStyleSheets = buildCheckMenuItem("Auto-Refresh StyleSheets", "A background thread will check modifications on the css files to reload them if needed", "StyleSheets autorefreshing disabled", "autoRefreshStyleSheets", Boolean.FALSE);
        autoRefreshStyleSheets.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                configuration.setAutoRefreshStyles(newValue);
                configurationUpdated();
            }
        });
        configuration.setAutoRefreshStyles(autoRefreshStyleSheets.isSelected());

        final Menu scenegraphMenu = new Menu("Scenegraph");
        scenegraphMenu.getItems().addAll(automaticScenegraphStructureRefreshing, autoRefreshStyleSheets, new SeparatorMenuItem(), componentSelectOnClick, ignoreMouseTransparentNodes);

        final Menu displayOptionsMenu = new Menu("Display Options");

        final Menu ruler = new Menu("Ruler");
        final Slider slider = new Slider(5, 50, 10);
        final TextField sliderValue = new TextField();
        slider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                configuration.setRulerSeparation((int) newValue.doubleValue());
                configurationUpdated();
                sliderValue.setText(DisplayUtils.format(newValue.doubleValue()));
            }
        });
        final HBox box = new HBox();
        sliderValue.setPrefWidth(40);
        sliderValue.setText(DisplayUtils.format(slider.getValue()));
        sliderValue.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                final double value = DisplayUtils.parse(sliderValue.getText());
                if (value >= slider.getMin() && value <= slider.getMax()) {
                    configuration.setRulerSeparation((int) slider.getValue());
                    configurationUpdated();
                    slider.setValue(value);
                } else if (value < slider.getMin()) {
                    sliderValue.setText(DisplayUtils.format(slider.getMin()));
                    slider.setValue(slider.getMin());
                } else {
                    sliderValue.setText(DisplayUtils.format(slider.getMax()));
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
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(allDetailsPane);

        treeView = new ScenegraphTreeView(activeNodeFilters, this);
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<SVNode>>() {

            @Override public void changed(final ObservableValue<? extends TreeItem<SVNode>> arg0, final TreeItem<SVNode> arg1, final TreeItem<SVNode> newValue) {
                final TreeItem<SVNode> selected = newValue;
                setSelectedNode(selected != null ? selected.getValue() : null);
                propertyFilterField.setText("");
                propertyFilterField.setDisable(selected == null);
                filterProperties(propertyFilterField.getText());
            }
        });

        leftPane = new VBox();
        leftPane.setId("main-nodeStructure");

        final TextField idFilterField = createFilterField("Node ID");
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
        final TextField classNameFilterField = createFilterField("Node className");
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
        final Button b1 = new Button();
        b1.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                idFilterField.setText("");
                activeStage.update();
            }
        });
        final Button b2 = new Button();
        b2.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                classNameFilterField.setText("");
                activeStage.update();
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

        for (int i = 0; i < controllers.size(); i++) {
            addNewApp(controllers.get(i));
        }

        this.scenicViewStage = senicViewStage;
        Persistence.loadProperty("stageWidth", senicViewStage, 640);
        Persistence.loadProperty("stageHeight", senicViewStage, 800);
    }

    protected void configurationUpdated() {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                stages.get(j).configurationUpdated(configuration);
            }
        }
    }

    public void addNewApp(final AppController appController) {
        if (!apps.contains(appController)) {
            if (apps.isEmpty()) {
                activeStage = appController.getStages().get(0);
            } else {
                apps.clear();
                activeStage = appController.getStages().get(0);
            }
            apps.add(appController);
        }
        final List<StageController> stages = appController.getStages();
        for (int j = 0; j < stages.size(); j++) {
            stages.get(j).setEventDispatcher(stageModelListener);
        }
        configurationUpdated();
    }

    //
    // public void addNewStage(final StageController stageModel) {
    // if (apps.isEmpty()) {
    // activeStage = stageModel;
    // } else {
    // apps.clear();
    // activeStage = stageModel;
    // }
    // apps.add(stageModel);
    // stageModel.setEventDispatcher(stageModelListener);
    // configurationUpdated();
    //
    // }

    void update() {
        for (int i = 0; i < apps.size(); i++) {
            final List<StageController> stages = apps.get(i).getStages();
            for (int j = 0; j < stages.size(); j++) {
                stages.get(j).update();
            }
        }
    }

    protected void filterProperties(final String text) {
        allDetailsPane.filterProperties(text);
    }

    @Override public void setSelectedNode(final SVNode value) {
        if (value != selectedNode) {
            storeSelectedNode(value);
            eventLogPane.setSelectedNode(value);
        }
    }

    @Override public SVNode getSelectedNode() {
        return selectedNode;
    }

    private void storeSelectedNode(final SVNode value) {
        selectedNode = value;
        allDetailsPane.setTarget(selectedNode.getImpl());
        setStatusText("Label on the labels to modify its values. The panel could have different capabilities. When changed the values will be highlighted", 8000);
        activeStage.setSelectedNode(value);
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
                activeStage.update();
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
        for (final Iterator<AppController> iterator = apps.iterator(); iterator.hasNext();) {
            final AppController stage = iterator.next();
            stage.close();
        }

        saveProperties();
    }

    private void saveProperties() {
        final Properties properties = new Properties();
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

    public static void show(final Scene target) {
        show(target.getRoot());
    }

    public static void show(final Parent target) {
        final Stage stage = new Stage();
        // workaround for RT-10714
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View v" + VERSION);
        final List<AppController> controllers = new ArrayList<AppController>();
        if (target != null) {
            final StageController sController = new StageController(target);
            final AppController aController = new AppController("Local");
            aController.getStages().add(sController);
            controllers.add(aController);
        }
        show(new ScenicView(controllers, stage), stage);
    }

    public static void show(final ScenicView scenicview, final Stage stage) {
        final Scene scene = new Scene(scenicview);
        scene.getStylesheets().addAll(STYLESHEETS);
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        if (scenicview.activeStage != null)
            scenicview.activeStage.placeStage(stage);

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            @Override public void handle(final WindowEvent arg0) {
                Runtime.getRuntime().removeShutdownHook(scenicview.shutdownHook);
                scenicview.close();
            }
        });
        stage.show();
    }

}
