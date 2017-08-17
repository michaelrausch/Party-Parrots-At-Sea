package seng302.gameServer.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kre39 on 12/07/17.
 */
public enum BoatAction {

    VMG(1),
    SAILS_IN(2),
    SAILS_OUT(3),
    TACK_GYBE(4),
    UPWIND(5),
    DOWNWIND(6),
    MAINTAIN_HEADING(7);

    private final int type;
    private static final Map<Integer, BoatAction> intToTypeMap = new HashMap<>();

    static {
        for (BoatAction type : BoatAction.values()) {
            intToTypeMap.put(type.getValue(), type);
        }
    }

    BoatAction(int type){
        this.type = type;
    }

    public static BoatAction getType(int value) {
        return intToTypeMap.get(value);
    }

    public int getValue() {
        return this.type;
    }
}
