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
