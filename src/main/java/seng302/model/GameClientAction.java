package seng302.model;

import java.util.HashMap;
import java.util.Map;

public enum GameClientAction {
    ZOOM_IN(1),
    ZOOM_OUT(2),
    VMG(3),
    SAILS_STATE(4),
    TACK_GYBE(5),
    UPWIND(6),
    DOWNWIND(7);

    private final int type;
    private static final Map<Integer, GameClientAction> intToTypeMap = new HashMap<>();

    static {
        for (GameClientAction type : GameClientAction.values()) {
            intToTypeMap.put(type.getValue(), type);
        }
    }

    GameClientAction(int type) {
        this.type = type;
    }

    public static GameClientAction getType(int value) {
        return intToTypeMap.get(value);
    }

    public int getValue() {
        return this.type;
    }
}
