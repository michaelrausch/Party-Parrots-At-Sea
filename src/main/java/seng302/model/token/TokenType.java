package seng302.model.token;

/**
 * An enum describing the different types of game objects
 * Created by wmu16 on 28/08/17.
 */
public enum TokenType {

    BOOST(0, "Boost", 10_000),
    HANDLING(1, "Handling", 10_000),
    BUMPER(2, "Bumper", 10_000),
    WIND_WALKER(3, "Wind Walker", 10_000),
    RANDOM(4, "Random", 10_000);

    private int value;
    private String name;
    private int timeout;

    TokenType(int value, String name, int timeout) {
        this.value = value;
        this.name = name;
        this.timeout = timeout;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getTimeout() {
        return timeout;
    }
}
