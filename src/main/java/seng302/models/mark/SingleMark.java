package seng302.models.mark;

/**
 * Represents the marker as a single mark
 *
 * Created by Haoming Yin (hyi25) on 17/3/2017
 */
public class SingleMark extends Mark {
    private double lat;
    private double lon;
    private String name;
    private int id;

    /**
     * Represents a marker
     *
     * @param name, the name of the marker*
     * @param lat,  the latitude of the marker
     * @param lon,  the longitude of the marker
     */
    public SingleMark(String name, double lat, double lon, int id) {
        super(name, MarkType.SINGLE_MARK, id);
        this.lat = lat;
        this.lon = lon;
        this.id = id;
    }

    /**
     * Represents the marker at the beginning of a leg
     *
     * @param name, the name of the marker
     */
    public SingleMark(String name) {
        super(name, MarkType.SINGLE_MARK, 0);
        this.lat = 0;
        this.lon = 0;
        this.id = 0;
    }

    public double getLatitude() {
        return this.lat;
    }

    public double getLongitude() {
        return this.lon;
    }
}