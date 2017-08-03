package seng302.gameServer.server.messages;

/**
 * The status of a boat rounding a mark
 */
public enum RoundingBoatStatus {
    UNKNOWN(0),
    RACING(1),
    DSQ(2),
    WITHDRAWN(3);

    private long code;

    RoundingBoatStatus(long code) {
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
