package seng302.models.parsers.packets;

public class BoatPositionPacket {
    private long boatId;
    private long timeValid;
    private double lat;
    private double lon;
    private double heading;
    private double groundSpeed;

    public BoatPositionPacket(long boatId, long timeValid, double lat, double lon, double heading, double groundSpeed) {
        this.boatId = boatId;
        this.timeValid = timeValid;
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.groundSpeed = groundSpeed;
    }

    public long getTimeValid() {
        return timeValid;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getHeading() {
        return heading;
    }

    public double getGroundSpeed() {
        return groundSpeed;
    }
}
