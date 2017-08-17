package seng302.gameServer.messages;

/**
 * The current status of a boat
 */
public enum BoatStatus {
    UNDEFINED(0),
    PRESTART(1),
    RACING(2),
    FINISHED(3),
    DNS(4),
    DNF(5),
    DSQ(6),
    CS(7);

    private long code;

    BoatStatus(long code) {
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
