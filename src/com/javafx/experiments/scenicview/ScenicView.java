/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import static com.javafx.experiments.scenicview.DisplayUtils.nodeClass;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Callback;

import com.javafx.experiments.scenicview.details.AllDetailsPane;

/**
 * 
 * @author aim
 */
public class ScenicView extends Region {

    private static final String SCENIC_VIEW_PROPERTIES_FILE = "scenicView.properties";
    private static final String HELP_URL = "http://fxexperience.com/scenic-view/help";
    private static final String SCENIC_VIEW_BASE_ID = "ScenicView.";

    static final Image APP_ICON = DisplayUtils.getUIImage("mglass.gif");

    public static void show(final Scene target) {
        show(target.getRoot());
    }

    public static void show(final Parent target) {
        final Stage stage = new Stage();
        // workaround for RT-10714
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View");
        show(new ScenicView(target, stage), stage);
    }

    public static void show(final ScenicView scenicview, final Stage stage) {
        final Scene scene = new Scene(scenicview);
        scene.getStylesheets().addAll("com/javafx/experiments/scenicview/scenicview.css");
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        if (scenicview.target.getScene() != null && scenicview.target.getScene().getWindow() != null) {
            final Window targetWindow = scenicview.target.getScene().getWindow();
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
                scenicview.close();
            }
        });
        stage.show();
    }

    protected BorderPane borderPane;
    protected MenuBar menuBar;
    private SplitPane splitPane;
    private TreeView<NodeInfo> treeView;
    private final List<TreeItem<NodeInfo>> treeViewData = new ArrayList<TreeItem<NodeInfo>>();
    private ScrollPane scrollPane;
    private AllDetailsPane allDetailsPane;
    private StructureTracePane structureTracePane;
    private static StatusBar statusBar;

    private CheckMenuItem showBoundsCheckbox;
    private CheckMenuItem showBaselineCheckbox;
    private CheckMenuItem showDefaultProperties;

    private CheckMenuItem showFilteredNodesInTree;
    private CheckMenuItem showNodesIdInTree;
    private CheckMenuItem ignoreMouseTransparentNodes;
    private CheckMenuItem autoRefreshStyleSheets;

    private StyleSheetRefresher refresher;
    private SubWindowChecker windowChecker;

    private Parent overlayParent;

    private Rectangle boundsInParentRect;
    private Rectangle layoutBoundsRect;
    private Line baselineLine;
    private Rectangle componentSelector;
    private Rectangle componentHighLighter;
    private RuleGrid grid;

    private Parent target;
    private Scene targetScene;
    private Window targetWindow;
    /**
     * Simplification for now, only a plain structure for now
     */
    private final List<PopupWindow> popupWindows = new ArrayList<PopupWindow>();
    private InvalidationListener targetScenePropListener;
    private InvalidationListener targetWindowPropListener;

    private Node selectedNode;
    private TreeItem<NodeInfo> previouslySelectedItem;

    List<NodeFilter> activeNodeFilters = new ArrayList<ScenicView.NodeFilter>();

    private InvalidationListener selectedNodePropListener;

    private ListChangeListener<Node> structureInvalidationListener;
    private ChangeListener<Boolean> visibilityInvalidationListener;

    VBox leftPane;
    
    private final EventHandler<? super MouseEvent> sceneHoverListener = new EventHandler<MouseEvent>() {

        @Override public void handle(final MouseEvent ev) {
            highlightHovered(ev.getX(), ev.getY());
        }

    };
    
    private static final Map<String, Image> loadedImages = new HashMap<String, Image>();
    private static final String CUSTOM_NODE_IMAGE = ScenicView.class.getResource("images/nodeicons/CustomNode.png").toString();

    public ScenicView() {
        loadProperties();
        setId("scenic-view");
        windowChecker = new SubWindowChecker();
        windowChecker.start();
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
        });

        final TextField propertyFilterField = createFilterField("Property name or value", null);
        propertyFilterField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                filterProperties(propertyFilterField.getText());
            }
        });
        propertyFilterField.setDisable(true);

        structureTracePane = new StructureTracePane();

        menuBar = new MenuBar();
        // menuBar.setId("main-menubar");

        // ---- File Menu
        final MenuItem exitItem = new MenuItem("E_xit Scenic View");
        exitItem.setAccelerator(KeyCombination.keyCombination("CTRL+Q"));
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                close();
                // TODO Why closing the Stage does not dispatch
                // WINDOW_CLOSE_REQUEST??
                getStage().close();
            }
        });

        final Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(exitItem);

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
        boundsInParentRect = new Rectangle();
        boundsInParentRect.setId(SCENIC_VIEW_BASE_ID + "boundsInParentRect");
        boundsInParentRect.setFill(Color.YELLOW);
        boundsInParentRect.setOpacity(.5);
        boundsInParentRect.setManaged(false);
        layoutBoundsRect = new Rectangle();
        layoutBoundsRect.setId(SCENIC_VIEW_BASE_ID + "layoutBoundsRect");
        layoutBoundsRect.setFill(null);
        layoutBoundsRect.setStroke(Color.GREEN);
        layoutBoundsRect.setStrokeType(StrokeType.INSIDE);
        layoutBoundsRect.setOpacity(.8);
        layoutBoundsRect.getStrokeDashArray().addAll(3.0, 3.0);
        layoutBoundsRect.setStrokeWidth(1);
        layoutBoundsRect.setManaged(false);

        showDefaultProperties = buildCheckMenuItem("Show Default Properties", "Show default properties", "Hide default properties", "showDefaultProperties", Boolean.TRUE);
        showDefaultProperties.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                setShowDefaultProperties(showDefaultProperties.isSelected());
            }
        });

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
        final InvalidationListener menuTreeChecksListener = new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                storeTarget(target);
            }
        };
        final CheckMenuItem automaticScenegraphStructureRefreshing = buildCheckMenuItem("Auto-Refresh Scenegraph", "Scenegraph structure will be automatically updated on change", "Scenegraph structure will NOT be automatically updated on change", "automaticScenegraphStructureRefreshing", Boolean.TRUE);
        final CheckMenuItem showInvisibleNodes = buildCheckMenuItem("Show Invisible Nodes In Tree", "Invisible nodes will be faded in the scenegraph tree", "Invisible nodes will not be shown in the scenegraph tree", "showInvisibleNodes", Boolean.FALSE);
        showInvisibleNodes.selectedProperty().addListener(menuTreeChecksListener);
        final CheckMenuItem showScenegraphTrace = buildCheckMenuItem("Show scenegraph trace", "Nodes included or removed from the scenegraph will be shown", "Scenegraph trace disabled", null, Boolean.FALSE);
        showScenegraphTrace.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {

                if (newValue) {
                    scrollPane.setContent(structureTracePane);
                } else {
                    scrollPane.setContent(allDetailsPane);
                }
                structureTracePane.activate(newValue);
            }
        });

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

        final CheckMenuItem componentSelectOnClick = buildCheckMenuItem("Component select on click", "Click on the scene to select a component", "", null, null);
        componentSelectOnClick.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                if (newValue) {
                    targetScene.addEventHandler(MouseEvent.MOUSE_MOVED, sceneHoverListener);
                    final Rectangle rect = new Rectangle();
                    rect.setFill(Color.TRANSPARENT);
                    rect.setWidth(targetWindow.getWidth());
                    rect.setHeight(targetWindow.getHeight());
                    rect.setId(SCENIC_VIEW_BASE_ID + "componentSelectorRect");
                    rect.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override public void handle(final MouseEvent ev) {
                            componentSelectOnClick.setSelected(false);
                            findDeepSelection(ev.getX(), ev.getY());
                            getStage().toFront();
                        }
                    });
                    rect.setManaged(false);
                    componentSelector = rect;
                    addToNode(target, componentSelector);
                    ((Stage) targetWindow).toFront();
                } else {
                    targetScene.removeEventHandler(MouseEvent.MOUSE_MOVED, sceneHoverListener);
                    if (componentHighLighter != null) {
                        removeFromNode(target, componentHighLighter);
                    }
                    if (componentSelector != null)
                        removeFromNode(target, componentSelector);
                }
            }
        });

        ignoreMouseTransparentNodes = buildCheckMenuItem("Ignore MouseTransparent Nodes", "Transparent nodes will not be selectable", "Transparent nodes can be selected", "ignoreMouseTransparentNodes", Boolean.TRUE);

        autoRefreshStyleSheets = buildCheckMenuItem("Auto-Refresh StyleSheets", "A background thread will check modifications on the css files to reload them if needed", "StyleSheets autorefreshing disabled", "autoRefreshStyleSheets", Boolean.FALSE);
        autoRefreshStyleSheets.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue) {
                    startRefresher();
                } else {
                    refresher.finish();
                }
            }

        });

        baselineLine = new Line();
        baselineLine.setId(SCENIC_VIEW_BASE_ID + "baselineLine");
        baselineLine.setStroke(Color.RED);
        baselineLine.setOpacity(.75);
        baselineLine.setStrokeWidth(1);
        baselineLine.setManaged(false);

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
        final DecimalFormat df = new DecimalFormat("0.0");
        slider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                grid.updateSeparation(newValue.doubleValue());
                sliderValue.setText(df.format(newValue.doubleValue()));
            }
        });
        final HBox box = new HBox();
        sliderValue.setPrefWidth(40);
        sliderValue.setText(df.format(slider.getValue()));
        box.getChildren().addAll(sliderValue, slider);
        final CustomMenuItem rulerSlider = new CustomMenuItem(box);

        final CheckMenuItem showRuler = buildCheckMenuItem("Show Ruler", "Show ruler in the scene for alignment purposes", "", null, null);
        showRuler.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean oldValue, final Boolean newValue) {
                if (newValue) {
                    grid = new RuleGrid((int) slider.getValue(), targetScene.getWidth(), targetScene.getHeight());
                    grid.setId(SCENIC_VIEW_BASE_ID + "ruler");
                    grid.setManaged(false);
                    addToNode(target, grid);
                } else {
                    if (grid != null)
                        removeFromNode(target, grid);
                }
            }
        });
        rulerSlider.disableProperty().bind(showRuler.selectedProperty().not());
        slider.disableProperty().bind(showRuler.selectedProperty().not());

        ruler.getItems().addAll(showRuler, rulerSlider);

        displayOptionsMenu.getItems().addAll(showDefaultProperties, showCSSProperties, new SeparatorMenuItem(), showBoundsCheckbox, showBaselineCheckbox, new SeparatorMenuItem(), ruler, new SeparatorMenuItem(), showFilteredNodesInTree, showInvisibleNodes, showNodesIdInTree);

        final Menu aboutMenu = new Menu("Help");

        final MenuItem help = new MenuItem("Help Contents");
        help.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                HelpNode.make("Help Contents", HELP_URL, stage);
            }
        });

        final MenuItem about = new MenuItem("About");
        about.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                AboutBox.make("About", stage);
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
        });
        final Button b1 = new Button();
        b1.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                idFilterField.setText("");
                storeTarget(target);
            }
        });
        final Button b2 = new Button();
        b2.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                classNameFilterField.setText("");
                storeTarget(target);
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

        splitPane.getItems().addAll(leftPane, scrollPane);
        splitPane.setDividerPosition(0, 0.3);

        borderPane.setCenter(splitPane);

        statusBar = new StatusBar();

        borderPane.setBottom(statusBar);

        getChildren().add(borderPane);

        targetScenePropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable value) {
                updateSceneDetails();
            }
        };

        targetWindowPropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable value) {
                statusBar.updateWindowDetails(targetWindow);
            }
        };

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
                        /**
                         * Remove the bean
                         */
                        structureTracePane.trace("DEL VISIB:", bean);
                        removeTreeItem(bean, false, false);
                        statusBar.updateNodeCount(targetScene);
                    } else if (filteringActive && newValue) {
                        structureTracePane.trace("ADD VISIB:", bean);
                        addNewNode(bean);
                        statusBar.updateNodeCount(targetScene);
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
                            structureTracePane.trace("DEL SCENE:", dead);
                            removeTreeItem(dead, true, false);
                        }
                        for (final Node alive : c.getAddedSubList()) {
                            structureTracePane.trace("ADD SCENE:", alive);
                            addNewNode(alive);
                        }
                    }
                    statusBar.updateNodeCount(targetScene);
                }
            }
        };
    }

    private void addNewNode(final Node alive) {

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
    }

    protected void filterProperties(final String text) {
        allDetailsPane.filterProperties(text);
    }

    public ScenicView(final Parent target, final Stage stage) {
        this();
        setTarget(target);
        setStage(stage);
    }

    // the Stage used to show Scenic View
    private Stage stage;
    private Properties properties;
    private final Map<String, Object> persistentComponents = new HashMap<String, Object>();
    private TreeItem<NodeInfo> previousHightLightedData;

    private Stage getStage() {
        return stage;
    }

    private void setStage(final Stage stage) {
        this.stage = stage;
    }

    public Parent getTarget() {
        return target;
    }

    public void setTarget(final Parent value) {
        if (target != value) {
            storeTarget(value);
        }
    }

    @SuppressWarnings("unchecked") protected void storeTarget(final Parent value) {
        // Parent old = this.target;

        this.target = value;
        final Node previouslySelected = selectedNode;
        treeViewData.clear();
        previouslySelectedItem = null;
        TreeItem<NodeInfo> root = createTreeItem(value, true);

        /**
         * If the target is the root node of the scene include subwindows
         */
        if (targetScene != null && targetScene.getRoot() == value && !popupWindows.isEmpty()) {

            final TreeItem<NodeInfo> app = new TreeItem<NodeInfo>(new DummyNodeInfo("App"), new ImageView(getClass().getResource("images/nodeicons/panel.png").toString()));
            final TreeItem<NodeInfo> subWindows = new TreeItem<NodeInfo>(new DummyNodeInfo("SubWindows"), new ImageView(getClass().getResource("images/nodeicons/panel.png").toString()));
            for (int i = 0; i < popupWindows.size(); i++) {
                final PopupWindow window = popupWindows.get(i);
                final TreeItem<NodeInfo> subWindow = new TreeItem<NodeInfo>(new DummyNodeInfo("SubWindow"), new ImageView(getClass().getResource("images/nodeicons/panel.png").toString()));
                subWindow.getChildren().add(createTreeItem(window.getScene().getRoot(), true));
                subWindows.getChildren().add(subWindow);
            }
            app.getChildren().addAll(root, subWindows);
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

        // Find parent we can use to hang bounds rectangles
        if (overlayParent != null) {
            removeFromNode(overlayParent, boundsInParentRect);
            removeFromNode(overlayParent, layoutBoundsRect);
            removeFromNode(overlayParent, baselineLine);
        }
        overlayParent = findFertileParent(value);
        if (overlayParent == null) {
            System.out.println("warning: could not find writable parent to add overlay nodes; overlays disabled.");
            setShowBounds(false);
            showBoundsCheckbox.setDisable(true);
            setShowBaseline(false);
            showBaselineCheckbox.setDisable(true);
        } else {
            addToNode(overlayParent, boundsInParentRect);
            addToNode(overlayParent, layoutBoundsRect);
            addToNode(overlayParent, baselineLine);
        }

        setTargetScene(target.getScene());
    }

    private Parent findFertileParent(final Parent p) {
        Parent fertile = (p instanceof Group || p instanceof Pane) ? p : null;
        if (fertile == null) {
            for (final Node child : p.getChildrenUnmodifiable()) {
                if (child instanceof Parent) {
                    fertile = findFertileParent((Parent) child);
                }
            }
        }
        return fertile; // could be null!
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

        for (final NodeFilter filter : activeNodeFilters) {
            if (!filter.accept(node)) {
                nodeAccepted = false;
                ignoreShowFiltered |= filter.ignoreShowFilteredNodesInTree();
                childrenAccepted &= filter.allowChildrenOnRejection();
            }
        }
        /**
         * Incredibly ugly workaround for the cursor in the TextField that
         * changes its visibility constantly
         */
        if (node.getId() == null || !node.getId().startsWith(SCENIC_VIEW_BASE_ID)) {
            node.visibleProperty().removeListener(visibilityInvalidationListener);
            node.visibleProperty().addListener(visibilityInvalidationListener);
        }
        final NodeData nodeData = new NodeData(node);
        final TreeItem<NodeInfo> treeItem = new TreeItem<NodeInfo>(nodeData, new ImageView(nodeData.getIcon()));
        if (nodeData.node == selectedNode) {
            previouslySelectedItem = treeItem;
        }
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
            treeItem.setExpanded(!(node instanceof Control));
        }
        if(updateCount)
            statusBar.updateNodeCount(targetScene);

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

    private void removeTreeItem(final Node node, final boolean removeVisibilityListener, final boolean updateCount) {
        if (getSelectedNode() == node) {
            treeView.getSelectionModel().clearSelection();
            setSelectedNode(null);
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
        if(updateCount) {
            statusBar.updateNodeCount(targetScene);
		}
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(final Node value) {
        if (value != selectedNode) {
            storeSelectedNode(value);
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

    public void setShowBounds(final boolean value) {
        if (value != showBoundsCheckbox.isSelected()) {
            showBoundsCheckbox.setSelected(value);
            updateBoundsRects();
        }
    }

    public boolean isShowBounds() {
        return showBoundsCheckbox.isSelected();
    }

    public void setShowBaseline(final boolean value) {
        if (value != showBaselineCheckbox.isSelected()) {
            showBaselineCheckbox.setSelected(value);
            updateBaseline();
        }
    }

    public boolean isShowBaseline() {
        return showBaselineCheckbox.isSelected();
    }

    private void updateBoundsRects() {
        /**
         * By node layout bounds only on main scene not on popups
         */
        if (showBoundsCheckbox.isSelected() && selectedNode != null && selectedNode.getScene() == targetScene) {
            updateRect(selectedNode, selectedNode.getBoundsInParent(), 0, 0, boundsInParentRect);
            updateRect(selectedNode, selectedNode.getLayoutBounds(), selectedNode.getLayoutX(), selectedNode.getLayoutY(), layoutBoundsRect);
            boundsInParentRect.setVisible(true);
            layoutBoundsRect.setVisible(true);
        } else {
            boundsInParentRect.setVisible(false);
            layoutBoundsRect.setVisible(false);
        }
    }

    private void updateRect(final Node node, final Bounds bounds, final double tx, final double ty, final Rectangle rect) {
        final Parent parent = node.getParent();
        if (parent != null) {
            // need to translate position
            final Point2D pt = overlayParent.sceneToLocal(node.getParent().localToScene(bounds.getMinX(), bounds.getMinY()));
            rect.setX(snapPosition(pt.getX()) + snapPosition(tx));
            rect.setY(snapPosition(pt.getY()) + snapPosition(ty));
            rect.setWidth(snapSize(bounds.getWidth()));
            rect.setHeight(snapSize(bounds.getHeight()));
        } else {
            // selected node is root
            rect.setX(snapPosition(bounds.getMinX()) + snapPosition(tx) + 1);
            rect.setY(snapPosition(bounds.getMinY()) + snapPosition(ty) + 1);
            rect.setWidth(snapSize(bounds.getWidth()) - 2);
            rect.setHeight(snapSize(bounds.getHeight()) - 2);
        }

    }

    private void updateBaseline() {
        if (showBaselineCheckbox.isSelected() && selectedNode != null) {
            final double baseline = selectedNode.getBaselineOffset();
            final Bounds bounds = selectedNode.getLayoutBounds();
            final Point2D pt = overlayParent.sceneToLocal(selectedNode.localToScene(bounds.getMinX(), bounds.getMinY() + baseline));
            baselineLine.setStartX(pt.getX());
            baselineLine.setStartY(pt.getY());
            baselineLine.setEndX(pt.getX() + bounds.getWidth());
            baselineLine.setEndY(pt.getY());
            baselineLine.setVisible(true);
        } else {
            baselineLine.setVisible(false);
        }
    }

    private void setShowDefaultProperties(final boolean show) {
        allDetailsPane.setShowDefaultProperties(show);
    }

    private void setTargetScene(final Scene value) {

        if (targetScene != null) {
            targetScene.widthProperty().removeListener(targetScenePropListener);
            targetScene.heightProperty().removeListener(targetScenePropListener);
        }
        targetScene = value;
        if (targetScene != null) {
            setTargetWindow(targetScene.getWindow());
            targetScene.widthProperty().addListener(targetScenePropListener);
            targetScene.heightProperty().addListener(targetScenePropListener);
            targetScene.setOnMouseMoved(new EventHandler<MouseEvent>() {

                @Override public void handle(final MouseEvent ev) {
                    statusBar.updateMousePosition((int) ev.getSceneX() + "x" + (int) ev.getSceneY());
                }
            });
            autoRefreshStyleSheets.setDisable(!StyleSheetRefresher.canStylesBeRefreshed(targetScene));

            if (refresher == null || refresher.scene != value) {
                if (refresher != null)
                    refresher.finish();
                if (!autoRefreshStyleSheets.isDisable() && autoRefreshStyleSheets.isSelected())
                    startRefresher();
            }
        }
        updateSceneDetails();
    }

    private void setTargetWindow(final Window value) {
        if (targetWindow != null) {
            targetWindow.xProperty().removeListener(targetWindowPropListener);
            targetWindow.yProperty().removeListener(targetWindowPropListener);
            targetWindow.widthProperty().removeListener(targetWindowPropListener);
            targetWindow.heightProperty().removeListener(targetWindowPropListener);
            targetWindow.focusedProperty().removeListener(targetWindowPropListener);
        }
        targetWindow = value;
        if (targetWindow != null) {
            targetWindow.xProperty().addListener(targetWindowPropListener);
            targetWindow.yProperty().addListener(targetWindowPropListener);
            targetWindow.widthProperty().addListener(targetWindowPropListener);
            targetWindow.heightProperty().addListener(targetWindowPropListener);
            targetWindow.focusedProperty().addListener(targetWindowPropListener);
        }
        statusBar.updateWindowDetails(targetWindow);

    }

    private void updateSceneDetails() {
        statusBar.updateSceneDetails(targetScene);
        // hack, since we can't listen for a STAGE prop change on scene
        if (targetScene != null && targetWindow == null) {
            setTargetWindow(targetScene.getWindow());
        }
    }

    private CheckMenuItem buildCheckMenuItem(final String text, final String toolTipSelected, final String toolTipNotSelected, final String property, final Boolean value) {
        final CheckMenuItem menuItem = new CheckMenuItem(text);
        if (property != null)
            loadProperty(property, menuItem, value);
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
                storeTarget(target);
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
        // filterField.setOnAction(new EventHandler<ActionEvent>() {
        // @Override public void handle(final ActionEvent ev) {
        // storeTarget(target);
        // }
        // });
        return filterField;
    }

    private void addToNode(final Parent parent, final Node node) {
        if (parent instanceof Group) {
            ((Group) parent).getChildren().add(node);
        } else if (parent instanceof ScenicView) {
            ((ScenicView) parent).getChildren().add(node);
        } else { // instanceof Pane
            ((Pane) parent).getChildren().add(node);
        }
    }

    private void removeFromNode(final Parent parent, final Node node) {
        if (parent instanceof Group) {
            ((Group) parent).getChildren().remove(node);
        } else if (parent instanceof ScenicView) {
            ((ScenicView) parent).getChildren().remove(node);
        } else { // instanceof Pane
            ((Pane) parent).getChildren().remove(node);
        }
    }

    private void findDeepSelection(final double x, final double y) {
        final TreeItem<NodeInfo> nodeData = getHoveredNode(x, y);
        if (nodeData != null) {
            treeView.getSelectionModel().select(nodeData);
        }
    }

    private void highlightHovered(final double x, final double y) {
        final TreeItem<NodeInfo> nodeData = getHoveredNode(x, y);
        if (previousHightLightedData != nodeData) {
            previousHightLightedData = null;
            if (componentHighLighter != null) {
                removeFromNode(target, componentHighLighter);
            }
            if (nodeData != null && nodeData.getValue().getNode() != null) {
                final Bounds bounds = nodeData.getValue().getNode().getBoundsInParent();
                final Point2D start = nodeData.getValue().getNode().localToScene(new Point2D(0, 0));
                final Rectangle rect = new Rectangle();
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.ORANGE);
                rect.setMouseTransparent(true);
                rect.setLayoutX(start.getX());
                rect.setLayoutY(start.getY());
                rect.setStrokeWidth(3);
                rect.setWidth(bounds.getMaxX() - bounds.getMinX());
                rect.setHeight(bounds.getMaxY() - bounds.getMinY());
                rect.setId(SCENIC_VIEW_BASE_ID + "componentHighLighter");
                rect.setManaged(false);
                componentHighLighter = rect;
                addToNode(target, componentHighLighter);
            }
        }
    }

    private TreeItem<NodeInfo> getHoveredNode(final double x, final double y) {
        final List<TreeItem<NodeInfo>> infos = treeViewData;
        for (int i = infos.size() - 1; i >= 0; i--) {
            final NodeInfo info = infos.get(i).getValue();
            final Point2D localPoint = info.getNode().sceneToLocal(x, y);
            if (info.getNode().contains(localPoint)) {
                /**
                 * Mouse Transparent nodes can be ignored
                 */
                final boolean selectable = !ignoreMouseTransparentNodes.isSelected() || !info.isMouseTransparent();
                if (selectable) {
                    return infos.get(i);
                }
            }
        }
        return null;
    }

    private void loadProperty(final String propertyName, final Object component, final Object defaultValue) {
        if (component instanceof CheckMenuItem) {
            ((CheckMenuItem) component).setSelected(Boolean.parseBoolean(properties.getProperty(propertyName, defaultValue.toString())));
        }
        persistentComponents.put(propertyName, component);
    }

    private void loadProperties() {
        properties = new Properties();
        try {
            final File propertiesFile = new File(SCENIC_VIEW_PROPERTIES_FILE);
            if (propertiesFile.exists()) {
                final FileInputStream in = new FileInputStream(propertiesFile);
                try {
                    properties.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (final Exception e) {
            System.out.println("Error while loading preferences");
        }
    }

    public void close() {
        removeScenicViewComponents(target);
        if(targetScene != null) {
            targetScene.removeEventHandler(MouseEvent.MOUSE_MOVED, sceneHoverListener);
        }
        if (refresher != null)
            refresher.finish();
        if (windowChecker != null)
            windowChecker.finish();
        saveProperties();
    }

    private void removeScenicViewComponents(final Node target) {
        /**
         * We should any component associated with ScenicView on close
         */
        if (target instanceof Parent) {
            if (target instanceof Group) {
                final List<Node> nodes = ((Group) target).getChildren();
                for (final Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                    final Node node = iterator.next();
                    if (node.getId() != null && node.getId().startsWith(SCENIC_VIEW_BASE_ID)) {
                        iterator.remove();
                    }
                }
            }
            if (target instanceof Pane) {
                final List<Node> nodes = ((Pane) target).getChildren();
                for (final Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                    final Node node = iterator.next();
                    if (node.getId() != null && node.getId().startsWith(SCENIC_VIEW_BASE_ID)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void saveProperties() {
        final Properties properties = new Properties();
        for (final Iterator<String> iterator = persistentComponents.keySet().iterator(); iterator.hasNext();) {
            final String propertyName = iterator.next();
            final Object component = persistentComponents.get(propertyName);
            if (component instanceof CheckMenuItem) {
                properties.put(propertyName, Boolean.toString(((CheckMenuItem) component).isSelected()));
            }
        }
        properties.put("ignoreMouseTransparentNodes", Boolean.toString(ignoreMouseTransparentNodes.isSelected()));
        try {
            final File propertiesFile = new File(SCENIC_VIEW_PROPERTIES_FILE);
            final FileOutputStream out = new FileOutputStream(propertiesFile);
            try {
                properties.store(out, "ScenicView properties");
            } finally {
                out.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("Error while saving preferences");
        }
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

    private void startRefresher() {
        refresher = new StyleSheetRefresher(targetScene);
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

    // Need this to prevent the tree from using the node as the display!
    class NodeData implements NodeInfo {

        

        public Node node;
        /**
         * Flag to indicate that this node is not valid for the filters but it's
         * kept only to indicate the structure of lower accepted nodes + Node
         * (Invalid) + Node (Invalid) + Node (Valid for filter)
         */
        public boolean invalidForFilter;
        ObservableList<Node> children;

        public NodeData(final Node node) {
            this.node = node;
        }

        @Override public String toString() {
            return nodeClass(node) + ((showNodesIdInTree.isSelected() && node.getId() != null) ? " \"" + node.getId() + "\"" : "");
        }

        @Override public boolean isVisible() {
            return isNodeVisible(node);
        }

        private boolean isNodeVisible(final Node node) {
            return DisplayUtils.isNodeVisible(node);
        }

        @Override public boolean isMouseTransparent() {
            return isMouseTransparent(node);
        }

        private boolean isMouseTransparent(final Node node) {
            if (node == null) {
                return false;
            } else {
                return node.isMouseTransparent() || isMouseTransparent(node.getParent());
            }
        }

        private Image getIcon() {
            final URL resource = getClass().getResource("images/nodeicons/" + nodeClass(node) + ".png");
            String url;
            if (resource != null) {
                url = resource.toString();
            } else {
                url = CUSTOM_NODE_IMAGE;
            }
            Image image = loadedImages.get(url);
            if (image == null) {
                image = new Image(url);
                loadedImages.put(url, image);
            }
            return image;
        }

        @Override public Node getNode() {
            return node;
        }

        @Override public boolean isInvalidForFilter() {
            return invalidForFilter;
        }
    }

    interface NodeFilter {
        /**
         * Checks if the node is accepted for this filter
         * 
         * @param node
         * @return
         */
        public boolean accept(Node node);

        /**
         * Checks if the children could be accepted even though this node is
         * rejected
         * 
         * @return
         */
        public boolean allowChildrenOnRejection();

        /**
         * Flag to hide always nodes
         * 
         * @return
         */
        public boolean ignoreShowFilteredNodesInTree();

    }

    private static class CustomTreeCell extends TreeCell<NodeInfo> {
        @Override public void updateItem(final NodeInfo item, final boolean empty) {
            super.updateItem(item, empty);
            
            if (getTreeItem() != null) {
                setGraphic(getTreeItem().getGraphic());
            }

            if (item != null) {
                setText(item.toString());
                setOpacity(1);
            }

            if (item != null && (!item.isVisible() || item.isInvalidForFilter())) {
                setOpacity(0.3);
            }

            if (item != null && item.getNode() != null && item.getNode().isFocused()) {
                setTextFill(Color.RED);
            }
        }
    }

    public final BorderPane getBorderPane() {
        return borderPane;
    }

    public final VBox getLeftPane() {
        return leftPane;
    }

    @SuppressWarnings("rawtypes")
    class SubWindowChecker extends Thread {

        boolean finish = false;
        Map<PopupWindow, Map> previousTree = new HashMap<PopupWindow, Map>();
        List<PopupWindow> windows = new ArrayList<PopupWindow>();

        @Override public void run() {
            final List<PopupWindow> tempPopups = new ArrayList<PopupWindow>();
            final Map<PopupWindow, Map> tree = new HashMap<PopupWindow, Map>();
            while (!finish) {
                try {
                    Thread.sleep(1000);
                } catch (final Exception e) {
                    // TODO: handle exception
                }
                tempPopups.clear();
                tree.clear();
                windows.clear();
                @SuppressWarnings("deprecation") final Iterator<Window> it = Window.impl_getWindows();
                while (it.hasNext()) {
                    final Window window = it.next();
                    if (window instanceof PopupWindow) {
                        tempPopups.add((PopupWindow) window);
                    }

                }
                for (final PopupWindow popupWindow : tempPopups) {
                    final Map<PopupWindow, Map> pos = valid(popupWindow, tree);
                    if (pos != null) {
                        pos.put(popupWindow, new HashMap<PopupWindow, Map>());
                        windows.add(popupWindow);
                    }
                }
                if (!tree.equals(previousTree)) {
                    previousTree.clear();
                    previousTree.putAll(tree);
                    final List<PopupWindow> actualWindows = new ArrayList<PopupWindow>(windows);
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            // No need for synchronization here
                            ScenicView.this.popupWindows.clear();
                            ScenicView.this.popupWindows.addAll(actualWindows);
                            ScenicView.this.storeTarget(target);
                        }
                    });

                }
            }
        }

        public void finish() {
            this.finish = true;
        }

        @SuppressWarnings("unchecked") Map<PopupWindow, Map> valid(final PopupWindow window, final Map<PopupWindow, Map> tree) {
            if (window.getOwnerWindow() == targetWindow)
                return tree;
            for (final Iterator<PopupWindow> iterator = tree.keySet().iterator(); iterator.hasNext();) {
                final PopupWindow type = iterator.next();
                if (type == window.getOwnerWindow()) {
                    return tree.get(type);
                } else {
                    final Map<PopupWindow, Map> lower = valid(window, tree.get(type));
                    if (lower != null)
                        return lower;
                }
            }
            return null;
        }
    }

    /**
     * For autoTesting purposes
     */
    @Override public ObservableList<Node> getChildren() {
        return super.getChildren();
    }
}
