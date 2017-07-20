package seng302.server.messages;

/**
 * Created by kre39 on 12/07/17.
 */
public enum BoatActionType {

    VMG(1),
    SAILS_IN(2),
    SAILS_OUT(3),
    TACK_GYBE(4),
    UPWIND(5),
    DOWNWIND(6);

    private int type;

    BoatActionType(int type){
        this.type = type;
    }

    public int getType(){
        return this.type;
    }

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
