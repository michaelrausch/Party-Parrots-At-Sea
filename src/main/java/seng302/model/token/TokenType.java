package seng302.model.token;

/**
 * An enum describing the different types of game objects
 * Created by wmu16 on 28/08/17.
 */
public enum TokenType {
    BOOST(0, "Boost"),
    HANDLING(1, "Handling"),
    BUMPER(2, "Bumper"),
    WIND_WALKER(3, "Wind Walker"),
    RANDOM(4, "Random");

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
}
