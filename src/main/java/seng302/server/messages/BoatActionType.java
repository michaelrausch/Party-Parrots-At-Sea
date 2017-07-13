package seng302.server.messages;

/**
 * Created by kre39 on 12/07/17.
 */
public enum BoatActionType {

    VMG,
    SAILS_IN,
    SAILS_OUT,
    TACK_GYBE,
    UPWIND,
    DOWNWIND;

    public static Short getBoatPacketType(BoatActionType type){
        switch (type){
            case VMG:
                return 1;
            case SAILS_IN:
                return 2;
            case SAILS_OUT:
                return 3;
            case TACK_GYBE:
                return 4;
            case UPWIND:
                return 5;
            case DOWNWIND:
                return 6;
        }
        return 0;
    }
}
