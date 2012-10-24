package com.javafx.experiments.scenicview.connector.details;

import java.lang.reflect.Method;
import java.util.*;

import javafx.beans.value.WritableValue;
import javafx.scene.Node;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;
import com.sun.javafx.css.*;
import com.sun.javafx.css.Stylesheet.Origin;

@SuppressWarnings({ "unchecked" })
public class StylesDetailPaneInfo extends DetailPaneInfo {

    public StylesDetailPaneInfo(final AppEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.STYLES);
    }

    Map<String, Detail> stylesPropertiesDetails;
    Map<String, StyleableProperty> styles;

    @Override protected String getPaneName() {
        return "Full Properties Details";
    }

    @Override public Class<? extends Node> getTargetClass() {
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
        details.clear();
        if (node != null) {
            final List<StyleableProperty> list = StyleableProperty.getStyleables(node);
            for (final Iterator<StyleableProperty> iterator = list.iterator(); iterator.hasNext();) {
                final StyleableProperty styleableProperty = iterator.next();
                final WritableValue wvalue = styleableProperty.getWritableValue(node);
                styles.put(styleableProperty.getProperty(), styleableProperty);
            }
        }

        stylesPropertiesDetails = new TreeMap<String, Detail>();

        for (final Iterator<String> iterator = styles.keySet().iterator(); iterator.hasNext();) {
            final String type = iterator.next();
            final StyleableProperty styleableProperty = styles.get(type);
            String style = null;
            Origin origin = null;
            if (styleableProperty != null) {
                style = styleableProperty.getProperty();

                try {
                    final Class classData = Class.forName("com.sun.javafx.css.Property");
                    final Method m = classData.getDeclaredMethod("getOrigin", null);
                    m.setAccessible(true);
                    origin = (Origin) m.invoke(styleableProperty.getWritableValue(node), null);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            stylesPropertiesDetails.put(type, addDetail(type, type + ":"));
        }
        updateAllDetails();
    }

    @Override protected void updateAllDetails() {
        if (stylesPropertiesDetails != null) {
            for (final Iterator<String> iterator = stylesPropertiesDetails.keySet().iterator(); iterator.hasNext();) {
                final String propertyName = iterator.next();
                final Detail detail = stylesPropertiesDetails.get(propertyName);
                final StyleableProperty styleableProperty = styles.get(propertyName);
                Origin origin = null;
                String style = null;
                if (styleableProperty != null) {

                    try {
                        final Class classData = Class.forName("com.sun.javafx.css.Property");
                        final Method m = classData.getDeclaredMethod("getOrigin", null);
                        m.setAccessible(true);
                        origin = (Origin) m.invoke(styleableProperty.getWritableValue((Node) getTarget()), null);
                        final Node value = (Node) getTarget();
                        final StyleHelper helper = StyleManager.getInstance().getStyleHelper(value);
                        if (helper != null) {
                            try {
                                final Method m2 = StyleHelper.class.getDeclaredMethod("getStyle", Node.class, String.class, long.class, Map.class);
                                m2.setAccessible(true);
                                final Object returned = m2.invoke(helper, value, styleableProperty.getProperty(), value.impl_getPseudoClassState(), new HashMap());
                                if (returned != null) {
                                    final Method m3 = returned.getClass().getDeclaredMethod("getStyle", null);
                                    m3.setAccessible(true);
                                    style = m3.invoke(returned, null).toString();
                                }
                            } catch (final Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                detail.setValue((origin != null ? origin.toString() : "NONE") + (style != null ? ("\n" + style) : ""));
                detail.setSimpleProperty(null);
                detail.unavailableEdition(Detail.STATUS_NOT_SUPPORTED);

            }
        }
        sendAllDetails();
    }

    @Override protected void updateDetail(final String propertyName) {

    }

}
