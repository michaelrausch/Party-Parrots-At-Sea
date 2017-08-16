package seng302.model;

import javafx.scene.paint.Color;

/**
 * Enum for generating colours.
 */
public enum Colors {
    RED, PERU, GOLD, GREEN, BLUE, PURPLE, DEEPPINK, GRAY;

    static Integer index = 0;

    public static Color getColor() {
        if (index == 8) {
            index = 0;
        }
        return Color.valueOf(values()[index++].toString());
    }
}
