package seng302.model.token;

/**
 * An enum describing the different types of game objects
 * Created by wmu16 on 28/08/17.
 */
public enum TokenType {
    BOOST(0),
    HANDLING(1);

    private int value;

    TokenType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TokenType getToken(int value) {
        switch (value) {
            case 0:
                return BOOST;
            case 1:
                return HANDLING;
            default:
                return BOOST;
        }
    }
}
