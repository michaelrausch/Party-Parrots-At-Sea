package seng302.gameServer.messages;

/**
 * The side the boat rounded the mark
 */
public enum RoundingSide {
    UNKNOWN(0, "Unknown"),
    PORT(1, "Port"),
    STARBOARD(2, "Stbd"),
    SP(3, "SP"),
    PS(4, "PS");


    private long code;
    private String name;

    RoundingSide(long code, String name) {
        this.code = code;
        this.name = name;
    }

    public long getCode(){
        return code;
    }

    public String getName() {
        return name;
    }

    public static RoundingSide getRoundingSide(String identifier) {
        RoundingSide roundingSide = UNKNOWN;
        switch (identifier) {
            case "Unknown":
                roundingSide = UNKNOWN;
                break;
            case "Port":
                roundingSide = PORT;
                break;
            case "Stbd":
                roundingSide = STARBOARD;
                break;
            case "SP":
                roundingSide = SP;
                break;
            case "PS":
                roundingSide = PS;
                break;
        }

        return roundingSide;
    }
}
