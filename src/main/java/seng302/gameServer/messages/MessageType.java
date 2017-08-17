package seng302.gameServer.messages;

/**
 * Enum containing the types of messages
 * sent by the server
 */
public enum MessageType {
    HEARTBEAT(1),
    RACE_STATUS(12),
    DISPLAY_TEXT_MESSAGE(20),
    XML_MESSAGE(26),
    RACE_START_STATUS(27),
    YACHT_EVENT_CODE(29),
    YACHT_ACTION_CODE(31),
    CHATTER_TEXT(36),
    BOAT_LOCATION(37),
    MARK_ROUNDING(38),
    COURSE_WIND(44),
    AVERAGE_WIND(47),
    BOAT_ACTION(100),
    REGISTRATION_REQUEST(101),
    REGISTRATION_RESPONSE(102),
    CUSTOMIZATION_REQUEST(103),
    CUSTOMIZATION_RESPONSE(104);


    private int code;

    MessageType(int code){
        this.code = code;
    }

    /**
     * Get the message code (From the API Spec)
     * @return the message code
     */
    int getCode(){
        return this.code;
    }


}
