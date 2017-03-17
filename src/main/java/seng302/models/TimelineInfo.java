package seng302.models;

import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;


/**
 * Created by zyt10 on 17/03/17.
 * this class is literally just to associate a timeline with a DoubleProperty x and y
 */
public class TimelineInfo {
    private Timeline timeline;
    private DoubleProperty x;
    private DoubleProperty y;

    public TimelineInfo(Timeline timeline, DoubleProperty x, DoubleProperty y) {
        this.timeline = timeline;
        this.x = x;
        this.y = y;
    }

    public Timeline getTimeline() {
        return timeline;
    }
    public DoubleProperty getX() {
        return x;
    }
    public DoubleProperty getY() {
        return y;
    }
}
