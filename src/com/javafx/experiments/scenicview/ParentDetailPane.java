/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import static com.javafx.experiments.scenicview.DisplayUtils.getBranchCount;
import javafx.scene.*;

/**
 * 
 * @author aim
 */
public class ParentDetailPane extends DetailPane {

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
        int row = 0;

        childCountDetail = addDetail("child Count", "child count:", row++);
        branchCountDetail = addDetail("branch Count", "branch count:", row++);
        needsLayoutDetail = addDetail("needsLayout", "needsLayout:", row++);

    }

    @Override protected void updateAllDetails() {
        final Parent parent = (Parent) getTarget();

        childCountDetail.valueLabel.setText(parent != null ? Integer.toString(parent.getChildrenUnmodifiable().size()) : "-");
        branchCountDetail.valueLabel.setText(parent != null ? Integer.toString(getBranchCount(parent)) : "-");

        // No property change events on these
        needsLayoutDetail.valueLabel.setText(parent != null ? Boolean.toString(parent.isNeedsLayout()) : "-");
        needsLayoutDetail.setIsDefault(parent == null || !parent.isNeedsLayout());
    }

    @Override protected void updateDetail(final String propertyName) {
        // no descrete "properties"
    }
}
