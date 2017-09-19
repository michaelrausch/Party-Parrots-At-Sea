package seng302.gameServer.messages;

/**
 * Created by wmu16 on 11/09/17.
 */
public enum YachtEventType {
    COLLISION(33),
    TOKEN_VELOCITY(34),
    TOKEN_BUMPER(35),
    TOKEN_HANDLING(36),
    TOKEN_WIND_WALKER(37),
    TOKEN_RANDOM(38);

    private int code;

    YachtEventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
