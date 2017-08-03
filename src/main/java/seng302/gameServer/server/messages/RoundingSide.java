package seng302.gameServer.server.messages;

/**
 * The side the boat rounded the mark
 */
public enum RoundingSide {
    UNKNOWN(0),
    PORT(1),
    STARBOARD(2);

    private long code;

    RoundingSide(long code) {
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
