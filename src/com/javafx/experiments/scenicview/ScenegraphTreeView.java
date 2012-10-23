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

import java.util.*;

import javafx.beans.value.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.node.*;
import com.javafx.experiments.scenicview.connector.node.SVDummyNode.NodeType;

public class ScenegraphTreeView extends TreeView<SVNode> {

    private TreeItem<SVNode> previouslySelectedItem;
    private final Map<SVNode, TreeItem<SVNode>> treeViewData = new HashMap<SVNode, TreeItem<SVNode>>();
    // private final Map<StageController, StageCollapsingListener> listeners =
    // new HashMap<StageController,
    // ScenegraphTreeView.StageCollapsingListener>();
    private final List<NodeFilter> activeNodeFilters;
    private final ConnectorController container;
    private final Map<SVNode, StageController> stages = new HashMap<SVNode, StageController>();

    TreeItem<SVNode> apps;

    private boolean blockSelection;
    private TreeItem<SVNode> patchedNode;

    private final List<Integer> forcedCollapsedItems = new ArrayList<Integer>();
    private final List<Integer> forcedExpandedItems = new ArrayList<Integer>();
    private final List<String> forcedCollapsedNodeClassItems = new ArrayList<String>();
    private final List<String> forcedExpandedNodeClassItems = new ArrayList<String>();
    ContextMenu selectedCM;

