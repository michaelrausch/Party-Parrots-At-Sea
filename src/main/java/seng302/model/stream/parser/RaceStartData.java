package seng302.model.stream.parser;

/**
 * Class for storing data parsed from race start status packet
 */
public class RaceStartData {

    private long raceId;
    private long raceStartTime;
    private int notificationType;
    private long timeStamp;

    public RaceStartData (long raceId, long raceStartTime, int notificationType, long timeStamp) {
        this.raceId = raceId;
        this.raceStartTime = raceStartTime;
        this.notificationType = notificationType;
        this.timeStamp = timeStamp;
    }

    public long getRaceId() {
        return raceId;
    }

    public long getRaceStartTime() {
        return raceStartTime;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
