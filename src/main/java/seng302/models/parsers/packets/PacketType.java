package seng302.models.parsers.packets;

/**
 * Created by Kusal on 4/24/2017.
 */
public enum PacketType {
    HEARTBEAT,
    RACE_STATUS,
    DISPLAY_TEXT_MESSAGE,
    XML_MESSAGE,
    RACE_START_STATUS,
    YACHT_EVENT_CODE,
    YACHT_ACTION_CODE,
    CHATTER_TEXT,
    BOAT_LOCATION,
    MARK_ROUNDING,
    COURSE_WIND,
    AVG_WIND,
    OTHER;

    public static PacketType assignPacketType(int packetType){
        switch(packetType){
            case 1:
                return HEARTBEAT;
            case 12:
                return RACE_STATUS;
            case 20:
                return DISPLAY_TEXT_MESSAGE;
            case 26:
                return XML_MESSAGE;
            case 27:
                return RACE_START_STATUS;
            case 29:
                return YACHT_EVENT_CODE;
            case 31:
                return YACHT_ACTION_CODE;
            case 36:
                return CHATTER_TEXT;
            case 37:
                return BOAT_LOCATION;
            case 38:
                return MARK_ROUNDING;
            case 44:
                return COURSE_WIND;
            case 47:
                return AVG_WIND;
            default:
        }
        return OTHER;
    }


}
