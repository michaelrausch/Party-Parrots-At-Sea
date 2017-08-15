package seng302.gameServer.messages;

/**
 * The types of race start status messages
 */
public enum RaceStartNotificationType {
    SET_RACE_START_TIME(1),
    RACE_POSTPONED(2),
    RACE_ABANDONED(3),
    RACE_TERMINATED(4);

    private final long type;

    RaceStartNotificationType(long type) {
        this.type = type;
    }

    long getType(){
        return type;
    }
}
