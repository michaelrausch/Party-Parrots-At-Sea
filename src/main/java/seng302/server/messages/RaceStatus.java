package seng302.server.messages;

/**
 * The current status of the race
 */
public enum RaceStatus {
    NOTACTIVE(0),
    WARNING(1), // Between 3:00 and 1:00 before start
    PREPARATORY(2), // Less than 1:00 before start
    STARTED(3),
    ABANDONED(6),
    POSTPONED(7),
    TERMINATED(8),
    RACE_START_TIME_NOT_SET(9),
    PRESTART(10); // More than 3:00 before start

    private int code;

    RaceStatus(int code){
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }
}
