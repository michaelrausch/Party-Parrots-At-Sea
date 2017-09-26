package seng302.model;

import java.util.HashMap;
import java.util.Map;

public enum KeyAction {
    ZOOM_IN(1),
    ZOOM_OUT(2),
    VMG(3),
    SAILS_STATE(4),
    TACK_GYBE(5),
    UPWIND(6),
    DOWNWIND(7),
    VIEW(8),
    RIGHT(9),
    LEFT(10),
    FORWARD(11),
    BACKWARD(12);

    private final int type;
    private static final Map<Integer, KeyAction> intToTypeMap = new HashMap<>();

    static {
        for (KeyAction type : KeyAction.values()) {
            intToTypeMap.put(type.getValue(), type);
        }
    }

    KeyAction(int type) {
        this.type = type;
    }

    public static KeyAction getType(int value) {
        return intToTypeMap.get(value);
    }

    public int getValue() {
        return this.type;
    }
}
