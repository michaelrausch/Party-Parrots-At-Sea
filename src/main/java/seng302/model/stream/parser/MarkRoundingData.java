package seng302.model.stream.parser;


/**
 * Simple data wrapper for mark rounding data packet.
 */
public class MarkRoundingData {

    private int boatId;
    private int markId;
    private int roundingSide;
    private long timeStamp;

    public MarkRoundingData(int boatId, int markId, int roundingSide, long timeStamp) {
        this.boatId = boatId;
        this.markId = markId;
        this.roundingSide = roundingSide;
        this.timeStamp = timeStamp;
    }

    public int getBoatId() {
        return boatId;
    }

    public int getMarkId() {
        return markId;
    }

    public int getRoundingSide() {
        return roundingSide;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
