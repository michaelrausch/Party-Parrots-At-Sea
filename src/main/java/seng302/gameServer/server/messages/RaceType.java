package seng302.gameServer.server.messages;

/**
 * Enum containing the types of races
 * sent by the server
 */
public enum RaceType {
    MATCH_RACE(1),
    FLEET_RACE(2);

    private long code;

    RaceType(long code){
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
