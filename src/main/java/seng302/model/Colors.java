package seng302.model;

import javafx.scene.paint.Color;

/**
 * Enum for generating colours.
 */
public enum Colors {
    RED, PERU, SEAGREEN, GREEN, BLUE, PURPLE;

    static Integer index = 0;

    public static Color getColor() {
        if (index == 6) {
            index = 0;
        }
        return Color.valueOf(values()[index++].toString());
    }
}
