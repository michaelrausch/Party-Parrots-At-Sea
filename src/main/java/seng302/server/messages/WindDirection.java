package seng302.server.messages;

/**
 * Enum containing the supported wind directions
 */
public enum WindDirection {
    NORTH(0x0000L),
    EAST(0x4000L),
    SOUTH(0x8000L);

    private long code;

    WindDirection(long code) {
        this.code = code;
    }

    public long getCode() {
        return code;
    }
}
