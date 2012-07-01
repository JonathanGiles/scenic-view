/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.connector.details;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.text.*;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;

/**
 * 
 * @author aim
 */
public class TextDetailPaneInfo extends DetailPaneInfo {

    Detail contentDetail;
    Detail fontDetail;
    Detail originDetail;
    Detail xyDetail;
    Detail alignmentDetail;
    Detail boundsTypeDetail;
    Detail wrappingWidthDetail;
    Detail underlineDetail;
    Detail strikethroughDetail;

    public TextDetailPaneInfo(final AppEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.TEXT);
    }

    @Override public Class<? extends Node> getTargetClass() {
        return Text.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Text;
    }

    @Override protected void createDetails() {
        contentDetail = addDetail("content", "content:");
        fontDetail = addDetail("font", "font:");
        originDetail = addDetail("textOrigin", "textOrigin:");
        xyDetail = addDetail("x", "x/y:");
        alignmentDetail = addDetail("textAlignment", "textAlignment:");
        boundsTypeDetail = addDetail("textBoundsType", "boundsType:");
        wrappingWidthDetail = addDetail("wrappingWidth", "wrappingWidth:");
        underlineDetail = addDetail("underline", "underline:");
        strikethroughDetail = addDetail("strikethrough", "strikethrough:");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Text textnode = (Text) getTarget();
        if (all || propertyName.equals("content")) {
            contentDetail.setValue(textnode != null ? "\"" + textnode.getText() + "\"" : "-");
            contentDetail.setIsDefault(textnode == null || textnode.getText() == null);
            contentDetail.setSimpleProperty((textnode != null) ? textnode.textProperty() : null);
            if (!all)
                contentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("font")) {
            fontDetail.setValue(textnode != null ? textnode.getFont().getName() : "-");
            fontDetail.setIsDefault(textnode == null);
            if (!all)
                fontDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textOrigin")) {
            originDetail.setValue(textnode != null ? textnode.getTextOrigin().toString() : "-");
            originDetail.setIsDefault(textnode == null);
            originDetail.setEnumProperty(textnode != null ? textnode.textOriginProperty() : null, VPos.class);
            if (!all)
                originDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("x") || propertyName.equals("y")) {
            xyDetail.setValue(textnode != null ? f.format(textnode.getX()) + " , " + f.format(textnode.getY()) : "-");
            xyDetail.setIsDefault(textnode == null || (textnode.getX() == 0 && textnode.getY() == 0));
            xyDetail.setSimpleSizeProperty(textnode != null ? textnode.xProperty() : null, textnode != null ? textnode.yProperty() : null);
            if (!all)
                xyDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textAlignment")) {
            alignmentDetail.setValue(textnode != null ? textnode.getTextAlignment().toString() : "-");
            alignmentDetail.setIsDefault(textnode == null);
            alignmentDetail.setEnumProperty(textnode != null ? textnode.textAlignmentProperty() : null, TextAlignment.class);
            if (!all)
                alignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textBoundsType")) {
            boundsTypeDetail.setValue(textnode != null ? textnode.getBoundsType().toString() : "-");
            boundsTypeDetail.setIsDefault(textnode == null || textnode.getBoundsType() == TextBoundsType.LOGICAL);
            boundsTypeDetail.setEnumProperty(textnode != null ? textnode.boundsTypeProperty() : null, TextBoundsType.class);
            if (!all)
                boundsTypeDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("wrappingWidth")) {
            wrappingWidthDetail.setValue(textnode != null ? f.format(textnode.getWrappingWidth()) : "-");
            wrappingWidthDetail.setIsDefault(textnode == null || textnode.getWrappingWidth() == 0);
            wrappingWidthDetail.setSimpleProperty((textnode != null) ? textnode.wrappingWidthProperty() : null);
            if (!all)
                wrappingWidthDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("underline")) {
            underlineDetail.setValue(textnode != null ? Boolean.toString(textnode.isUnderline()) : "-");
            underlineDetail.setIsDefault(textnode == null || !textnode.isUnderline());
            underlineDetail.setSimpleProperty(textnode != null ? textnode.underlineProperty() : null);
            if (!all)
                underlineDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("strikethrough")) {
            strikethroughDetail.setValue(textnode != null ? Boolean.toString(textnode.isStrikethrough()) : "-");
            strikethroughDetail.setIsDefault(textnode == null || !textnode.isStrikethrough());
            strikethroughDetail.setSimpleProperty(textnode != null ? textnode.strikethroughProperty() : null);
            if (!all)
                strikethroughDetail.updated();
        }
        if (all)
            sendAllDetails();
    }
}
