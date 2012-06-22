/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import static com.javafx.experiments.scenicview.DisplayUtils.boundsToString;
import javafx.beans.value.WritableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Transform;

import com.javafx.experiments.scenicview.DisplayUtils;

/**
 * 
 * @author aim
 */
public class NodeDetailPane extends DetailPane {

    Detail nodeClassName;
    Detail styleClassDetail;
    Detail managedDetail;
    Detail visibleDetail;
    Detail layoutBoundsDetail;
    Detail effectDetail;
    Detail opacityDetail;
    Detail clipDetail;
    Detail transformsDetail;
    Detail scaleXYDetail;
    Detail rotateDetail;
    Detail layoutXYDetail;
    Detail translateXYDetail;
    Detail boundsInParentDetail;
    Detail resizableDetail;
    Detail contentBiasDetail;
    Detail baselineDetail;
    Detail minSizeDetail;
    Detail prefSizeDetail;
    Detail maxSizeDetail;
    Detail constraintsDetail;

    ListChangeListener<Transform> transformListener;

    public NodeDetailPane() {
        super();
        transformListener = new ListChangeListener<Transform>() {
            @Override public void onChanged(final Change<? extends Transform> c) {
                updateDetail("transforms");
            }
        };
    }

    @Override public Class<? extends Node> getTargetClass() {
        return Node.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Node;
    }

    @Override protected void createDetails() {
        int row = 0;

        nodeClassName = addDetail("className", "className:", row++);
        styleClassDetail = addDetail("styleClass", "styleClass:", row++);
        visibleDetail = addDetail("visible", "visible:", row++);
        managedDetail = addDetail("managed", "managed:", row++);
        final Rectangle layoutBoundsIcon = new Rectangle(12, 12);
        layoutBoundsIcon.setFill(null);
        layoutBoundsIcon.setStroke(Color.GREEN);
        layoutBoundsIcon.setOpacity(.8);
        layoutBoundsIcon.getStrokeDashArray().addAll(3.0, 3.0);
        layoutBoundsIcon.setStrokeWidth(1);
        layoutBoundsDetail = addDetail("layoutBounds", "layoutBounds:", layoutBoundsIcon, new Label(), row++);
        effectDetail = addDetail("effect", "effect:", row++);
        opacityDetail = addDetail("opacity", "opacity:", row++);
        clipDetail = addDetail("clip", "clip:", row++);
        transformsDetail = addDetail("transforms", "transforms:", row++);
        scaleXYDetail = addDetail("scaleX", "scaleX/Y:", row++);
        rotateDetail = addDetail("rotate", "rotate:", row++);
        layoutXYDetail = addDetail("layoutX", "layoutX/Y:", row++);
        translateXYDetail = addDetail("translateX", "translateX/Y:", row++);
        final Rectangle boundsInParentIcon = new Rectangle(12, 12);
        boundsInParentIcon.setFill(Color.YELLOW);
        boundsInParentIcon.setOpacity(.5);
        boundsInParentDetail = addDetail("boundsInParent", "boundsInParent:", boundsInParentIcon, new Label(), row++);
        resizableDetail = addDetail(null, "resizable:", row++);
        contentBiasDetail = addDetail(null, "contentBias:", row++);
        final Group baselineIcon = new Group();
        final Line line = new Line(0, 8, 14, 8);
        line.setStroke(Color.RED);
        line.setOpacity(.75);
        line.setStrokeWidth(1);
        baselineIcon.getChildren().addAll(new Rectangle(10, 10, Color.TRANSPARENT), line);
        baselineDetail = addDetail(null, "baselineOffset:", baselineIcon, new Label(), row++);
        minSizeDetail = addDetail(null, "minWidth(h)/Height(w):", row++);
        prefSizeDetail = addDetail(null, "prefWidth(h)/Height(w):", row++);
        maxSizeDetail = addDetail(null, "maxWidth(h)/Height(w):", row++);
        constraintsDetail = addDetail(null, "layout constraints:", new ConstraintsDisplay(), row++);
    }

    @Override public void setTarget(final Object value) {
        final Node old = (Node) getTarget();
        if (old != null) {
            old.getTransforms().removeListener(transformListener);
        }
        super.setTarget(value);

        final Node node = (Node) value;
        if (node != null) {
            node.getTransforms().addListener(transformListener);
        }
    }

