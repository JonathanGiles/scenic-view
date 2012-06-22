package com.javafx.experiments.scenicview.connector;

import java.io.Serializable;

public final class StageID implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1091256426733557091L;
    private final int appID;
    private final int stageID;
    
    public StageID(final int appID, final int stageID) {
        this.appID = appID;
        this.stageID = stageID;
    }

    public int getAppID() {
        return appID;
    }

    public int getStageID() {
        return stageID;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + appID;
        result = prime * result + stageID;
        return result;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StageID other = (StageID) obj;
        if (appID != other.appID)
            return false;
        if (stageID != other.stageID)
            return false;
        return true;
    }

}
