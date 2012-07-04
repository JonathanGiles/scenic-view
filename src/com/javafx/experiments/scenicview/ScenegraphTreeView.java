package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import com.javafx.experiments.scenicview.connector.StageController;
import com.javafx.experiments.scenicview.connector.node.*;

public class ScenegraphTreeView extends TreeView<SVNode> {

    private TreeItem<SVNode> previouslySelectedItem;
    private final Map<SVNode, TreeItem<SVNode>> treeViewData = new HashMap<SVNode, TreeItem<SVNode>>();
    private final List<NodeFilter> activeNodeFilters;
    private final SelectedNodeContainer container;

    TreeItem<SVNode> apps;

    private boolean secPressed;

    public ScenegraphTreeView(final List<NodeFilter> activeNodeFilters, final SelectedNodeContainer container) {
        this.activeNodeFilters = activeNodeFilters;
        this.container = container;
        setId("main-treeview");
        setShowRoot(false);
        setPrefSize(200, 500);
        setCellFactory(new Callback<TreeView<SVNode>, TreeCell<SVNode>>() {
            @Override public TreeCell<SVNode> call(final TreeView<SVNode> node) {
                return new CustomTreeCell();
            }
        });
        setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (ev.isSecondaryButtonDown()) {
                    secPressed = true;
                    getSelectionModel().clearSelection();
                }
            }
        });
        /**
         * Ugly patch for a problem that causes a reselection of the TreeItem on
         * mouseRelease even if the selection has been cleared
         */
        setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (secPressed) {
                    secPressed = false;
                    getSelectionModel().clearSelection();
                }
            }
        });

    }

    void nodeSelected(final SVNode nodeData) {
        if (nodeData != null && treeViewData.containsKey(nodeData)) {
            final TreeItem<SVNode> item = treeViewData.get(nodeData);
            getSelectionModel().select(item);
            scrollTo(getSelectionModel().getSelectedIndex());
        }
    }

    TreeItem<SVNode> findStageRoot(final StageController controller, final TreeItem<SVNode> stageRoot) {
        if (apps == null) {
            final SVDummyNode dummy = new SVDummyNode("Apps", "Java", 0);
            apps = new TreeItem<SVNode>(dummy, new ImageView(DisplayUtils.getIcon(dummy)));
            apps.setExpanded(true);

        }
        TreeItem<SVNode> app = null;
        final List<TreeItem<SVNode>> apps = this.apps.getChildren();
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getValue().getNodeId() == controller.getAppController().getID()) {
                app = apps.get(i);
                break;
            }
        }
        if (app == null) {
            final SVDummyNode dummy = new SVDummyNode("VM - " + controller.getAppController(), "Java", controller.getAppController().getID());
            app = new TreeItem<SVNode>(dummy, new ImageView(DisplayUtils.getIcon(dummy)));
            app.setExpanded(true);
            this.apps.getChildren().add(app);
        }
        if (apps.size() == 1) {
            if (app.getChildren().size() == 0 || (app.getChildren().size() == 1 && app.getChildren().get(0).getValue().equals(stageRoot.getValue()))) {
                setRoot(stageRoot);
            } else
                setRoot(app);
        } else {
            setRoot(this.apps);
        }
        return app;
    }

    void clearAllApps() {
        if (getRoot() != null) {
            getRoot().getChildren().clear();
        }
    }

    void updateStageModel(final StageController controller, final SVNode value, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
        final SVNode previouslySelected = container.getSelectedNode();
        // treeViewData.clear();
        previouslySelectedItem = null;
        removeForNode(getTreeItem(value));
        final TreeItem<SVNode> root = createTreeItem(value, showNodesIdInTree, showFilteredNodesInTree);

        final TreeItem<SVNode> applicationRoot = findStageRoot(controller, root);

        /**
         * Check if the application was already present
         */
        boolean added = false;
        final List<TreeItem<SVNode>> apps = applicationRoot.getChildren();
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getValue().getNodeId() == controller.getID().getStageID()) {
                apps.remove(i);
                apps.add(i, root);
                added = true;
                break;
            }
        }
        if (!added) {
            applicationRoot.getChildren().add(root);
        }
        if (previouslySelectedItem != null) {
            /**
             * TODO Why this is not working??
             */
            // treeView.getSelectionModel().clearSelection();
            // treeView.getSelectionModel().select(previouslySelectedItem);
            /**
             * TODO Remove
             */
            container.setSelectedNode(previouslySelected);

        }
    }

    void removeNode(final SVNode node) {
        if (node.getId() == null || !node.getId().startsWith(StageController.SCENIC_VIEW_BASE_ID)) {
            TreeItem<SVNode> selected = null;
            if (container.getSelectedNode() == node) {
                getSelectionModel().clearSelection();
                container.setSelectedNode(null);
            } else {
                // Ugly workaround
                selected = getSelectionModel().getSelectedItem();
            }
            final TreeItem<SVNode> treeItem = getTreeItem(node);
            /**
             * TODO Analyze this problem:
             * 
             * In some situations a parent node could be removed by visibility
             * and after that a children could also change its visibility to
             * false triggering a removal that actually does not exist. In those
             * situations we should keep visibility listener on the parent and
             * remove it to its childrens. Anyway this need to be tested deeply
             * and this protection is not so dangerous
             */
            if (treeItem == null) {
                System.out.println("Removing unfound treeItem:" + node.getExtendedId());
                return;
            }
            final List<TreeItem<SVNode>> treeItemChildren = treeItem.getChildren();
            if (treeItemChildren != null) {
                /**
                 * Do not use directly the list as it will suffer concurrent
                 * modifications
                 */
                @SuppressWarnings("unchecked") final TreeItem<SVNode> children[] = treeItemChildren.toArray(new TreeItem[treeItemChildren.size()]);
                for (int i = 0; i < children.length; i++) {
                    removeNode(children[i].getValue());
                }
            }

            // This does not seem to delete the TreeItem from the tree -- only
            // moves
            // it up a level visually
            /**
             * I don't know why this protection is needed
             */
            if (treeItem.getParent() != null) {
                treeItem.getParent().getChildren().remove(treeItem);
            }
            treeViewData.remove(treeItem.getValue());
            if (selected != null) {
                // Ugly workaround
                getSelectionModel().select(selected);
            }
        }
    }

    void addNewNode(final SVNode alive, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
        if (alive.getId() == null || !alive.getId().startsWith(StageController.SCENIC_VIEW_BASE_ID)) {
            final TreeItem<SVNode> selected = getSelectionModel().getSelectedItem();
            final TreeItem<SVNode> treeItem = createTreeItem(alive, showNodesIdInTree, showFilteredNodesInTree);
            // childItems[x] could be null because of bounds
            // rectangles or filtered nodes
            if (treeItem != null) {
                final SVNode parent = alive.getParent();
                final TreeItem<SVNode> parentTreeItem = getTreeItem(parent);
                /**
                 * In some situations node could be previously added
                 */
                final boolean found = findInTree(parentTreeItem, alive);

                if (!found) {
                    /**
                     * We try to insert the treeItem in the real position of the
                     * parent
                     */
                    boolean posFound = false;
                    int previousPos = -1;
                    final List<SVNode> childrens = parent.getChildren();
                    final int pos = childrens.indexOf(alive);
                    final List<TreeItem<SVNode>> items = parentTreeItem.getChildren();
                    for (int i = 0; i < items.size(); i++) {
                        final TreeItem<SVNode> node = items.get(i);
                        final int actualPos = childrens.indexOf(node.getValue());
                        if (previousPos > actualPos) {
                            System.out.println("This should never happen :" + parent.getExtendedId() + " node:" + node.getValue().getExtendedId());
                        }
                        if (pos > previousPos && pos < actualPos) {
                            parentTreeItem.getChildren().add(i, treeItem);
                            posFound = true;
                            break;
                        }
                        previousPos = actualPos;
                    }
                    if (!posFound) {
                        parentTreeItem.getChildren().add(treeItem);
                    }
                }
            }
            if (selected != null) {
                // Ugly workaround
                getSelectionModel().select(selected);
            }
        }
    }

    private TreeItem<SVNode> getTreeItem(final SVNode node) {
        final TreeItem<SVNode> item = treeViewData.get(node);
        if (item == null) {
            for (final Iterator<SVNode> iterator = treeViewData.keySet().iterator(); iterator.hasNext();) {
                final SVNode type = iterator.next();
                if (type.equals(node)) {
                    System.out.println("Error on hashmap:" + node.getExtendedId() + " and type:" + type.getExtendedId() + " are equals but:" + treeViewData.containsKey(node));
                }
            }
        }
        return item;
    }

    private TreeItem<SVNode> createTreeItem(final SVNode node, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
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
        node.setShowId(showNodesIdInTree);
        final TreeItem<SVNode> treeItem = new TreeItem<SVNode>(node, new ImageView(DisplayUtils.getIcon(node)));
        if (node.equals(container.getSelectedNode())) {
            previouslySelectedItem = treeItem;
        }
        /**
         * TODO Improve this calculation THIS IS NOT CORRECT AS NEW NODES ARE
         * INCLUDED ON TOP a) updateCount=true: We are adding all the nodes b)
         * updateCount=false: We are adding only one node, find its position
         */
        treeViewData.put(node, treeItem);

        final List<TreeItem<SVNode>> childItems = new ArrayList<TreeItem<SVNode>>();
        for (final SVNode child : node.getChildren()) {
            childItems.add(createTreeItem(child, showNodesIdInTree, showFilteredNodesInTree));
        }
        for (final TreeItem<SVNode> childItem : childItems) {
            // childItems[x] could be null because of bounds rectangles or
            // filtered nodes
            if (childItem != null) {
                treeItem.getChildren().add(childItem);
            }
        }

        treeItem.setExpanded(expand || node.isExpanded());

        if (nodeAccepted) {
            return treeItem;
        } else if (!nodeAccepted && !ignoreShowFiltered && showFilteredNodesInTree) {
            /**
             * Mark the node as invalidForFilter
             */
            node.setInvalidForFilter(true);
            return treeItem;
        } else if (!nodeAccepted && childrenAccepted) {
            /**
             * Only return if the node has children
             */
            if (treeItem.getChildren().isEmpty()) {
                return null;
            } else {
                /**
                 * Mark the node as invalidForFilter
                 */
                node.setInvalidForFilter(true);
                return treeItem;
            }
        } else {
            return null;
        }
    }

    private boolean findInTree(final TreeItem<SVNode> parentTreeItem, final SVNode alive) {
        final List<TreeItem<SVNode>> actualNodes = parentTreeItem.getChildren();
        boolean found = false;
        for (final TreeItem<SVNode> node : actualNodes) {
            if (node.getValue().equals(alive)) {
                found = true;
            }
        }
        return found;
    }

    /**
     * TODO Remove this
     * 
     * @author Ander
     * 
     */
    interface SelectedNodeContainer {
        void setSelectedNode(SVNode node);

        SVNode getSelectedNode();
    }

    private void removeForNode(final TreeItem<SVNode> treeItem) {
        if (treeItem != null) {
            final List<TreeItem<SVNode>> treeItemChildren = treeItem.getChildren();
            if (treeItemChildren != null) {
                /**
                 * Do not use directly the list as it will suffer concurrent
                 * modifications
                 */
                @SuppressWarnings("unchecked") final TreeItem<SVNode> children[] = treeItemChildren.toArray(new TreeItem[treeItemChildren.size()]);
                for (int i = 0; i < children.length; i++) {
                    removeForNode(children[i]);
                }
            }

            // This does not seem to delete the TreeItem from the tree -- only
            // moves
            // it up a level visually
            /**
             * I don't know why this protection is needed
             */
            if (treeItem.getParent() != null) {
                treeItem.getParent().getChildren().remove(treeItem);
            }
            treeViewData.remove(treeItem.getValue());
        }

    }

}
