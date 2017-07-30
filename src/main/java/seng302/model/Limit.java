package seng302.model;

/**
 * Stores data on the border of a race
 */
public class Limit extends GeoPoint {

    private Integer seqID;

    public Limit(Integer seqID, Double lat, Double lng) {
        super(lat, lng);
        this.seqID = seqID;
    }

    public Integer getSeqID() {
        return seqID;
    }
}