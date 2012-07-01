/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.connector.details;

import static com.javafx.experiments.scenicview.connector.ConnectorUtils.getBranchCount;
import javafx.scene.*;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;

/**
 * 
 * @author aim
 */
public class ParentDetailPaneInfo extends DetailPaneInfo {

    public ParentDetailPaneInfo(final AppEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.PARENT);
    }

    Detail needsLayoutDetail;
    Detail childCountDetail;
    Detail branchCountDetail;

    @Override public Class<? extends Node> getTargetClass() {
        return Parent.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Parent;
    }

    @Override protected void createDetails() {
        childCountDetail = addDetail("child Count", "child count:");
        branchCountDetail = addDetail("branch Count", "branch count:");
        needsLayoutDetail = addDetail("needsLayout", "needsLayout:");
    }

    @Override protected void updateAllDetails() {
        final Parent parent = (Parent) getTarget();

        childCountDetail.setValue(parent != null ? Integer.toString(parent.getChildrenUnmodifiable().size()) : "-");
        branchCountDetail.setValue(parent != null ? Integer.toString(getBranchCount(parent)) : "-");

        // No property change events on these
        needsLayoutDetail.setValue(parent != null ? Boolean.toString(parent.isNeedsLayout()) : "-");
        needsLayoutDetail.setIsDefault(parent == null || !parent.isNeedsLayout());
        sendAllDetails();
    }

    @Override protected void updateDetail(final String propertyName) {
        // no descrete "properties"
    }
}
