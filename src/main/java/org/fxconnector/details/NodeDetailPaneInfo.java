/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fxconnector.details;

import static org.fxconnector.ConnectorUtils.boundsToString;

import java.util.Set;

import javafx.beans.value.WritableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;

import org.fxconnector.ConnectorUtils;
import org.fxconnector.StageID;
import org.fxconnector.details.Detail.LabelType;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class NodeDetailPaneInfo extends DetailPaneInfo {

    Detail nodeClassName;
    Detail pseudoClassStateDetail;
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
//    private final Map<Long, String> pseudoStates = new HashMap<Long, String>();

    NodeDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.NODE);
        transformListener = new ListChangeListener<Transform>() {
            @Override public void onChanged(final Change<? extends Transform> c) {
                updateDetail("transforms");
            }
        };
    }

    @Override Class<? extends Node> getTargetClass() {
        return Node.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Node;
    }

    @Override protected void createDetails() {
        nodeClassName = addDetail("className", "className:");
        styleClassDetail = addDetail("styleClass", "styleClass:");
        pseudoClassStateDetail = addDetail(null, "pseudoClassState:");
        visibleDetail = addDetail("visible", "visible:");
        managedDetail = addDetail("managed", "managed:");
        layoutBoundsDetail = addDetail("layoutBounds", "layoutBounds:", LabelType.LAYOUT_BOUNDS);
        effectDetail = addDetail("effect", "effect:");
        opacityDetail = addDetail("opacity", "opacity:");
        clipDetail = addDetail("clip", "clip:");
        transformsDetail = addDetail("transforms", "transforms:");
        scaleXYDetail = addDetail("scaleX", "scaleX/Y:");
        rotateDetail = addDetail("rotate", "rotate:");
        layoutXYDetail = addDetail("layoutX", "layoutX/Y:");
        translateXYDetail = addDetail("translateX", "translateX/Y:");
        final Rectangle boundsInParentIcon = new Rectangle(12, 12);
        boundsInParentIcon.setFill(Color.YELLOW);
        boundsInParentIcon.setOpacity(.5);
        boundsInParentDetail = addDetail("boundsInParent", "boundsInParent:", LabelType.BOUNDS_PARENT);
        resizableDetail = addDetail(null, "resizable:");
        contentBiasDetail = addDetail(null, "contentBias:");

        baselineDetail = addDetail(null, "baselineOffset:", LabelType.BASELINE);
        minSizeDetail = addDetail(null, "minWidth(h)/Height(w):");
        prefSizeDetail = addDetail(null, "prefWidth(h)/Height(w):");
        maxSizeDetail = addDetail(null, "maxWidth(h)/Height(w):");
        constraintsDetail = addDetail(null, "layout constraints:", ValueType.CONSTRAINTS);
    }

    @Override void setTarget(final Object value) {
        final Node old = (Node) getTarget();
        if (old != null) {
            old.getTransforms().removeListener(transformListener);
        }
        super.setTarget(value);

        final Node node = (Node) value;

        fillPseudoStates(node);

        if (node != null) {
            node.getTransforms().addListener(transformListener);
        }
    }

