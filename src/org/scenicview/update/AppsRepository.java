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
package org.scenicview.update;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import org.fxconnector.AppController;
import org.fxconnector.StageController;
import org.scenicview.ScenicViewGui;
import org.scenicview.utils.ScenicViewDebug;

public final class AppsRepository {
    
    private final List<AppController> apps = new ArrayList<AppController>();
    private final ScenicViewGui scenicView;
    
    public AppsRepository(ScenicViewGui scenicView) {
        this.scenicView = scenicView;
    }
    
    public final List<AppController> getApps() {
        return apps;
    }

    int findAppControllerIndex(final int appID) {
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getID() == appID) {
                return i;
            }
        }
        return -1;
    }

    int findStageIndex(final List<StageController> stages, final int stageID) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getID().getStageID() == stageID) {
                return i;
            }
        }
        return -1;
    }

    public void stageRemoved(final StageController stageController) {
        Platform.runLater(() -> {
            dumpStatus("stageRemovedStart", stageController.getID().getStageID());
            final List<StageController> stages = apps.get(findAppControllerIndex(stageController.getID().getAppID())).getStages();
            // Remove and close
            stages.remove(findStageIndex(stages, stageController.getID().getStageID())).close();
            scenicView.removeStage(stageController);
            dumpStatus("stageRemovedStop", stageController.getID().getStageID());
        });
    }

    public void stageAdded(final StageController stageController) {
        Platform.runLater(() -> {
            dumpStatus("stageAddedStart", stageController.getID().getStageID());
            apps.get(findAppControllerIndex(stageController.getID().getAppID())).getStages().add(stageController);
            stageController.setEventDispatcher(scenicView.getStageModelListener());
            scenicView.configurationUpdated();
            dumpStatus("stageAddedStop", stageController.getID().getStageID());
        });
    }

    public void appRemoved(final AppController appController) {
        Platform.runLater(() -> {
            dumpStatus("appRemovedStart", appController.getID());
            // Remove and close
            apps.remove(findAppControllerIndex(appController.getID())).close();
            scenicView.removeApp(appController);
            dumpStatus("appRemovedStop", appController.getID());
        });
    }

    public void appAdded(final AppController appController) {
        Platform.runLater(() -> {
            dumpStatus("appAddedStart", appController.getID());
            if (!apps.contains(appController)) {
                if (apps.isEmpty() && !appController.getStages().isEmpty()) {
                    scenicView.setActiveStage(appController.getStages().get(0));
                }
                apps.add(appController);
            }
            final List<StageController> stages = appController.getStages();
            for (int j = 0; j < stages.size(); j++) {
                stages.get(j).setEventDispatcher(scenicView.getStageModelListener());
            }
            scenicView.configurationUpdated();
            dumpStatus("appAddedStop", appController.getID());
        });
    }

    private void dumpStatus(final String operation, final int id) {
        ScenicViewDebug.print(operation + ":" + id);
        for (int i = 0; i < apps.size(); i++) {
            ScenicViewDebug.print("App:" + apps.get(i).getID());
            final List<StageController> scs = apps.get(i).getStages();
            for (int j = 0; j < scs.size(); j++) {
                ScenicViewDebug.print("\tStage:" + scs.get(j).getID().getStageID());
            }
        }
    }
}
