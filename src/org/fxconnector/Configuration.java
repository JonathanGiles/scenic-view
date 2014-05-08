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
package org.fxconnector;

import java.io.Serializable;

public final class Configuration implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -9086033764395754103L;

    private boolean showBounds;

    private boolean showBaseline;

    private boolean showRuler;

    private int rulerSeparation;

    private String rulerColor = "000000";

    private boolean eventLogEnabled;

    private boolean autoRefreshStyles;

    private boolean ignoreMouseTransparent;

    private boolean collapseControls;

    private boolean collapseContentControls;

    private boolean autoRefreshSceneGraph;

    private boolean visibilityFilteringActive;

    private boolean CSSPropertiesDetail;

    /**
     * I'm not totally sure about this...
     */
    private boolean componentSelectOnClick;

    private boolean registerShortcuts;

    public boolean isShowBounds() {
        return showBounds;
    }

    public void setShowBounds(final boolean showBounds) {
        this.showBounds = showBounds;
    }

    public boolean isShowBaseline() {
        return showBaseline;
    }

    public void setShowBaseline(final boolean showBaseline) {
        this.showBaseline = showBaseline;
    }

    public boolean isEventLogEnabled() {
        return eventLogEnabled;
    }

    public void setEventLogEnabled(final boolean eventLogEnabled) {
        this.eventLogEnabled = eventLogEnabled;
    }

    public boolean isShowRuler() {
        return showRuler;
    }

    public void setShowRuler(final boolean showRuler) {
        this.showRuler = showRuler;
    }

    public int getRulerSeparation() {
        return rulerSeparation;
    }

    public void setRulerSeparation(final int rulerSeparation) {
        this.rulerSeparation = rulerSeparation;
    }

    public boolean isAutoRefreshStyles() {
        return autoRefreshStyles;
    }

    public void setAutoRefreshStyles(final boolean autoRefreshStyles) {
        this.autoRefreshStyles = autoRefreshStyles;
    }

    public boolean isIgnoreMouseTransparent() {
        return ignoreMouseTransparent;
    }

    public void setIgnoreMouseTransparent(final boolean ignoreMouseTransparent) {
        this.ignoreMouseTransparent = ignoreMouseTransparent;
    }

    public boolean isCollapseControls() {
        return collapseControls;
    }

    public void setCollapseControls(final boolean collapseControls) {
        this.collapseControls = collapseControls;
    }

    public boolean isCollapseContentControls() {
        return collapseContentControls;
    }

    public void setCollapseContentControls(final boolean collapseContentControls) {
        this.collapseContentControls = collapseContentControls;
    }

    public boolean isAutoRefreshSceneGraph() {
        return autoRefreshSceneGraph;
    }

    public void setAutoRefreshSceneGraph(final boolean autoRefreshSceneGraph) {
        this.autoRefreshSceneGraph = autoRefreshSceneGraph;
    }

    public boolean isVisibilityFilteringActive() {
        return visibilityFilteringActive;
    }

    public void setVisibilityFilteringActive(final boolean visibilityFilteringActive) {
        this.visibilityFilteringActive = visibilityFilteringActive;
    }

    public boolean isComponentSelectOnClick() {
        return componentSelectOnClick;
    }

    public void setComponentSelectOnClick(final boolean componentSelectOnClick) {
        this.componentSelectOnClick = componentSelectOnClick;
    }

    @Override public String toString() {
        return "Configuration [showBounds=" + showBounds + ", showBaseline=" + showBaseline + ", showRuler=" + showRuler + ", rulerSeparation=" + rulerSeparation + ", eventLogEnabled=" + eventLogEnabled + ", autoRefreshStyles=" + autoRefreshStyles + ", ignoreMouseTransparent=" + ignoreMouseTransparent + ", collapseControls=" + collapseControls + ", collapseContentControls=" + collapseContentControls + ", autoRefreshSceneGraph=" + autoRefreshSceneGraph + ", visibilityFilteringActive=" + visibilityFilteringActive + ", componentSelectOnClick=" + componentSelectOnClick + "]";
    }

    public boolean isCSSPropertiesDetail() {
        return CSSPropertiesDetail;
    }

    public void setCSSPropertiesDetail(final boolean cSSPropertiesDetail) {
        CSSPropertiesDetail = cSSPropertiesDetail;
    }

    public String getRulerColor() {
        return rulerColor;
    }

    public void setRulerColor(final String rulerColor) {
        this.rulerColor = rulerColor;
    }

    public void setRegisterShortcuts(final boolean newValue) {
        this.registerShortcuts = newValue;
    }

    public boolean isRegisterShortcuts() {
        return registerShortcuts;
    }

}
