/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.connector.details;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.TextAlignment;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;

/**
 * 
 * @author aim
 */
public class LabeledDetailPaneInfo extends DetailPaneInfo {
    public LabeledDetailPaneInfo(final AppEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.LABELED);
        // TODO Auto-generated constructor stub
    }

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
        textDetail = addDetail("text", "text:");
        graphicDetail = addDetail("graphic", "graphic:");
        graphicTextGapDetail = addDetail("graphicTextGap", "graphicTextGap:");
        contentDisplayDetail = addDetail("contentDisplay", "contentDisplay:");
        alignmentDetail = addDetail("alignment", "alignment:");
        textAlignmentDetail = addDetail("textAlignment", "textAlignment:");
        textOverrunDetail = addDetail("textOverrun", "textOverrun:");
        wrapTextDetail = addDetail("wrapText", "wrapText:");
        underlineDetail = addDetail("underline", "underline:");
        fontDetail = addDetail("font", "font:");

    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Labeled labeled = (Labeled) getTarget();
        if (all || propertyName.equals("text")) {
            textDetail.setValue(labeled != null ? "\"" + labeled.getText() + "\"" : "-");
            textDetail.setIsDefault(labeled == null || labeled.getText() == null);
            textDetail.setSimpleProperty(labeled != null ? labeled.textProperty() : null);
            if (!all)
                textDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("font")) {
            fontDetail.setValue(labeled != null ? labeled.getFont().getName() : "-");
            fontDetail.setIsDefault(labeled == null);
            if (!all)
                fontDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("graphic")) {
            final Node graphic = labeled != null ? labeled.getGraphic() : null;
            graphicDetail.setValue(graphic != null ? graphic.getClass().getSimpleName() : "-");
            graphicDetail.setIsDefault(graphic == null);
            if (!all)
                graphicDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("graphicTextGap")) {
            graphicTextGapDetail.setValue(labeled != null ? f.format(labeled.getGraphicTextGap()) : "-");
            graphicTextGapDetail.setIsDefault(labeled == null || labeled.getGraphic() == null);
            graphicTextGapDetail.setSimpleProperty(labeled != null ? labeled.graphicTextGapProperty() : null);
            if (!all)
                graphicTextGapDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("alignment")) {
            alignmentDetail.setValue(labeled != null ? labeled.getAlignment().toString() : "-");
            alignmentDetail.setIsDefault(labeled == null);
            alignmentDetail.setEnumProperty(labeled != null ? labeled.alignmentProperty() : null, Pos.class);
            if (!all)
                alignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textAlignment")) {
            textAlignmentDetail.setValue(labeled != null ? labeled.getTextAlignment().toString() : "-");
            textAlignmentDetail.setIsDefault(labeled == null);
            textAlignmentDetail.setEnumProperty(labeled != null ? labeled.textAlignmentProperty() : null, TextAlignment.class);
            if (!all)
                textAlignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textOverrun")) {
            textOverrunDetail.setValue(labeled != null ? labeled.getTextOverrun().toString() : "-");
            textOverrunDetail.setIsDefault(labeled == null);
            textOverrunDetail.setEnumProperty(labeled != null ? labeled.textOverrunProperty() : null, OverrunStyle.class);
            if (!all)
                textOverrunDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("contentDisplay")) {
            contentDisplayDetail.setValue(labeled != null ? labeled.getContentDisplay().toString() : "-");
            contentDisplayDetail.setIsDefault(labeled == null);
            contentDisplayDetail.setEnumProperty(labeled != null ? labeled.contentDisplayProperty() : null, ContentDisplay.class);
            if (!all)
                contentDisplayDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("underline")) {
            underlineDetail.setValue(labeled != null ? Boolean.toString(labeled.isUnderline()) : "-");
            underlineDetail.setIsDefault(labeled == null || !labeled.isUnderline());
            underlineDetail.setSimpleProperty(labeled != null ? labeled.underlineProperty() : null);
            if (!all)
                underlineDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("wrapText")) {
            wrapTextDetail.setValue(labeled != null ? Boolean.toString(labeled.isWrapText()) : "-");
            wrapTextDetail.setIsDefault(labeled == null || !labeled.isWrapText());
            wrapTextDetail.setSimpleProperty(labeled != null ? labeled.wrapTextProperty() : null);
            if (!all)
                wrapTextDetail.updated();
            if (!all)
                return;
        }
        if (all)
            sendAllDetails();
    }

}
