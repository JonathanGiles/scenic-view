/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import com.javafx.experiments.scenicview.*;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.text.*;

/**
 * 
 * @author aim
 */
public class TextDetailPane extends DetailPane {

    Detail contentDetail;
    Detail fontDetail;
    Detail originDetail;
    Detail xyDetail;
    Detail alignmentDetail;
    Detail boundsTypeDetail;
    Detail wrappingWidthDetail;
    Detail underlineDetail;
    Detail strikethroughDetail;

    public TextDetailPane() {
        super();
    }

    @Override public Class<? extends Node> getTargetClass() {
        return Text.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Text;
    }

    @Override protected void createDetails() {
        int row = 0;

        contentDetail = addDetail("content", "content:", row++);
        fontDetail = addDetail("font", "font:", row++);
        originDetail = addDetail("textOrigin", "textOrigin:", row++);
        xyDetail = addDetail("x", "x/y:", row++);
        alignmentDetail = addDetail("textAlignment", "textAlignment:", row++);
        boundsTypeDetail = addDetail("textBoundsType", "boundsType:", row++);
        wrappingWidthDetail = addDetail("wrappingWidth", "wrappingWidth:", row++);
        underlineDetail = addDetail("underline", "underline:", row++);
        strikethroughDetail = addDetail("strikethrough", "strikethrough:", row++);

    }

    @Override protected void updateAllDetails() {
        updateDetail("*");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Text textnode = (Text) getTarget();
        if (all || propertyName.equals("content")) {
            contentDetail.valueLabel.setText(textnode != null ? "\"" + textnode.getText() + "\"" : "-");
            contentDetail.setIsDefault(textnode == null || textnode.getText() == null);
            contentDetail.setSimpleProperty((textnode != null) ? textnode.textProperty() : null);
            if (!all)
                contentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("font")) {
            fontDetail.valueLabel.setText(textnode != null ? textnode.getFont().getName() : "-");
            fontDetail.setIsDefault(textnode == null);
            if (!all)
                fontDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textOrigin")) {
            originDetail.valueLabel.setText(textnode != null ? textnode.getTextOrigin().toString() : "-");
            originDetail.setIsDefault(textnode == null);
            originDetail.setEnumProperty(textnode != null ? textnode.textOriginProperty() : null, VPos.class);
            if (!all)
                originDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("x") || propertyName.equals("y")) {
            xyDetail.valueLabel.setText(textnode != null ? f.format(textnode.getX()) + " , " + f.format(textnode.getY()) : "-");
            xyDetail.setIsDefault(textnode == null || (textnode.getX() == 0 && textnode.getY() == 0));
            xyDetail.setSimpleSizeProperty(textnode != null ? textnode.xProperty() : null, textnode != null ? textnode.yProperty() : null);
            if (!all)
                xyDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textAlignment")) {
            alignmentDetail.valueLabel.setText(textnode != null ? textnode.getTextAlignment().toString() : "-");
            alignmentDetail.setIsDefault(textnode == null);
            alignmentDetail.setEnumProperty(textnode != null ? textnode.textAlignmentProperty() : null, TextAlignment.class);
            if (!all)
                alignmentDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("textBoundsType")) {
            boundsTypeDetail.valueLabel.setText(textnode != null ? textnode.getBoundsType().toString() : "-");
            boundsTypeDetail.setIsDefault(textnode == null || textnode.getBoundsType() == TextBoundsType.LOGICAL);
            boundsTypeDetail.setEnumProperty(textnode != null ? textnode.boundsTypeProperty() : null, TextBoundsType.class);
            if (!all)
                boundsTypeDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("wrappingWidth")) {
            wrappingWidthDetail.valueLabel.setText(textnode != null ? f.format(textnode.getWrappingWidth()) : "-");
            wrappingWidthDetail.setIsDefault(textnode == null || textnode.getWrappingWidth() == 0);
            wrappingWidthDetail.setSimpleProperty((textnode != null) ? textnode.wrappingWidthProperty() : null);
            if (!all)
                wrappingWidthDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("underline")) {
            underlineDetail.valueLabel.setText(textnode != null ? Boolean.toString(textnode.isUnderline()) : "-");
            underlineDetail.setIsDefault(textnode == null || !textnode.isUnderline());
            underlineDetail.setSimpleProperty(textnode != null ? textnode.underlineProperty() : null);
            if (!all)
                underlineDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("strikethrough")) {
            strikethroughDetail.valueLabel.setText(textnode != null ? Boolean.toString(textnode.isStrikethrough()) : "-");
            strikethroughDetail.setIsDefault(textnode == null || !textnode.isStrikethrough());
            strikethroughDetail.setSimpleProperty(textnode != null ? textnode.strikethroughProperty() : null);
            if (!all)
                strikethroughDetail.updated();
        }
    }
}
