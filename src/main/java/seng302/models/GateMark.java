package seng302.models;

/**
 * Created by ptg19 on 16/03/17.
 */
public class GateMark {
    private double lat;
    private double lon;
    private Mark mark1;
    private Mark mark2;
    private String name;

    public GateMark(String name, Mark mark1, Mark mark2, double lat, double lon){
        this.lat = lat;
        this.lon = lon;
        this.mark1 = mark1;
        this.mark2 = mark2;
        this.name = name;
    }
}
