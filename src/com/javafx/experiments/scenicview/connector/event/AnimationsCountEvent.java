package com.javafx.experiments.scenicview.connector.event;

import java.util.*;

import com.javafx.experiments.scenicview.connector.*;

public class AnimationsCountEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 1245351064842340102L;
    List<SVAnimation> animations = new ArrayList<SVAnimation>();

    public AnimationsCountEvent(final StageID id, final List<SVAnimation> animations) {
        super(SVEventType.ANIMATIONS_UPDATED, id);
        this.animations = animations;
    }

    public List<SVAnimation> getAnimations() {
        return animations;
    }

}
