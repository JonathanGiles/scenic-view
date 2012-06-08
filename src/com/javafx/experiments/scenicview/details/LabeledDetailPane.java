/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import com.javafx.experiments.scenicview.details.*;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.TextAlignment;

/**
 * 
 * @author aim
 */
public class LabeledDetailPane extends DetailPane {
    Detail textDetail;
    Detail graphicDetail;
    Detail contentDisplayDetail;
    Detail graphicTextGapDetail;
    Detail alignmentDetail;
    Detail textAlignmentDetail;
    Detail textOverrunDetail;
    Detail wrapTextDetail;
    Detail underlineDetail;
    Detail fontDetail;

    @Override public Class<? extends Node> getTargetClass() {
        return Labeled.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Labeled;
    }

    @Override protected void createDetails() {
        int row = 0;

        textDetail = addDetail("text", "text:", row++);
        graphicDetail = addDetail("graphic", "graphic:", row++);
        graphicTextGapDetail = addDetail("graphicTextGap", "graphicTextGap:", row++);
        contentDisplayDetail = addDetail("contentDisplay", "contentDisplay:", row++);
        alignmentDetail = addDetail("alignment", "alignment:", row++);
        textAlignmentDetail = addDetail("textAlignment", "textAlignment:", row++);
        textOverrunDetail = addDetail("textOverrun", "textOverrun:", row++);
        wrapTextDetail = addDetail("wrapText", "wrapText:", row++);
        underlineDetail = addDetail("underline", "underline:", row++);
        fontDetail = addDetail("font", "font:", row++);

    }

    @Override protected void updateAllDetails() {
        updateDetail("*");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Labeled labeled = (Labeled) getTarget();
        if (all || propertyName.equals("text")) {
            textDetail.valueLabel.setText(labeled != null ? "\"" + labeled.getText() + "\"" : "-");
            textDetail.setIsDefault(labeled == null || labeled.getText() == null);
            textDetail.setSimpleProperty(labeled != null ? labeled.textProperty() : null);
            if (!all)
                textDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("font")) {
            fontDetail.valueLabel.setText(labeled != null ? labeled.getFont().getName() : "-");
            fontDetail.setIsDefault(labeled == null);
            if (!all)
                fontDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("graphic")) {
            final Node graphic = labeled != null ? labeled.getGraphic() : null;
            graphicDetail.valueLabel.setText(graphic != null ? graphic.getClass().getSimpleName() : "-");
            graphicDetail.setIsDefault(graphic == null);
            if (!all)
                graphicDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("graphicTextGap")) {
            graphicTextGapDetail.valueLabel.setText(labeled != null ? f.format(labeled.getGraphicTextGap()) : "-");
            graphicTextGapDetail.setIsDefault(labeled == null || labeled.getGraphic() == null);
            graphicTextGapDetail.setSimpleProperty(labeled != null ? labeled.graphicTextGapProperty() : null);
            if (!all)
                graphicTextGapDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("alignment")) {
            alignmentDetail.valueLabel.setText(labeled != null ? labeled.getAlignment().toString() : "-");
            alignmentDetail.setIsDefault(labeled == null);
            alignmentDetail.setEnumProperty(labeled != null ? labeled.alignmentProperty() : null, Pos.class);
            if (!all)
                alignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textAlignment")) {
            textAlignmentDetail.valueLabel.setText(labeled != null ? labeled.getTextAlignment().toString() : "-");
            textAlignmentDetail.setIsDefault(labeled == null);
            textAlignmentDetail.setEnumProperty(labeled != null ? labeled.textAlignmentProperty() : null, TextAlignment.class);
            if (!all)
                textAlignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textOverrun")) {
            textOverrunDetail.valueLabel.setText(labeled != null ? labeled.getTextOverrun().toString() : "-");
            textOverrunDetail.setIsDefault(labeled == null);
            textOverrunDetail.setEnumProperty(labeled != null ? labeled.textOverrunProperty() : null, OverrunStyle.class);
            if (!all)
                textOverrunDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("contentDisplay")) {
            contentDisplayDetail.valueLabel.setText(labeled != null ? labeled.getContentDisplay().toString() : "-");
            contentDisplayDetail.setIsDefault(labeled == null);
            contentDisplayDetail.setEnumProperty(labeled != null ? labeled.contentDisplayProperty() : null, ContentDisplay.class);
            if (!all)
                contentDisplayDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("underline")) {
            underlineDetail.valueLabel.setText(labeled != null ? Boolean.toString(labeled.isUnderline()) : "-");
            underlineDetail.setIsDefault(labeled == null || !labeled.isUnderline());
            underlineDetail.setSimpleProperty(labeled != null ? labeled.underlineProperty() : null);
            if (!all)
                underlineDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("wrapText")) {
            wrapTextDetail.valueLabel.setText(labeled != null ? Boolean.toString(labeled.isWrapText()) : "-");
            wrapTextDetail.setIsDefault(labeled == null || !labeled.isWrapText());
            wrapTextDetail.setSimpleProperty(labeled != null ? labeled.wrapTextProperty() : null);
            if (!all)
                wrapTextDetail.updated();
            if (!all)
                return;
        }
    }

}
