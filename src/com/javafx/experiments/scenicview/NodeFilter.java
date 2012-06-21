package com.javafx.experiments.scenicview;

import com.javafx.experiments.scenicview.connector.SVNode;

interface NodeFilter {
    /**
     * Checks if the node is accepted for this filter
     * 
     * @param node
     * @return
     */
    public boolean accept(SVNode node);

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
    
    /**
     * Flag to indicate if all the nodes must be expanded on filtering
     * @return
     */
    public boolean expandAllNodes();

}