//    private Field[] getDeclaredFields(final Object node) {
//        final List<Field> fields = new ArrayList<Field>();
//        Class<?> classType = node.getClass();
//        do {
//            fields.addAll(Arrays.asList(classType.getDeclaredFields()));
//            classType = classType.getSuperclass();
//        } while (classType != null);
//        return fields.toArray(new Field[fields.size()]);
//    }

    private void fillPseudoStates(final Node node) {
//        pseudoStates.clear();
//        pseudoClassStates = node == null ? null : node.getPseudoClassStates();
//        if (node != null) {
//            PseudoClass.States states = node.getPseudoClassStates();
//            
//            final Field[] fields = getDeclaredFields(node);
//            for (int i = 0; i < fields.length; i++) {
//                final String name = fields[i].getName();
//                if (name.endsWith("_PSEUDOCLASS_STATE")) {
//                    try {
//                        fields[i].setAccessible(true);
//                        final long lvalue = fields[i].getLong(node);
//                        final String pseudoClass = name.substring(0, name.indexOf("_PSEUDOCLASS_STATE")).toLowerCase();
//                        pseudoStates.put(lvalue, pseudoClass);
//                    } catch (final IllegalArgumentException e) {
//                        ScenicViewExceptionLogger.submitException(e);
//                    } catch (final IllegalAccessException e) {
//                        ScenicViewExceptionLogger.submitException(e);
//                    }
//                }
//            }
//        }
    }

    @Override protected void updateAllDetails() {
        final Node node = (Node) getTarget();

        // No property change events on these
        resizableDetail.setValue(node != null ? Boolean.toString(node.isResizable()) : "-");
        // boolean showResizable = node != null && node.isResizable();
        resizableDetail.setIsDefault(node == null);

        Orientation bias = null;
        if (node != null) {
            bias = node.getContentBias();
            contentBiasDetail.setValue(bias != null ? bias.toString() : "none");
        } else {
            contentBiasDetail.setValue("-");
        }
        contentBiasDetail.setIsDefault(node == null || node.getContentBias() == null);

        baselineDetail.setValue(node != null ? f.format(node.getBaselineOffset()) : "-");
        baselineDetail.setIsDefault(node == null);

        if (node != null) {
            double minw = 0;
            double minh = 0;
            double prefw = 0;
            double prefh = 0;
            double maxw = 0;
            double maxh = 0;

            if (bias == null) {
                minSizeDetail.setLabel("minWidth(-1)/minHeight(-1):");
                prefSizeDetail.setLabel("prefWidth(-1)/prefHeight(-1):");
                maxSizeDetail.setLabel("maxWidth(-1)/maxHeight(-1):");
                minw = node.minWidth(-1);
                minh = node.minHeight(-1);
                prefw = node.prefWidth(-1);
                prefh = node.prefHeight(-1);
                maxw = node.maxWidth(-1);
                maxh = node.maxHeight(-1);
            } else if (bias == Orientation.HORIZONTAL) {
                minSizeDetail.setLabel("minWidth(-1)/minHeight(w):");
                prefSizeDetail.setLabel("prefWidth(-1)/prefHeight(w):");
                maxSizeDetail.setLabel("maxWidth(-1)/maxHeight(w):");
                minw = node.minWidth(-1);
                minh = node.minHeight(minw);
                prefw = node.prefWidth(-1);
                prefh = node.prefHeight(prefw);
                maxw = node.maxWidth(-1);
                maxh = node.maxHeight(maxw);
            } else { // VERTICAL
                minSizeDetail.setLabel("minWidth(h)/minHeight(-1):");
                prefSizeDetail.setLabel("prefWidth(h)/prefHeight(-1):");
                maxSizeDetail.setLabel("maxWidth(h)/maxHeight(-1):");
                minh = node.minHeight(-1);
                minw = node.minWidth(minh);
                prefh = node.prefHeight(-1);
                prefw = node.prefWidth(prefh);
                maxh = node.maxHeight(-1);
                maxw = node.maxWidth(maxh);
            }

            minSizeDetail.setValue(f.format(minw) + " x " + f.format(minh));
            prefSizeDetail.setValue(f.format(prefw) + " x " + f.format(prefh));
            maxSizeDetail.setValue((maxw >= Double.MAX_VALUE ? "MAXVALUE" : f.format(maxw)) + " x " + (maxh >= Double.MAX_VALUE ? "MAXVALUE" : f.format(maxh)));
        } else {
            minSizeDetail.setValue("-");
            prefSizeDetail.setValue("-");
            maxSizeDetail.setValue("-");
        }
        final boolean fade = node == null || !node.isResizable();
        minSizeDetail.setIsDefault(fade);
        prefSizeDetail.setIsDefault(fade);
        maxSizeDetail.setIsDefault(fade);
        @SuppressWarnings("rawtypes") final ObservableMap map = node != null && node.hasProperties() ? node.getProperties() : null;
        constraintsDetail.setValue(ConnectorUtils.serializePropertyMap(map));
        constraintsDetail.setIsDefault(map == null || map.size() == 0);

        updateDetail("*");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Node node = (Node) getTarget();
        if (all && node != null) {
            nodeClassName.setValue(node.getClass().getName());
        }

        if (node != null) {
            final Set<PseudoClass> states = node.getPseudoClassStates();
            
            final String state = getPseudoState(states);
            if (!state.equals(pseudoClassStateDetail.getValue())) {
                pseudoClassStateDetail.setValue(getPseudoState(states));
                if (!all) {
                    pseudoClassStateDetail.updated();
                }
            }
        }

        if (all || propertyName.equals("styleClass")) {
            styleClassDetail.setValue(node != null ? "\"" + node.getStyleClass().toString() + "\"" : "-");
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
            visibleDetail.setValue(node != null ? Boolean.toString(node.isVisible()) : "-");
            visibleDetail.setIsDefault(node == null || node.isVisible());
            visibleDetail.setSimpleProperty((node != null) ? node.visibleProperty() : null);
            if (!all)
                visibleDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("managed")) {
            managedDetail.setValue(node != null ? Boolean.toString(node.isManaged()) : "-");
            managedDetail.setIsDefault(node == null || node.isManaged());
            managedDetail.setSimpleProperty((node != null) ? node.managedProperty() : null);
            if (!all)
                managedDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("opacity")) {
            opacityDetail.setValue(node != null ? node.getOpacity() * 100 + "%" : "-");
            opacityDetail.setIsDefault(node == null || node.getOpacity() == 1.0);
            opacityDetail.setSimpleProperty((node != null) ? node.opacityProperty() : null);
            /**
             * Include the slider
             */
            if (opacityDetail.serializer != null) {
                ((SimpleSerializer) opacityDetail.serializer).setMaxValue(1);
                ((SimpleSerializer) opacityDetail.serializer).setMinValue(0);
            }
            opacityDetail.setSerializer(opacityDetail.serializer);
            if (!all)
                opacityDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("effect")) {
            final Effect effect = node != null ? node.getEffect() : null;
            effectDetail.setValue(effect != null ? effect.getClass().getSimpleName() : "-");
            effectDetail.setIsDefault(effect == null);
            if (!all)
                effectDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("layoutBounds")) {
            layoutBoundsDetail.setValue(node != null ? boundsToString(node.getLayoutBounds()) : "-");
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
                    str.append(ConnectorUtils.transformToString(tx));
                    str.append(" ");
                }
                txstr = str.toString();
            }
            transformsDetail.setValue(txstr);
            transformsDetail.setIsDefault(node == null || node.getTransforms().size() == 0);
            if (!all)
                transformsDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("scaleX") || propertyName.equals("scaleY")) {
            scaleXYDetail.setValue(node != null ? f.format(node.getScaleX()) + " x " + f.format(node.getScaleY()) : "-");
            scaleXYDetail.setIsDefault(node == null || (node.getScaleX() == 1.0 && node.getScaleY() == 1.0));
            scaleXYDetail.setSimpleSizeProperty(node != null ? node.scaleXProperty() : null, node != null ? node.scaleYProperty() : null);
            if (!all)
                scaleXYDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("rotate")) {
            rotateDetail.setValue(node != null ? f.format(node.getRotate()) : "-");
            rotateDetail.setIsDefault(node == null || node.getRotate() == 0);
            rotateDetail.setSimpleProperty((node != null) ? node.rotateProperty() : null);
            if (!all)
                rotateDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("clip")) {
            final Node clip = node != null ? node.getClip() : null;
            clipDetail.setValue(node != null ? clip != null ? (clip.getClass().getSimpleName() + ":" + boundsToString(clip.getBoundsInLocal())) : "null" : "-");
            clipDetail.setIsDefault(clip == null);
            if (!all)
                clipDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("layoutX") || propertyName.equals("layoutY")) {
            layoutXYDetail.setValue(node != null ? f.format(node.getLayoutX()) + "," + f.format(node.getLayoutY()) : "-");
            layoutXYDetail.setIsDefault(node == null || (node.getLayoutX() == 0 && node.getLayoutY() == 0));
            layoutXYDetail.setSimpleSizeProperty(node != null ? node.layoutXProperty() : null, node != null ? node.layoutYProperty() : null);
            if (!all)
                return;
        }
        if (all || propertyName.equals("translateX") || propertyName.equals("translateY")) {
            translateXYDetail.setValue(node != null ? f.format(node.getTranslateX()) + "," + f.format(node.getTranslateY()) : "-");
            translateXYDetail.setIsDefault(node == null || (node.getTranslateX() == 0 && node.getTranslateY() == 0));
            translateXYDetail.setSimpleSizeProperty(node != null ? node.translateXProperty() : null, node != null ? node.translateYProperty() : null);
            if (!all)
                translateXYDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("boundsInParent")) {
            final String bounds = node != null ? boundsToString(node.getBoundsInParent()) : "-";
            // In some situations this variable is updated without reason, we
            // check if there
            // was a real update
            if (!bounds.equals(boundsInParentDetail.getValue())) {
                boundsInParentDetail.setValue(bounds);
                boundsInParentDetail.setIsDefault(node == null);
                if (!all)
                    boundsInParentDetail.updated();
            }
        }
        if (all)
            sendAllDetails();
    }

    private String getPseudoState(final Set<PseudoClass> states) {
        final StringBuilder sb = new StringBuilder();
        
        int i = 0;
        for (PseudoClass pseudoClass : states) {
            if (pseudoClass != null && ! pseudoClass.getPseudoClassName().isEmpty()) {
                if (i++ != 0) {
                    sb.append(',');
                }
                sb.append(pseudoClass.getPseudoClassName());
            }
        }
        return sb.length() != 0 ? sb.toString() : Detail.EMPTY_DETAIL;
    }
}
