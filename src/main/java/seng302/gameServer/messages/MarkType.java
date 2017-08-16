package seng302.gameServer.messages;

/**
 * Types of marks boats can round
 */
public enum MarkType {
    UNKNOWN(0),
    ROUNDING_MARK(1),
    GATE(2);

    private long code;

    MarkType(long code) {
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
