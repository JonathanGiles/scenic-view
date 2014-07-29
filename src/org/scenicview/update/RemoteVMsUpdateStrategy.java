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

import org.fxconnector.AppController;
import org.fxconnector.StageController;

import java.util.ArrayList;
import java.util.List;

import org.fxconnector.helper.WorkerThread;
import org.fxconnector.remote.FXConnector;
import org.scenicview.utils.ExceptionLogger;

public class RemoteVMsUpdateStrategy extends WorkerThread implements UpdateStrategy {

    private boolean first = true;
    private FXConnector connector;
    
    AppsRepository repository;
    List<AppController> previous = new ArrayList<AppController>();

    public RemoteVMsUpdateStrategy() {
        super(RemoteVMsUpdateStrategy.class.getName(), 500);
    }

    private List<AppController> getActiveApps() {
        if (first) {
            /**
             * Wait for the server to startup
             */
            first = false;
            while (connector == null) {
                try {
                    Thread.sleep(50);
                } catch (final InterruptedException e) {
                    ExceptionLogger.submitException(e);
                }
            }
        }

        return connector.connect();
    }

    @Override public void finish() {
        super.finish();
        connector.close();
        System.exit(0);
    }

    public void setFXConnector(final FXConnector connector) {
        this.connector = connector;
    }
    
    @Override public void start(final AppsRepository repository) {
        this.repository = repository;
        start();
    }

    @Override protected void work() {
        boolean modifications = false;
        final List<AppController> actualApps = getActiveApps();
        
        final List<StageController> unused = new ArrayList<StageController>();
        for (int i = 0; i < actualApps.size(); i++) {
            unused.addAll(actualApps.get(i).getStages());
        }
        
        /**
         * First check new apps
         */
        for (int i = 0; i < actualApps.size(); i++) {
            if (isAppOnArray(actualApps.get(i), previous) == -1) {
                repository.appAdded(actualApps.get(i));
                unused.removeAll(actualApps.get(i).getStages());
                modifications = true;
            }
        }
        
        /**
         * Then check remove apps
         */
        for (int i = 0; i < previous.size(); i++) {
            if (isAppOnArray(previous.get(i), actualApps) == -1) {
                repository.appRemoved(previous.get(i));
                modifications = true;
            }
        }
        
        /**
         * Then check added/removed Stages
         */
        for (int i = 0; i < actualApps.size(); i++) {
            if (isAppOnArray(actualApps.get(i), previous) != -1) {
                final List<StageController> stages = actualApps.get(i).getStages();
                final List<StageController> previousStages = previous.get(isAppOnArray(actualApps.get(i), previous)).getStages();
                for (int j = 0; j < stages.size(); j++) {
                    if (!isStageOnArray(stages.get(j), previousStages)) {
                        repository.stageAdded(stages.get(j));
                        unused.remove(stages.get(j));
                        modifications = true;
                    }
                }
                for (int j = 0; j < previousStages.size(); j++) {
                    if (!isStageOnArray(previousStages.get(j), stages)) {
                        repository.stageRemoved(previousStages.get(j));
                        modifications = true;
                    }
                }
            }
        }
        if (modifications) {
            previous = actualApps;
        }
    }

    boolean isStageOnArray(final StageController controller, final List<StageController> stages) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getID().getStageID() == controller.getID().getStageID()) {
                return true;
            }
        }
        return false;
    }

    int isAppOnArray(final AppController controller, final List<AppController> apps) {
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getID() == controller.getID()) {
                return i;
            }
        }
        return -1;
    }
}
