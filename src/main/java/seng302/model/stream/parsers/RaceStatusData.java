package seng302.model.stream.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores parsed data from race status packets
 */
public class RaceStatusData {

    //CONVERSION CONSTANTS
    private static final double WIND_DIR_FACTOR = 0x4000 / 90; //0x4000 is 90 degrees
    private static final double MS_TO_KNOTS = 1.94384;

    private double windDirection;
    private double windSpeed;
    private boolean raceStarted = false;
    private long currentTime;
    private long expectedStartTime;
    List<long[]> boatData = new ArrayList<>();

    public RaceStatusData(
        long windDir, long rawWindSpeed, int raceStatus, long currentTime, long expectedStartTime) {

        windDirection = windDir / WIND_DIR_FACTOR;
        windSpeed = rawWindSpeed / 1000 * MS_TO_KNOTS;
        raceStarted = raceStatus == 3;
        this.currentTime = currentTime;
        this.expectedStartTime = expectedStartTime;
    }

    public void addBoatData (long boatID, long estTimeToNextMark, long estTimeToFinish, int leg) {
        boatData.add(new long[] {boatID, estTimeToNextMark, estTimeToFinish, leg});
    }

    public double getWindDirection() {
        return windDirection;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public boolean isRaceStarted() {
        return raceStarted;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getExpectedStartTime() {
        return expectedStartTime;
    }

    /**
     * Returns the data for boats collected form race status packets.
     *
     * @return A list of boat data. Boat data is in the form
     * [boatID, estTimeToNextMark, estTimeToFinish, legNumber].
     */
    public List<long[]> getBoatData () {
        return boatData;
    }
}
