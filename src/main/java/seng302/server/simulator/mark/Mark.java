package seng302.server.simulator.mark;

/**
 * An abstract class to represent general marks
 * Created by Haoming Yin (hyi25) on 17/3/17.
 */
public class Mark {

    private int seqID;
    private String name;
    private double lat;
    private double lng;
    //private int sourceID;

    public Mark(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * Prints out mark's info and its geo location, good for testing
     * @return a string showing its details
     */
    @Override
    public String toString() {
        return String.format("Mark: %d (%s), lat: %f, lng: %f", seqID, name, lat, lng);
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}


