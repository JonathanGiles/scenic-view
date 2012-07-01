package com.javafx.experiments.scenicview.connector.event;

import java.util.List;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.details.*;

public class DetailsEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -6272264701263599805L;
    private final DetailPaneType paneType;
    private final String paneName;
    final List<Detail> details;

    public DetailsEvent(final SVEventType type, final StageID id, final DetailPaneType dtype, final String paneName, final List<Detail> details) {
        super(type, id);
        this.paneType = dtype;
        this.paneName = paneName;
        this.details = details;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public DetailPaneType getPaneType() {
        return paneType;
    }

    public String getPaneName() {
        return paneName;
    }

}
