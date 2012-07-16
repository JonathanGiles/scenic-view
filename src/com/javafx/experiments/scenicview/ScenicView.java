/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.value.*;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.embed.swing.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.*;
import javafx.util.Duration;

import com.javafx.experiments.scenicview.ScenegraphTreeView.SelectedNodeContainer;
import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.Detail;
import com.javafx.experiments.scenicview.connector.event.*;
import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.javafx.experiments.scenicview.details.*;
import com.javafx.experiments.scenicview.details.GDetailPane.RemotePropertySetter;
import com.javafx.experiments.scenicview.dialog.*;
import com.javafx.experiments.scenicview.update.*;
import com.javafx.experiments.scenicview.utils.ClassPathDialog;
import com.javafx.experiments.scenicview.utils.PathChangeListener;
import com.javafx.experiments.scenicview.utils.PropertiesUtils;
import com.javafx.experiments.scenicview.utils.ScenicViewBooter;
import java.net.URI;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * 
 * @author aim
 */
public class ScenicView extends Region implements SelectedNodeContainer, CParent {

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
    private final AnimationsPane animationsPane;
    private static StatusBar statusBar;
    private final VBox leftPane;

    TextField propertyFilterField;

    /**
     * Menu Options
     */
    protected final MenuBar menuBar;
    private final CheckMenuItem showFilteredNodesInTree;
    private final CheckMenuItem showNodesIdInTree;
    private final CheckMenuItem autoRefreshStyleSheets;
    private final CheckMenuItem componentSelectOnClick;

    private final Configuration configuration = new Configuration();

    private UpdateStrategy updateStrategy;

    private final AppEventDispatcher stageModelListener = new AppEventDispatcher() {

        private boolean isActive(final StageID stageID) {
            return activeStage.getID().equals(stageID);
        }

        @Override public void dispatchEvent(final AppEvent appEvent) {
            System.out.println("Event" + appEvent);
            if (isValid(appEvent)) {
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
                    if (isValid(appEvent))
                        treeView.updateStageModel(getStageController(appEvent.getStageID()), ((NodeAddRemoveEvent) appEvent).getNode(), showNodesIdInTree.isSelected(), showFilteredNodesInTree.isSelected());
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
                    System.out.println("Unused event for type " + appEvent);
                    break;
                }
            } else {
                System.out.println("Unused event " + appEvent);
            }
        }

