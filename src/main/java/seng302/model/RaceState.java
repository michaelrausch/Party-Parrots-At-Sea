package seng302.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import seng302.model.stream.parser.RaceStartData;
import seng302.model.stream.parser.RaceStatusData;

/**
 * Class for storing race data that does not relate to specific vessels or marks such as time or wind.
 * Calculates the state of critical race attributes when relevant data is added.
 */
public class RaceState {

//    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private double windSpeed;
    private double windDirection;
    private long raceTime;
    private long expectedStartTime;
    private boolean isRaceStarted = false;
//    long timeTillStart;

    public RaceState() {
    }

    public void updateState (RaceStatusData data) {
        this.windSpeed = data.getWindSpeed();
        this.windDirection = data.getWindDirection();
        this.raceTime = data.getCurrentTime();
        this.expectedStartTime = data.getExpectedStartTime();
        this.isRaceStarted = data.isRaceStarted();
    }

    public void setTimeZone (TimeZone timeZone) {
        DATE_TIME_FORMAT.setTimeZone(timeZone);
    }

    public void updateState (RaceStartData data) {
//        this.timeTillStart = data.getRaceStartTime();
        System.out.println(data.getRaceStartTime());
    }

    public String getRaceTimeStr () {
        return DATE_TIME_FORMAT.format(raceTime);
    }

    public long getTimeTillStart () {
        return (expectedStartTime - raceTime) / 1000;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public long getRaceTime() {
        return raceTime;
    }

    public long getExpectedStartTime() {
        return expectedStartTime;
    }

    public boolean isRaceStarted () {
        return isRaceStarted;
    }
}
