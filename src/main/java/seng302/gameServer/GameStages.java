package seng302.gameServer;

/**
 * An enum describing the states of the game
 * Created by wmu16 on 11/07/17.
 */
public enum GameStages {

    LOBBYING(0),
    PRE_RACE(1),
    RACING(2),
    FINISHED(3);

    private long code;

    GameStages(long code) {
        this.code = code;
    }

    public long getCode(){
        return code;
    }
}
