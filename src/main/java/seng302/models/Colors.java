package seng302.models;

import javafx.scene.paint.Color;

/**
 * Enum for randomly generating colours.
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
