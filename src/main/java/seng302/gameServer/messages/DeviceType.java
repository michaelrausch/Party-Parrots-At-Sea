package seng302.gameServer.messages;

public enum DeviceType {
    UNKNOWN(0),
    RACING_YACHT(1);

    private long code;

    DeviceType(long code) {
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
