package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import com.javafx.experiments.scenicview.connector.SVAnimation;

public class AnimationsPane extends VBox {

    public AnimationsPane() {
        // TODO Auto-generated constructor stub
    }

    public void update(final List<SVAnimation> animations) {
        getChildren().clear();
        for (final Iterator iterator = animations.iterator(); iterator.hasNext();) {
            final SVAnimation svAnimation = (SVAnimation) iterator.next();
            final Label label = new Label(svAnimation.toString());
            label.setWrapText(true);
            getChildren().add(label);
        }
    }

}