    @Override protected void updateAllDetails() {
        final Node node = (Node) getTarget();

        updateDetail("*");

        // No property change events on these
        resizableDetail.valueLabel.setText(node != null ? Boolean.toString(node.isResizable()) : "-");
        // boolean showResizable = node != null && node.isResizable();
        resizableDetail.setIsDefault(node == null);

        Orientation bias = null;
        if (node != null) {
            bias = node.getContentBias();
            contentBiasDetail.valueLabel.setText(bias != null ? bias.toString() : "none");
        } else {
            contentBiasDetail.valueLabel.setText("-");
        }
        contentBiasDetail.setIsDefault(node == null || node.getContentBias() == null);

        baselineDetail.valueLabel.setText(node != null ? f.format(node.getBaselineOffset()) : "-");
        baselineDetail.setIsDefault(node == null);

        if (node != null) {
            double minw = 0;
            double minh = 0;
            double prefw = 0;
            double prefh = 0;
            double maxw = 0;
            double maxh = 0;

            if (bias == null) {
                minSizeDetail.label.setText("minWidth(-1)/minHeight(-1):");
                prefSizeDetail.label.setText("prefWidth(-1)/prefHeight(-1):");
                maxSizeDetail.label.setText("maxWidth(-1)/maxHeight(-1):");
                minw = node.minWidth(-1);
                minh = node.minHeight(-1);
                prefw = node.prefWidth(-1);
                prefh = node.prefHeight(-1);
                maxw = node.maxWidth(-1);
                maxh = node.maxHeight(-1);
            } else if (bias == Orientation.HORIZONTAL) {
                minSizeDetail.label.setText("minWidth(-1)/minHeight(w):");
                prefSizeDetail.label.setText("prefWidth(-1)/prefHeight(w):");
                maxSizeDetail.label.setText("maxWidth(-1)/maxHeight(w):");
                minw = node.minWidth(-1);
                minh = node.minHeight(minw);
                prefw = node.prefWidth(-1);
                prefh = node.prefHeight(prefw);
                maxw = node.maxWidth(-1);
                maxh = node.maxHeight(maxw);
            } else { // VERTICAL
                minSizeDetail.label.setText("minWidth(h)/minHeight(-1):");
                prefSizeDetail.label.setText("prefWidth(h)/prefHeight(-1):");
                maxSizeDetail.label.setText("maxWidth(h)/maxHeight(-1):");
                minh = node.minHeight(-1);
                minw = node.minWidth(minh);
                prefh = node.prefHeight(-1);
                prefw = node.prefWidth(prefh);
                maxh = node.maxHeight(-1);
                maxw = node.maxWidth(maxh);
            }

            minSizeDetail.valueLabel.setText(f.format(minw) + " x " + f.format(minh));
            prefSizeDetail.valueLabel.setText(f.format(prefw) + " x " + f.format(prefh));
            maxSizeDetail.valueLabel.setText((maxw >= Double.MAX_VALUE ? "MAXVALUE" : f.format(maxw)) + " x " + (maxh >= Double.MAX_VALUE ? "MAXVALUE" : f.format(maxh)));
        } else {
            minSizeDetail.valueLabel.setText("-");
            prefSizeDetail.valueLabel.setText("-");
            maxSizeDetail.valueLabel.setText("-");
        }
        final boolean fade = node == null || !node.isResizable();
        minSizeDetail.setIsDefault(fade);
        prefSizeDetail.setIsDefault(fade);
        maxSizeDetail.setIsDefault(fade);

        ((ConstraintsDisplay) constraintsDetail.valueNode).setPropertiesMap(node != null && node.hasProperties() ? node.getProperties() : null);
        constraintsDetail.setIsDefault(!((ConstraintsDisplay) constraintsDetail.valueNode).isShowingConstraints());
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Node node = (Node) getTarget();

        if (all && node != null) {
            nodeClassName.valueLabel.setText(node.getClass().getName());
        }

        if (all || propertyName.equals("styleClass")) {
            styleClassDetail.valueLabel.setText(node != null ? "\"" + node.getStyleClass().toString() + "\"" : "-");
            styleClassDetail.setIsDefault(node == null || node.getStyleClass().isEmpty());
            styleClassDetail.setSerializer(new WritableValue<String>() {
                @Override public void setValue(final String data) {
                    final String[] styles = data.split(" ");
                    node.getStyleClass().clear();
                    node.getStyleClass().addAll(styles);
                }

                @Override public String getValue() {
                    return node.getStyleClass().toString();
                }
            });
            if (!all)
                return;
        }
        if (all || propertyName.equals("visible")) {
            visibleDetail.valueLabel.setText(node != null ? Boolean.toString(node.isVisible()) : "-");
            visibleDetail.setIsDefault(node == null || node.isVisible());
            visibleDetail.setSimpleProperty((node != null) ? node.visibleProperty() : null);
            if (!all)
                visibleDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("managed")) {
            managedDetail.valueLabel.setText(node != null ? Boolean.toString(node.isManaged()) : "-");
            managedDetail.setIsDefault(node == null || node.isManaged());
            managedDetail.setSimpleProperty((node != null) ? node.managedProperty() : null);
            if (!all)
                managedDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("opacity")) {
            opacityDetail.valueLabel.setText(node != null ? node.getOpacity() * 100 + "%" : "-");
            opacityDetail.setIsDefault(node == null || node.getOpacity() == 1.0);
            opacityDetail.setSimpleProperty((node != null) ? node.opacityProperty() : null);
            /**
             * Include the slider
             */
            if (opacityDetail.serializer != null) {
                ((SimpleSerializer) opacityDetail.serializer).setMaxValue(1);
                ((SimpleSerializer) opacityDetail.serializer).setMinValue(0);
            }
            if (!all)
                opacityDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("effect")) {
            final Effect effect = node != null ? node.getEffect() : null;
            effectDetail.valueLabel.setText(effect != null ? effect.getClass().getSimpleName() : "-");
            effectDetail.setIsDefault(effect == null);
            if (!all)
                effectDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("layoutBounds")) {
            layoutBoundsDetail.valueLabel.setText(node != null ? boundsToString(node.getLayoutBounds()) : "-");
            layoutBoundsDetail.setIsDefault(node == null);
            if (!all)
                layoutBoundsDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("transforms")) {
            String txstr = "-";
            if (node != null && node.getTransforms().size() > 0) {
                final StringBuilder str = new StringBuilder();
                for (final Transform tx : node.getTransforms()) {
                    str.append(DisplayUtils.transformToString(tx));
                    str.append(" ");
                }
                txstr = str.toString();
            }
            transformsDetail.valueLabel.setText(txstr);
            transformsDetail.setIsDefault(node == null || node.getTransforms().size() == 0);
            if (!all)
                transformsDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("scaleX") || propertyName.equals("scaleY")) {
            scaleXYDetail.valueLabel.setText(node != null ? f.format(node.getScaleX()) + " x " + f.format(node.getScaleY()) : "-");
            scaleXYDetail.setIsDefault(node == null || (node.getScaleX() == 1.0 && node.getScaleY() == 1.0));
            scaleXYDetail.setSimpleSizeProperty(node != null ? node.scaleXProperty() : null, node != null ? node.scaleYProperty() : null);
            if (!all)
                scaleXYDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("rotate")) {
            rotateDetail.valueLabel.setText(node != null ? f.format(node.getRotate()) : "-");
            rotateDetail.setIsDefault(node == null || node.getRotate() == 0);
            rotateDetail.setSimpleProperty((node != null) ? node.rotateProperty() : null);
            if (!all)
                rotateDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("clip")) {
            final Node clip = node != null ? node.getClip() : null;
            clipDetail.valueLabel.setText(node != null ? clip != null ? (clip.getClass().getSimpleName() + ":" + boundsToString(clip.getBoundsInLocal())) : "null" : "-");
            clipDetail.setIsDefault(clip == null);
            if (!all)
                clipDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("layoutX") || propertyName.equals("layoutY")) {
            layoutXYDetail.valueLabel.setText(node != null ? f.format(node.getLayoutX()) + "," + f.format(node.getLayoutY()) : "-");
            layoutXYDetail.setIsDefault(node == null || (node.getLayoutX() == 0 && node.getLayoutY() == 0));
            layoutXYDetail.setSimpleSizeProperty(node != null ? node.layoutXProperty() : null, node != null ? node.layoutYProperty() : null);
            if (!all)
                return;
        }
        if (all || propertyName.equals("translateX") || propertyName.equals("translateY")) {
            translateXYDetail.valueLabel.setText(node != null ? f.format(node.getTranslateX()) + "," + f.format(node.getTranslateY()) : "-");
            translateXYDetail.setIsDefault(node == null || (node.getTranslateX() == 0 && node.getTranslateY() == 0));
            translateXYDetail.setSimpleSizeProperty(node != null ? node.translateXProperty() : null, node != null ? node.translateYProperty() : null);
            if (!all)
                translateXYDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("boundsInParent")) {
            boundsInParentDetail.valueLabel.setText(node != null ? boundsToString(node.getBoundsInParent()) : "-");
            boundsInParentDetail.setIsDefault(node == null);
            if (!all)
                boundsInParentDetail.updated();
        }
    }
}
