package seng302.server.simulator.mark;

/**
 * An abstract class to represent general marks
 * Created by Haoming Yin (hyi25) on 17/3/17.
 */
public class Mark extends Position {

    private int seqID;
    private String name;
    private int sourceID;

    public Mark(String name, double lat, double lng, int sourceID) {
        super(lat, lng);
        this.name = name;
        this.sourceID = sourceID;
    }

    /**
     * Prints out mark's info and its geo location, good for testing
     * @return a string showing its details
     */
    @Override
    public String toString() {
        return String.format("Mark%d: %s, source: %d, lat: %f, lng: %f", seqID, name, sourceID, lat, lng);
    }

    public int getSeqID() {
        return seqID;
    }

    public void setSeqID(int seqID) {
        this.seqID = seqID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSourceID() {
        return sourceID;
    }

    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }
}


