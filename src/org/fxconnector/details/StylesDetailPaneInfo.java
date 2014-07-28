/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fxconnector.details;

import com.sun.javafx.css.Style;

import java.lang.reflect.*;
import java.util.*;

import javafx.collections.ObservableMap;


import javafx.scene.Node;

import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.remote.FXConnectorFactory;
import org.fxconnector.remote.util.ScenicViewExceptionLogger;

import javafx.css.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
class StylesDetailPaneInfo extends DetailPaneInfo {

    StylesDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.STYLES);
    }

    Map<String, Detail> stylesPropertiesDetails;
    Map<String, CssMetaData> styles;

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
        styles = new TreeMap<String, CssMetaData>();
        stylesPropertiesDetails = new TreeMap<String, Detail>();
        details.clear();
        if (node != null) {
            final List<CssMetaData<? extends Styleable, ?>> list = node.getCssMetaData();
            for (final Iterator<CssMetaData<? extends Styleable, ?>> iterator = list.iterator(); iterator.hasNext();) {
                final CssMetaData cssMetaData = iterator.next();
                final String type = cssMetaData.getProperty();
                styles.put(cssMetaData.getProperty(), cssMetaData);
                stylesPropertiesDetails.put(type, addDetail(type, type + ":"));
            }
        }
        updateAllDetails();
    }

    @SuppressWarnings("deprecation") @Override protected void updateAllDetails() {
        final Node value = (Node) getTarget();
//        final CssStyleHelper helper = value.impl_getStyleHelper();//StyleManager.getInstance().getStyleHelper(value);
        value.getCssMetaData();

        if (stylesPropertiesDetails != null) {
            for (final Iterator<String> iterator = stylesPropertiesDetails.keySet().iterator(); iterator.hasNext();) {
                final String propertyName = iterator.next();
                final Detail detail = stylesPropertiesDetails.get(propertyName);
                final CssMetaData cssMetaData = styles.get(propertyName);
                StyleOrigin origin = null;
                String style = null;
                try {
//                    long[] pseudoClassStateLongArray = (long[]) invoke(Class.forName("com.sun.javafx.css.StyleHelper"), "getPseudoClassState", helper);
                    final Set<PseudoClass> pseudoClassState = value.getPseudoClassStates();
                            
                    StyleableProperty styleableProperty = cssMetaData.getStyleableProperty(value);
                    origin = styleableProperty.getStyleOrigin();
//                    origin = (StyleOrigin) invoke(Class.forName("javafx.css.StyleableProperty"), "getStyleOrigin", cssMetaData.getStyleableProperty(value));
                    
                    ObservableMap<StyleableProperty<?>, List<Style>> styleMap = value.impl_getStyleMap();
                    org.fxconnector.Debugger.debug(styleMap);
                    if (styleMap != null && styleMap.containsKey(styleableProperty)) {
                        List<Style> styles = value.impl_getStyleMap().get(styleableProperty);
                        org.fxconnector.Debugger.debug("styles size: " + styles.size());
                        if (styles.size() == 1) {
                            style = styles.get(0).toString();
                        }
                    }
                    
//                    if (helper != null) {
//                        final Object returned = invoke(StyleHelper.class, "getStyle", helper, new Class[] { Node.class, String.class, long[].class, Map.class }, new Object[] { value, cssMetaData.getProperty(), pseudoClassStateLongArray, new HashMap() });
//                        if (returned != null) {
//                            style = invoke(returned.getClass(), "getStyle", returned).toString();
//                        }
//                    }

                } catch (final Exception e) {
                    ScenicViewExceptionLogger.submitException(e);
                }

                detail.setValue((origin != null ? origin.toString() : "NONE") + (style != null ? ("\n" + style) : ""));
                detail.setDefault(origin == null && style == null);
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
