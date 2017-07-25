package seng302.model.stream.parser;

public class PositionUpdateData {

    public enum DeviceType {
        YACHT_TYPE,
        MARK_TYPE
    }

    private int deviceId;
    private DeviceType type;
    private double lat;
    private double lon;
    private double heading;
    private double groundSpeed;

    PositionUpdateData(int deviceId, DeviceType type, double lat, double lon,
        double heading, double groundSpeed) {
        this.deviceId = deviceId;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.groundSpeed = groundSpeed;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public DeviceType getType() {
        return type;
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
