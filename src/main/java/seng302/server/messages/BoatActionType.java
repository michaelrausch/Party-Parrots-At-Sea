package seng302.server.messages;

import java.util.HashMap;
import java.util.Map;

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

    private final int type;
    private static final Map<Integer, BoatActionType> intToTypeMap = new HashMap<>();

    static {
        for (BoatActionType type : BoatActionType.values()) {
            intToTypeMap.put(type.getValue(), type);
        }
    }

    BoatActionType(int type){
        this.type = type;
    }

    public static BoatActionType getType(int value) {
        return intToTypeMap.get(value);
    }

    public int getValue() {
        return this.type;
    }
}
