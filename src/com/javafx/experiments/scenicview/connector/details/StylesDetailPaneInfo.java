package com.javafx.experiments.scenicview.connector.details;

import java.lang.reflect.*;
import java.util.*;

import javafx.scene.Node;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.FXConnectorEventDispatcher;
import com.sun.javafx.css.*;
import com.sun.javafx.css.Stylesheet.Origin;

@SuppressWarnings({ "unchecked", "rawtypes" })
class StylesDetailPaneInfo extends DetailPaneInfo {

    StylesDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.STYLES);
    }

    Map<String, Detail> stylesPropertiesDetails;
    Map<String, StyleableProperty> styles;

    @Override protected String getPaneName() {
        return "Styles Details";
    }

    @Override Class<? extends Node> getTargetClass() {
        return null;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate != null;
    }

    @Override protected void createDetails() {
        // Nothing to do
    }

    @Override public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            createPropertiesPanel();
        }

    }

    private void createPropertiesPanel() {
        final Node node = (Node) getTarget();
        styles = new TreeMap<String, StyleableProperty>();
        stylesPropertiesDetails = new TreeMap<String, Detail>();
        details.clear();
        if (node != null) {
            final List<StyleableProperty> list = StyleableProperty.getStyleables(node);
            for (final Iterator<StyleableProperty> iterator = list.iterator(); iterator.hasNext();) {
                final StyleableProperty styleableProperty = iterator.next();
                final String type = styleableProperty.getProperty();
                styles.put(styleableProperty.getProperty(), styleableProperty);
                stylesPropertiesDetails.put(type, addDetail(type, type + ":"));
            }
        }
        updateAllDetails();
    }

    @SuppressWarnings("deprecation") @Override protected void updateAllDetails() {
        final Node value = (Node) getTarget();
        final StyleHelper helper = StyleManager.getInstance().getStyleHelper(value);

        if (stylesPropertiesDetails != null) {
            for (final Iterator<String> iterator = stylesPropertiesDetails.keySet().iterator(); iterator.hasNext();) {
                final String propertyName = iterator.next();
                final Detail detail = stylesPropertiesDetails.get(propertyName);
                final StyleableProperty styleableProperty = styles.get(propertyName);
                Origin origin = null;
                String style = null;
                try {
                    origin = (Origin) invoke(Class.forName("com.sun.javafx.css.Property"), "getOrigin", styleableProperty.getWritableValue(value));
                    if (helper != null) {
                        final Object returned = invoke(StyleHelper.class, "getStyle", helper, new Class[] { Node.class, String.class, long.class, Map.class }, new Object[] { value, styleableProperty.getProperty(), value.impl_getPseudoClassState(), new HashMap() });
                        if (returned != null) {
                            style = invoke(returned.getClass(), "getStyle", returned).toString();
                        }
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                }

                detail.setValue((origin != null ? origin.toString() : "NONE") + (style != null ? ("\n" + style) : ""));
                detail.setSimpleProperty(null);
                detail.unavailableEdition(Detail.STATUS_NOT_SUPPORTED);

            }
        }
        sendAllDetails();
    }

    private Object invoke(final Class classData, final String methodName, final Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        return invoke(classData, methodName, object, null, null);
    }

    private Object invoke(final Class classData, final String methodName, final Object object, final Class[] signature, final Object[] values) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        final Method m = classData.getDeclaredMethod(methodName, signature);
        m.setAccessible(true);
        return m.invoke(object, values);
    }

    @Override protected void updateDetail(final String propertyName) {

    }

}
