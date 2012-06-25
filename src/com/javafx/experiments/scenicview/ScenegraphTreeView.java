package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import com.javafx.experiments.scenicview.connector.*;

public class ScenegraphTreeView extends TreeView<SVNode> {

    private TreeItem<SVNode> previouslySelectedItem;
    private final List<TreeItem<SVNode>> treeViewData = new ArrayList<TreeItem<SVNode>>();
    private final List<NodeFilter> activeNodeFilters;
    private final SelectedNodeContainer container;

    public ScenegraphTreeView(final List<NodeFilter> activeNodeFilters, final SelectedNodeContainer container) {
        this.activeNodeFilters = activeNodeFilters;
        this.container = container;
        setId("main-treeview");
        setPrefSize(200, 500);
        setCellFactory(new Callback<TreeView<SVNode>, TreeCell<SVNode>>() {
            @Override public TreeCell<SVNode> call(final TreeView<SVNode> node) {
                return new CustomTreeCell();
            }
        });
        setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (ev.isSecondaryButtonDown()) {
                    getSelectionModel().clearSelection();
                }
            }
        });

    }

    void nodeSelected(final SVNode nodeData) {
        if (nodeData != null) {
            for (int i = 0; i < treeViewData.size(); i++) {
                final TreeItem<SVNode> item = treeViewData.get(i);
                if (item.getValue().equals(nodeData)) {
                    getSelectionModel().select(item);
                    scrollTo(getSelectionModel().getSelectedIndex());
                    break;
                }
            }
        }
    }

    void updateStageModel(final SVNode value, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {

        final SVNode previouslySelected = container.getSelectedNode();
        treeViewData.clear();
        previouslySelectedItem = null;
        final TreeItem<SVNode> root = createTreeItem(value, showNodesIdInTree, showFilteredNodesInTree);

        setRoot(root);
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
            treeViewData.remove(treeItem);
            if (selected != null) {
                // Ugly workaround
                getSelectionModel().select(selected);
            }
        }
    }

    void addNewNode(final SVNode alive, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
        if (alive.getId() == null || !alive.getId().startsWith(StageController.SCENIC_VIEW_BASE_ID)) {
            final TreeItem<SVNode> selected = getSelectionModel().getSelectedItem();
            final TreeItem<SVNode> TreeItem = createTreeItem(alive, showNodesIdInTree, showFilteredNodesInTree);
            // childItems[x] could be null because of bounds
            // rectangles or filtered nodes
            if (TreeItem != null) {
                final SVNode parent = alive.getParent();
                final TreeItem<SVNode> parentTreeItem = getTreeItem(parent);

                /**
                 * In some situations node could be previously added
                 */
                final List<TreeItem<SVNode>> actualNodes = parentTreeItem.getChildren();
                boolean found = false;
                for (final TreeItem<SVNode> node : actualNodes) {
                    if (node.getValue().equals(alive)) {
                        found = true;
                    }

                }
                if (!found) {
                    parentTreeItem.getChildren().add(TreeItem);
                }
            }
            if (selected != null) {
                // Ugly workaround
                getSelectionModel().select(selected);
            }
        }
    }

    private TreeItem<SVNode> getTreeItem(final SVNode node) {
        for (int i = 0; i < treeViewData.size(); i++) {
            if (treeViewData.get(i).getValue().equals(node)) {
                return treeViewData.get(i);
            }
        }
        return null;
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
        treeViewData.add(treeItem);

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
                treeViewData.remove(treeItem);
                return null;
            } else {
                /**
                 * Mark the node as invalidForFilter
                 */
                node.setInvalidForFilter(true);
                return treeItem;
            }
        } else {
            treeViewData.remove(treeItem);
            return null;
        }
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

}
