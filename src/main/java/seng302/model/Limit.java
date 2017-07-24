package seng302.model;

/**
 * Stores data on the border of a race
 */
public class Limit {

    private Integer seqID;
    private Double lat;
    private Double lng;

    public Limit(Integer seqID, Double lat, Double lng) {
        this.seqID = seqID;
        this.lat = lat;
        this.lng = lng;
    }

    public Integer getSeqID() {
        return seqID;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}