package seng302.model.stream.packets;

/**
 * Created by Kusal on 4/24/2017.
 */
public enum PacketType {
    HEARTBEAT,
    RACE_STATUS,
    DISPLAY_TEXT_MESSAGE,
    RACE_XML,
    REGATTA_XML,
    BOAT_XML,
    RACE_START_STATUS,
    YACHT_EVENT_CODE,
    YACHT_ACTION_CODE,
    CHATTER_TEXT,
    BOAT_LOCATION,
    MARK_ROUNDING,
    COURSE_WIND,
    AVG_WIND,
    BOAT_ACTION,
    OTHER;

    public static PacketType assignPacketType(int packetType, byte[] payload){
        switch(packetType){
            case 1:
                return HEARTBEAT;
            case 12:
                return RACE_STATUS;
            case 20:
                return DISPLAY_TEXT_MESSAGE;
            case 26:
                switch (payload[9]) { //The type of XML message
                    case 5:
                        return REGATTA_XML;
                    case 6:
                        return RACE_XML;
                    case 7:
                        return BOAT_XML;
                }
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
            case 100:
                return BOAT_ACTION;
            default:
        }
        return OTHER;
    }
}
