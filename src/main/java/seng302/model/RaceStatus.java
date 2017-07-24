package seng302.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Class for storing race data that does not relate to specific vessels or marks such as time or wind
 */
public class RaceStatus {
    double windSpeed;
    double windDirection;
    long raceTime;

}
