package seng302.model.token;

/**
 * An enum describing the different types of game objects
 * Created by wmu16 on 28/08/17.
 */
public enum TokenType {
    BOOST(0, "Boost"),
    HANDLING(1, "Handling"),
    BUMPER(2, "Bumper"),
    RANDOM(3, "Random"),
    WIND_WALKER(4, "Wind Walker");

    private int value;
    private String name;

    TokenType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
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