        private boolean isValid(final AppEvent appEvent) {
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
    private WebView wview;
    private Tab javadocTab;
    private TabPane tabPane;
    private String loadedPage;
    private Tab detailsTab;

    public ScenicView(final UpdateStrategy updateStrategy, final Stage senicViewStage) {
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
                return StageControllerImpl.isNormalNode(node);
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
                
                ClassPathDialog.showDialog(toolsPath, jfxPath, false, new PathChangeListener() {
                    public void onPathChanged(Map<String, URI> map) {
                        URI toolsPath = map.get(PathChangeListener.TOOLS_JAR_KEY);
                        URI jfxPath = map.get(PathChangeListener.JFXRT_JAR_KEY);

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
        fileMenu.getItems().addAll(classpathItem, new SeparatorMenuItem(), exitItem);

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

        final CheckMenuItem showCSSProperties = buildCheckMenuItem("Show CSS Properties", "Show CSS properties", "Hide CSS properties", "showCSSProperties", Boolean.FALSE);
        showCSSProperties.selectedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                configuration.setCSSPropertiesDetail(showCSSProperties.isSelected());
                configurationUpdated();
            }
        });
        configuration.setCSSPropertiesDetail(showCSSProperties.isSelected());

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

        final CheckMenuItem animationsEnabled = buildCheckMenuItem("Animations enabled", "Animations will run on the application", "Animations will be stopped", null, Boolean.TRUE);
        animationsEnabled.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                animationsEnabled(animationsEnabled.isSelected());
            }
        });

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
                configuration.setComponentSelectOnClick(newValue);
                configurationUpdated();
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
        scenegraphMenu.getItems().addAll(automaticScenegraphStructureRefreshing, autoRefreshStyleSheets, new SeparatorMenuItem(), componentSelectOnClick, ignoreMouseTransparentNodes, new SeparatorMenuItem(), animationsEnabled);

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

        allDetailsPane = new AllDetailsPane(new APILoader() {

            @Override public void loadAPI(final String property) {
                ScenicView.this.loadAPI(property);
            }
        });
        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(allDetailsPane);

        treeView = new ScenegraphTreeView(activeNodeFilters, this);

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
        final ImageView b1 = new ImageView(DisplayUtils.CLEAR_IMAGE);
        b1.setOnMousePressed(new EventHandler<Event>() {

            @Override public void handle(final Event arg0) {
                idFilterField.setText("");
                update();
            }
        });
        final ImageView b2 = new ImageView(DisplayUtils.CLEAR_IMAGE);
        b2.setOnMousePressed(new EventHandler<Event>() {

            @Override public void handle(final Event arg0) {
                classNameFilterField.setText("");
                update();
            }
        });
        final ImageView b3 = new ImageView(DisplayUtils.CLEAR_IMAGE);
        b3.setOnMousePressed(new EventHandler<Event>() {

            @Override public void handle(final Event arg0) {
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
        final TitledPane treeViewPane = new TitledPane("JavaFX Apps", treeView);
        treeViewPane.setCollapsible(false);
        treeViewPane.setMaxHeight(Double.MAX_VALUE);
        // This solves the resizing of filtersPane
        treeViewPane.setPrefHeight(50);
        leftPane.getChildren().addAll(filtersPane, treeViewPane);
        VBox.setVgrow(treeViewPane, Priority.ALWAYS);

        animationsPane = new AnimationsPane(this);

        tabPane = new TabPane();
        detailsTab = new Tab("Details");
        detailsTab.setGraphic(new ImageView(DisplayUtils.getUIImage("details.png")));
        detailsTab.setContent(scrollPane);
        detailsTab.setClosable(false);
        javadocTab = new Tab("JavaDoc");
        wview = new WebView();

        final StackPane javadocTabStackPane = new StackPane();
        javadocTabStackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        javadocTab.setContent(javadocTabStackPane);
        final ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(300, 300);
        wview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            Animation anim;

            @Override public void changed(final ObservableValue<? extends State> arg0, final State old, final State newValue) {
                if (newValue == State.READY) {
                    anim = TimelineBuilder.create().keyFrames(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {
                        @Override public void handle(final ActionEvent arg0) {
                            if (wview.getEngine().getLoadWorker().getProgress() == -1) {
                                doLoad(loadedPage);
                            }
                        }
                    })).build();
                    anim.play();
                } else if (anim != null) {
                    anim.stop();
                    anim = null;
                }
            }
        });
        wview.getEngine().getLoadWorker().progressProperty().addListener(new ChangeListener<Number>() {
            private final double DURATION = 1000;
            private final FadeTransition fadeIn = new FadeTransition(Duration.millis(DURATION));
            private final FadeTransition fadeOut = new FadeTransition(Duration.millis(DURATION));
            private final ParallelTransition fader = new ParallelTransition(fadeIn, fadeOut);

            {
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
            }

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number progress) {
                final double progressValue = progress.doubleValue();

                if (progressValue == 0) {
                    javadocTabStackPane.getChildren().setAll(progressIndicator);
                    doFade(wview, progressIndicator);
                } else if (progressValue == 1.0) {
                    javadocTabStackPane.getChildren().setAll(wview);
                    doFade(progressIndicator, wview);
                }
                progressIndicator.setProgress(progressValue);
            }

            private void doFade(final Node n1, final Node n2) {
                fader.stop();
                fadeOut.setNode(n1);
                fadeIn.setNode(n2);
                fader.play();
            }
        });

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
        final Tab eventsTab = new Tab("Events");
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
        Persistence.loadProperty("stageWidth", senicViewStage, 640);
        Persistence.loadProperty("stageHeight", senicViewStage, 800);
        setUpdateStrategy(updateStrategy);
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

    public void loadAPI(final String property) {
        if (property != null) {
            tabPane.getSelectionModel().select(javadocTab);
        }
        if (javadocTab.isSelected()) {
            if (selectedNode == null || selectedNode.getNodeClassName() == null || !selectedNode.getNodeClassName().startsWith("javafx.")) {
                doLoad("http://docs.oracle.com/javafx/2/api/overview-summary.html");
            } else {
                String baseClass = selectedNode.getNodeClassName();
                if (property != null) {
                    baseClass = findProperty(baseClass, property);
                }
                final String page = "http://docs.oracle.com/javafx/2/api/" + baseClass.replace('.', '/') + ".html" + (property != null ? ("#" + property + "Property") : "");
                if (!wview.getEngine().getLocation().equals(page)) {
                    doLoad(page);
                }
            }
        }
    }

    private void doLoad(final String page) {
        loadedPage = page;
        wview.getEngine().load(page);
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

    private TextField createFilterField(final String prompt) {
        return createFilterField(prompt, new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                update();
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
            final StageControllerImpl sController = new StageControllerImpl(target, aController);

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
                System.out.println(operation + ":" + id);
                for (int i = 0; i < apps.size(); i++) {
                    System.out.println("App:" + apps.get(i).getID());
                    final List<StageController> scs = apps.get(i).getStages();
                    for (int j = 0; j < scs.size(); j++) {
                        System.out.println("\tStage:" + scs.get(j).getID().getStageID());
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
                            if (apps.isEmpty()) {
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

    void openStage(final StageController controller) {
        controller.setEventDispatcher(stageModelListener);
        controller.configurationUpdated(configuration);
    }
}
