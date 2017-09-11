package seng302.gameServer.messages;

/**
 * Created by wmu16 on 11/09/17.
 */
public enum YachtEventType {
    COLLISION(33),
    TOKEN(34);

    private int code;

    YachtEventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
