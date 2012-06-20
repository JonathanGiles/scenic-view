package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.beans.*;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.helper.StyleSheetRefresher;

public class StageModel {

    private StyleSheetRefresher refresher;
    


    private Parent overlayParent;
    Parent target;
    Scene targetScene;
    public Window targetWindow;
    /**
     * Simplification for now, only a plain structure for now
     */
    final List<PopupWindow> popupWindows = new ArrayList<PopupWindow>();
    
    private final Rectangle boundsInParentRect;
    private final Rectangle layoutBoundsRect;
    private final Line baselineLine;
    private Rectangle componentSelector;
    private Node componentHighLighter;
    RuleGrid grid;
    
    private Model2GUI model2gui;
    
    private final SubWindowChecker windowChecker;
    
    private final InvalidationListener targetScenePropListener;
    private final InvalidationListener targetWindowPropListener;
    private final InvalidationListener targetWindowSceneListener;
    
    private final EventHandler<? super MouseEvent> sceneHoverListener = new EventHandler<MouseEvent>() {

        @Override public void handle(final MouseEvent ev) {
            highlightHovered(ev.getX(), ev.getY());
        }

    };
    
    private TreeItem<NodeInfo> previousHightLightedData;
    
    public StageModel(final Stage stage) {
        this(stage.getScene().getRoot());
    }