    public ScenegraphTreeView(final List<NodeFilter> activeNodeFilters, final ConnectorController container) {
        this.activeNodeFilters = activeNodeFilters;
        this.container = container;
        setId("main-treeview");
        setShowRoot(false);
        setPrefSize(200, 500);
        getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<SVNode>>() {

            @Override public void changed(final ObservableValue<? extends TreeItem<SVNode>> arg0, final TreeItem<SVNode> arg1, final TreeItem<SVNode> newValue) {
                if (!blockSelection) {
                    final TreeItem<SVNode> selected = newValue;
                    setSelectedNode(selected != null && !(selected.getValue() instanceof SVDummyNode) ? selected : null);
                }
            }
        });
        setCellFactory(new Callback<TreeView<SVNode>, TreeCell<SVNode>>() {
            @Override public TreeCell<SVNode> call(final TreeView<SVNode> node) {
                return new CustomTreeCell();
            }
        });
        setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (selectedCM != null) {
                    selectedCM.hide();
                    selectedCM = null;
                }
                if (ev.isSecondaryButtonDown()) {
                    showContextMenu(ev);
                }
                // if (ev.isMiddleButtonDown() &&
                // getSelectionModel().getSelectedItem() != null) {
                // showContextMenu(ev); }
                // if (ev.isSecondaryButtonDown()) {
                // secPressed = true;
                // getSelectionModel().clearSelection();
                // }
            }
        });
        /**
         * Ugly patch for a problem that causes a reselection of the TreeItem on
         * mouseRelease even if the selection has been cleared
         */
        setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (ev.isSecondaryButtonDown()) {
                    showContextMenu(ev);
                }
                // if (secPressed) {
                // secPressed = false;
                // getSelectionModel().clearSelection();
                // }
            }
        });

    }

    private void showContextMenu(final MouseEvent ev) {
        if (getSelectionModel().getSelectedItem() != null && selectedCM == null) {
            final TreeItem<SVNode> node = getSelectionModel().getSelectedItem();
            final int hash = node.getValue().hashCode();
            final String nodeClass = node.getValue().getNodeClass();
            final boolean last = node.getChildren().isEmpty();
            final boolean collapsed = forcedCollapsedItems.contains(hash);
            final boolean expanded = forcedExpandedItems.contains(hash);
            final boolean collapsedClass = forcedCollapsedNodeClassItems.contains(nodeClass);
            final boolean expandedClass = forcedExpandedNodeClassItems.contains(nodeClass);
            final Menu forcedExpand = new Menu("Forced Expand");
            forcedExpand.setDisable(collapsed || collapsedClass);
            final Menu forcedCollapse = new Menu("Forced Collapse");
            forcedCollapse.setDisable(expanded || expandedClass);
            selectedCM = new ContextMenu();
            final CheckMenuItem collapseNode = new CheckMenuItem("For this node");
            collapseNode.setOnAction(forceExpandCollapse(node, forcedCollapsedItems, hash, collapsed, true));
            collapseNode.setSelected(collapsed);
            collapseNode.setDisable(last);

            final CheckMenuItem collapseNodeType = new CheckMenuItem("For this node type");
            collapseNodeType.setOnAction(forceExpandCollapse(node, forcedCollapsedNodeClassItems, nodeClass, collapsedClass, true));
            collapseNodeType.setSelected(collapsedClass);
            forcedCollapse.getItems().addAll(collapseNode, collapseNodeType);

            final CheckMenuItem expandNode = new CheckMenuItem("For this node");
            expandNode.setOnAction(forceExpandCollapse(node, forcedExpandedItems, hash, expanded, false));
            expandNode.setSelected(expanded);
            expandNode.setDisable(last);
            final CheckMenuItem expandNodeType = new CheckMenuItem("For this node type");
            expandNodeType.setOnAction(forceExpandCollapse(node, forcedExpandedNodeClassItems, nodeClass, expandedClass, false));
            expandNodeType.setSelected(expandedClass);
            forcedExpand.getItems().addAll(expandNode, expandNodeType);

            final Menu goTo = new Menu("Go to");
            goTo.getItems().addAll(goToTab("Details", SVTab.DETAILS), goToTab("Events", SVTab.EVENTS), goToTab("JavaDoc", SVTab.JAVADOC));

            final MenuItem close = new MenuItem("Close");
            close.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(final ActionEvent e) {
                    selectedCM.hide();
                }
            });
            final MenuItem deselect = new MenuItem("Clear selection");
            deselect.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(final ActionEvent e) {
                    getSelectionModel().clearSelection();
                }
            });
            deselect.disableProperty().bind(getSelectionModel().selectedItemProperty().isNull());

            selectedCM.getItems().addAll(deselect, forcedCollapse, forcedExpand, goTo, close);
            selectedCM.show(ScenegraphTreeView.this, ev.getScreenX(), ev.getScreenY());
        }
    }

    private MenuItem goToTab(final String text, final SVTab tab) {
        final MenuItem goTo = new MenuItem(text);
        goTo.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent e) {
                container.goTo(tab);
            }
        });
        return goTo;
    }

    @SuppressWarnings({ "unchecked" }) private EventHandler<ActionEvent> forceExpandCollapse(final TreeItem<SVNode> node, @SuppressWarnings("rawtypes") final List filter, final Object data, final boolean remove, final boolean expandIfRemoved) {
        return new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent e) {
                if (remove) {
                    filter.remove(data);
                    // This is not completely correct but...
                    container.forceUpdate();
                } else {
                    filter.add(data);
                    container.forceUpdate();
                }
            }
        };
    }

    protected void setSelectedNode(final TreeItem<SVNode> item) {
        // TODO Auto-generated method stub
        if (item != null) {
            container.setSelectedNode(stages.get(findStageForNode(item).getValue()), item.getValue());
        } else {
            container.setSelectedNode(null, null);
        }
    }

    TreeItem<SVNode> findStageForNode(final TreeItem<SVNode> item) {
        if (item.getValue() instanceof SVDummyNode && ((SVDummyNode) item.getValue()).getNodeType() == NodeType.STAGE) {
            return item;
        } else {
            return findStageForNode(item.getParent());
        }
    }

    void nodeSelected(final SVNode nodeData) {
        if (nodeData != null && treeViewData.containsKey(nodeData)) {
            final TreeItem<SVNode> item = treeViewData.get(nodeData);
            getSelectionModel().select(item);
            scrollTo(getSelectionModel().getSelectedIndex());
        }
    }

    void placeStageRoot(final StageController controller, final TreeItem<SVNode> stageRoot) {
        /**
         * Create the main root which will not be visible
         */
        if (apps == null) {
            final SVDummyNode dummy = new SVDummyNode("Apps", "Java", 0, NodeType.VMS_ROOT);
            apps = new TreeItem<SVNode>(dummy);
            apps.setExpanded(true);
        }
        TreeItem<SVNode> app = null;
        /**
         * Find if the application related with this stage (VM - XXXX) was
         * previously present
         */
        final List<TreeItem<SVNode>> apps = this.apps.getChildren();
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getValue().getNodeId() == controller.getAppController().getID()) {
                app = apps.get(i);
                break;
            }
        }
        /**
         * Create the application node (VM - XXXX)
         */
        if (app == null) {
            final SVDummyNode dummy = new SVDummyNode("VM - " + controller.getAppController(), "Java", controller.getAppController().getID(), NodeType.VM);
            app = new TreeItem<SVNode>(dummy, new ImageView(DisplayUtils.getIcon(dummy)));
            app.setExpanded(false);
            this.apps.getChildren().add(app);
        }
        /**
         * Check if the stage was already present
         */
        boolean added = false;
        final List<TreeItem<SVNode>> apps2 = app.getChildren();
        for (int i = 0; i < apps2.size(); i++) {
            if (apps2.get(i).getValue().getNodeId() == controller.getID().getStageID()) {
                /**
                 * If it was present remove the old one and insert the new one
                 */
                // if (listeners.containsKey(controller)) {
                // apps2.get(i).removeEventHandler(TreeItem.branchCollapsedEvent(),
                // listeners.get(controller));
                // apps2.get(i).removeEventHandler(TreeItem.branchExpandedEvent(),
                // listeners.get(controller));
                // }
                apps2.remove(i);
                apps2.add(i, stageRoot);
                added = true;
                break;
            }
        }
        if (!added) {
            // Not previously found, include
            app.getChildren().add(stageRoot);
        }
        /**
         * If only one VM is present
         */
        if (apps.size() == 1) {
            /**
             * If there were no stages present in the VM or the was only one but
             * was the same the root node will be root node the Stage
             */
            if (app.getChildren().size() == 0 || (app.getChildren().size() == 1 && app.getChildren().get(0).getValue().equals(stageRoot.getValue()))) {
                placeNewRoot(stageRoot);
            } else {
                /**
                 * More than one Stage is present, the root node will be the VM
                 * - XXXX node
                 */
                placeNewRoot(app);
            }
        } else {
            // More than one VM are running "Apps" node is the root
            placeNewRoot(this.apps);
        }
    }

    /**
     * This is a patch for TreeView indentation issue
     * 
     * @param realNode
     */
    void patchRoot(final TreeItem<SVNode> realNode) {
        this.patchedNode = realNode;
        final TreeItem<SVNode> real = new TreeItem<SVNode>(realNode.getValue(), realNode.getGraphic());
        real.getChildren().addAll(realNode.getChildren());
        setRoot(real);
    }

    @SuppressWarnings("unchecked") void unpatchRoot(final TreeItem<SVNode> newRoot) {
        /**
         * Another ugly patch for solving the indentation issue in this case
         * when a node that was root node is inside the tree
         */
        if (this.patchedNode != null && ((SVDummyNode) getRoot().getValue()).getNodeType() != ((SVDummyNode) newRoot.getValue()).getNodeType()) {
            @SuppressWarnings("rawtypes") final TreeItem[] items = this.patchedNode.getChildren().toArray(new TreeItem[0]);
            this.patchedNode.getChildren().setAll(items);

        }
        this.patchedNode = null;
    }

    void placeNewRoot(final TreeItem<SVNode> newRoot) {
        unpatchRoot(newRoot);
        patchRoot(newRoot);
    }

    void updateRoot() {
        final List<TreeItem<SVNode>> apps = this.apps.getChildren();
        if (apps.isEmpty() || apps.size() > 1) {
            placeNewRoot(this.apps);
        } else {
            final TreeItem<SVNode> app = apps.get(0);
            if (app.getChildren().size() == 1) {
                final TreeItem<SVNode> stageRoot = app.getChildren().get(0);
                placeNewRoot(stageRoot);
            } else {
                placeNewRoot(app);
            }
        }
    }

    void clearApp(final AppController appController) {
        for (final Iterator<TreeItem<SVNode>> iterator = apps.getChildren().iterator(); iterator.hasNext();) {
            final TreeItem<SVNode> type = iterator.next();
            if (type.getValue().getNodeId() == appController.getID()) {
                iterator.remove();
                break;
            }
        }
        updateRoot();
    }

    void clearStage(final StageController stageController) {
        for (final Iterator<TreeItem<SVNode>> iterator = apps.getChildren().iterator(); iterator.hasNext();) {
            final TreeItem<SVNode> type = iterator.next();
            if (type.getValue().getNodeId() == stageController.getID().getAppID()) {
                for (final Iterator<TreeItem<SVNode>> iterator2 = type.getChildren().iterator(); iterator2.hasNext();) {
                    final TreeItem<SVNode> type2 = iterator2.next();
                    if (type2.getValue().getNodeId() == stageController.getID().getStageID()) {
                        // type2.removeEventHandler(TreeItem.branchCollapsedEvent(),
                        // listeners.get(stageController));
                        // type2.removeEventHandler(TreeItem.branchExpandedEvent(),
                        // listeners.get(stageController));
                        // listeners.remove(stageController);
                        iterator2.remove();
                        updateRoot();
                        return;
                    }
                }
            }
        }

    }

    void updateStageModel(final StageController controller, final SVNode value, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
        // treeViewData.clear();
        stages.put(value, controller);
        previouslySelectedItem = null;
        blockSelection = true;
        removeForNode(getTreeItem(value));
        blockSelection = false;
        final TreeItem<SVNode> root = createTreeItem(value, showNodesIdInTree, showFilteredNodesInTree);
        final StageCollapsingListener listener = new StageCollapsingListener(root, controller);
        root.addEventHandler(TreeItem.branchCollapsedEvent(), listener);
        root.addEventHandler(TreeItem.branchExpandedEvent(), listener);
        // this.listeners.put(controller, listener);
        placeStageRoot(controller, root);

        if (previouslySelectedItem != null) {
            /**
             * TODO Why this is not working??
             */
            // treeView.getSelectionModel().clearSelection();
            // treeView.getSelectionModel().select(previouslySelectedItem);
            /**
             * TODO Remove
             */
            setSelectedNode(previouslySelectedItem);

        }
    }

    void removeNode(final SVNode node) {
        blockSelection = true;
        doRemoveNode(node);
        blockSelection = false;
    }

    void doRemoveNode(final SVNode node) {
        try {
            if (StageControllerImpl.isNormalNode(node)) {
                TreeItem<SVNode> selected = null;
                if (container.getSelectedNode() == node) {
                    getSelectionModel().clearSelection();
                    setSelectedNode(null);
                } else {
                    // Ugly workaround
                    selected = getSelectionModel().getSelectedItem();
                }
                final TreeItem<SVNode> treeItem = getTreeItem(node);
                /**
                 * TODO Analyze this problem:
                 * 
                 * In some situations a parent node could be removed by
                 * visibility and after that a children could also change its
                 * visibility to false triggering a removal that actually does
                 * not exist. In those situations we should keep visibility
                 * listener on the parent and remove it to its childrens. Anyway
                 * this need to be tested deeply and this protection is not so
                 * dangerous
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
                        doRemoveNode(children[i].getValue());
                    }
                }

                // This does not seem to delete the TreeItem from the tree --
                // only
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
        } catch (final NullPointerException e2) {
            throw new RuntimeException("Error while removing node:" + node.getExtendedId(), e2);
        }

    }

    void addNewNode(final SVNode alive, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
        blockSelection = true;
        doAddNewNode(alive, showNodesIdInTree, showFilteredNodesInTree);
        blockSelection = false;
    }

    private void doAddNewNode(final SVNode alive, final boolean showNodesIdInTree, final boolean showFilteredNodesInTree) {
        try {
            if (StageControllerImpl.isNormalNode(alive)) {
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
                         * We try to insert the treeItem in the real position of
                         * the parent
                         */
                        boolean posFound = false;
                        int previousPos = -1;
                        final List<SVNode> childrens = parent.getChildren();
                        // if (childrens != null) {
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
                            // }
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

        } catch (final NullPointerException e) {
            throw new RuntimeException("Error while adding new node:" + alive.getExtendedId() + " parent:" + alive.getParent() + " treeParent:" + (alive.getParent() == null ? "null" : getTreeItem(alive.getParent())), e);
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
        final ImageView graphic = new ImageView(DisplayUtils.getIcon(node));
        graphic.setFitHeight(16);
        graphic.setFitWidth(16);
        final TreeItem<SVNode> treeItem = new TreeItem<SVNode>(node, graphic);
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
        final int hash = node.hashCode();
        final String nodeClass = node.getNodeClass();
        if (!forcedCollapsedItems.contains(hash) && !forcedCollapsedNodeClassItems.contains(nodeClass)) {
            treeItem.setExpanded(expand || node.isExpanded() || forcedExpandedItems.contains(hash) || forcedExpandedNodeClassItems.contains(nodeClass));
        }

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
    interface ConnectorController {
        void setSelectedNode(StageController controller, SVNode node);

        SVNode getSelectedNode();

        void forceUpdate();

        void goTo(final SVTab tab);

        void openStage(StageController controller);

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

    class StageCollapsingListener implements EventHandler<TreeModificationEvent<Object>> {

        TreeItem<SVNode> root;
        StageController controller;

        public StageCollapsingListener(final TreeItem<SVNode> root, final StageController controller) {
            this.root = root;
            this.controller = controller;
        }

        @Override public void handle(final TreeModificationEvent<Object> ev) {
            if (!root.isExpanded() && controller.isOpened()) {
                // Closing controller
                controller.close();
            } else if (root.isExpanded() && !controller.isOpened()) {
                // Opening controller
                container.openStage(controller);
            }
        }
    }

}
