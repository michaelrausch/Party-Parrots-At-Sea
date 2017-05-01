package seng302.server.messages;

/**
 * Enum containing the types of XML messages
 */
public enum XMLMessageSubType {
    REGATTA(5),
    RACE(6),
    BOAT(7);

    private int type;

    XMLMessageSubType(int type){
        this.type = type;
    }

    public int getType(){
        return this.type;
    }
}