    public StageModel(final Parent target) {
        targetScenePropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable value) {
                updateSceneDetails();
            }
        };

        targetWindowPropListener = new InvalidationListener() {
            @Override public void invalidated(final Observable value) {
                model2gui.updateWindowDetails(StageModel.this, targetWindow);
            }
        };
        targetWindowSceneListener = new InvalidationListener() {
            
            @Override public void invalidated(final Observable arg0) {
                if(targetScene.getRoot()==StageModel.this.target) {
                    setTarget(targetWindow.getScene().getRoot());
                    update();
                }
            }
        };
        windowChecker = new SubWindowChecker(this);
        windowChecker.start();
        boundsInParentRect = new Rectangle();
        boundsInParentRect.setId(ScenicView.SCENIC_VIEW_BASE_ID + "boundsInParentRect");
        boundsInParentRect.setFill(Color.YELLOW);
        boundsInParentRect.setOpacity(.5);
        boundsInParentRect.setManaged(false);
        boundsInParentRect.setMouseTransparent(true);
        layoutBoundsRect = new Rectangle();
        layoutBoundsRect.setId(ScenicView.SCENIC_VIEW_BASE_ID + "layoutBoundsRect");
        layoutBoundsRect.setFill(null);
        layoutBoundsRect.setStroke(Color.GREEN);
        layoutBoundsRect.setStrokeType(StrokeType.INSIDE);
        layoutBoundsRect.setOpacity(.8);
        layoutBoundsRect.getStrokeDashArray().addAll(3.0, 3.0);
        layoutBoundsRect.setStrokeWidth(1);
        layoutBoundsRect.setManaged(false);
        layoutBoundsRect.setMouseTransparent(true);
        baselineLine = new Line();
        baselineLine.setId(ScenicView.SCENIC_VIEW_BASE_ID + "baselineLine");
        baselineLine.setStroke(Color.RED);
        baselineLine.setOpacity(.75);
        baselineLine.setStrokeWidth(1);
        baselineLine.setManaged(false);
        this.target = target;
    }
    
    private void startRefresher() {
        refresher = new StyleSheetRefresher(targetScene);
    }

    public void styleRefresher(final Boolean newValue) {
        if (newValue) {
            startRefresher();
        } else {
            refresher.finish();
        }
    }
    
    void updateBoundsRects(final Node selectedNode) {
        /**
         * By node layout bounds only on main scene not on popups
         */
        if (selectedNode != null && selectedNode.getScene() == targetScene) {
            updateRect(selectedNode, selectedNode.getBoundsInParent(), 0, 0, boundsInParentRect);
            updateRect(selectedNode, selectedNode.getLayoutBounds(), selectedNode.getLayoutX(), selectedNode.getLayoutY(), layoutBoundsRect);
            boundsInParentRect.setVisible(true);
            layoutBoundsRect.setVisible(true);
        } else {
            boundsInParentRect.setVisible(false);
            layoutBoundsRect.setVisible(false);
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
                    if (node.getId() != null && node.getId().startsWith(ScenicView.SCENIC_VIEW_BASE_ID)) {
                        iterator.remove();
                    }
                }
            }
            if (target instanceof Pane) {
                final List<Node> nodes = ((Pane) target).getChildren();
                for (final Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                    final Node node = iterator.next();
                    if (node.getId() != null && node.getId().startsWith(ScenicView.SCENIC_VIEW_BASE_ID)) {
                        iterator.remove();
                    }
                }
            }
        }
    }


    public void update() {
        model2gui.updateStageModel(this);
    }

    public void updateSceneDetails() {
        // hack, since we can't listen for a STAGE prop change on scene
        model2gui.updateSceneDetails(this, targetScene);
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
        }
        model2gui.updateWindowDetails(this, targetWindow);

    }
    
    void setTarget(final Parent value) {
     // Find parent we can use to hang bounds rectangles
        this.target = value;
        if (overlayParent != null) {
            removeFromNode(overlayParent, boundsInParentRect);
            removeFromNode(overlayParent, layoutBoundsRect);
            removeFromNode(overlayParent, baselineLine);
        }
        overlayParent = findFertileParent(value);
        if (overlayParent == null) {
            System.out.println("warning: could not find writable parent to add overlay nodes; overlays disabled.");
            /**
             * This should be improved
             */
            model2gui.overlayParentNotFound(this);
            
        } else {
            addToNode(overlayParent, boundsInParentRect);
            addToNode(overlayParent, layoutBoundsRect);
            addToNode(overlayParent, baselineLine);
        }
        setTargetScene(target.getScene());
    }
    

    private void updateRect(final Node node, final Bounds bounds, final double tx, final double ty, final Rectangle rect) {
        final Bounds b = toSceneBounds(node, bounds, tx, ty);
        rect.setX(b.getMinX());
        rect.setY(b.getMinY());
        rect.setWidth(b.getMaxX()-b.getMinX());
        rect.setHeight(b.getMaxY()-b.getMinY());
    }
    
    private Bounds toSceneBounds(final Node node, final Bounds bounds, final double tx, final double ty) {
        final Parent parent = node.getParent();
        if (parent != null) {
            // need to translate position
            final Point2D pt = overlayParent.sceneToLocal(node.getParent().localToScene(bounds.getMinX(), bounds.getMinY()));
            return new BoundingBox(snapPosition(pt.getX()) + snapPosition(tx), 
                    snapPosition(pt.getY()) + snapPosition(ty)
                    , snapSize(bounds.getWidth()), snapSize(bounds.getHeight()));
        } else {
            // selected node is root
            return new BoundingBox(snapPosition(bounds.getMinX()) + snapPosition(tx) + 1, 
                    snapPosition(bounds.getMinY()) + snapPosition(ty) + 1
                    , snapSize(bounds.getWidth()) - 2, snapSize(bounds.getHeight()) - 2);
        }
    }
    
    private double snapPosition(final double pos) {
        return pos;
    }
    
    private double snapSize(final double pos) {
        return pos;
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

    public void componentSelectOnClick(final boolean newValue) {
        if (newValue) {
            targetScene.addEventHandler(MouseEvent.MOUSE_MOVED, sceneHoverListener);
            final Rectangle rect = new Rectangle();
            rect.setFill(Color.TRANSPARENT);
            rect.setWidth(targetWindow.getWidth());
            rect.setHeight(targetWindow.getHeight());
            rect.setId(ScenicView.SCENIC_VIEW_BASE_ID + "componentSelectorRect");
            rect.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent ev) {
                    
                    model2gui.selectOnClick(StageModel.this, findDeepSelection(ev.getX(), ev.getY()));
                    
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

    public void showGrid(final boolean newValue, final int gap) {
        if (newValue) {
            grid = new RuleGrid(gap, targetScene.getWidth(), targetScene.getHeight());
            grid.setId(ScenicView.SCENIC_VIEW_BASE_ID + "ruler");
            grid.setManaged(false);
            addToNode(target, grid);
        } else {
            if (grid != null)
                removeFromNode(target, grid);
        }
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
    

    void updateBaseline(final boolean show, final Point2D orig, final double width) {
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
        }
        targetScene = value;
        if (targetScene != null) {
            setTargetWindow(targetScene.getWindow());
            targetScene.widthProperty().addListener(targetScenePropListener);
            targetScene.heightProperty().addListener(targetScenePropListener);
            targetScene.setOnMouseMoved(new EventHandler<MouseEvent>() {

                @Override public void handle(final MouseEvent ev) {
                    model2gui.updateMousePosition(StageModel.this, (int) ev.getSceneX() + "x" + (int) ev.getSceneY());
                }
            });
            final boolean canBeRefreshed = StyleSheetRefresher.canStylesBeRefreshed(targetScene);
            
            if (refresher == null || refresher.getScene() != value) {
                if (refresher != null)
                    refresher.finish();
                if (canBeRefreshed && model2gui.isAutoRefreshStyles())
                    startRefresher();
            }
        }
        updateSceneDetails();
    }

    private void highlightHovered(final double x, final double y) {
        final TreeItem<NodeInfo> nodeData = getHoveredNode(x, y);
        if (previousHightLightedData != nodeData) {
            previousHightLightedData = null;
            if (componentHighLighter != null) {
                removeFromNode(target, componentHighLighter);
            }
            if (nodeData != null && nodeData.getValue().getNode() != null) {
                final Node node = nodeData.getValue().getNode();
                componentHighLighter = new ComponentHighLighter(nodeData.getValue(), targetWindow != null ? targetWindow.getWidth() : -1, targetWindow != null ? targetWindow.getHeight() : -1, toSceneBounds(node, node.getBoundsInParent(), 0, 0));
                addToNode(target, componentHighLighter);
            }
        }
        
    }
    

    private TreeItem<NodeInfo> findDeepSelection(final double x, final double y) {
        return getHoveredNode(x, y);
        
    }

    private TreeItem<NodeInfo> getHoveredNode(final double x, final double y) {
        final List<TreeItem<NodeInfo>> infos = model2gui.getTreeItems();
        for (int i = infos.size() - 1; i >= 0; i--) {
            final NodeInfo info = infos.get(i).getValue();
            /**
             * Discard filtered nodes
             */
            if(!info.isInvalidForFilter()) {
                final Point2D localPoint = info.getNode().sceneToLocal(x, y);
                if (info.getNode().contains(localPoint)) {
                    /**
                     * Mouse Transparent nodes can be ignored
                     */
                    final boolean selectable = !model2gui.isIgnoreMouseTransparent() || !info.isMouseTransparent();
                    if (selectable) {
                        return infos.get(i);
                    }
                }
            }
        }
        return null;
    }


    public void setModel2gui(final Model2GUI model2gui) {
        this.model2gui = model2gui;
        setTarget(target);
        update();
    }

    public boolean canStylesheetsBeRefreshed() {
        return StyleSheetRefresher.canStylesBeRefreshed(targetScene);
    }
}
