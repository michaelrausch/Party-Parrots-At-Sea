package seng302.model;

import javafx.scene.paint.Color;

/**
 * Enum for generating colours.
 */
public enum Colors {
    RED, PERU, GOLD, GREEN, BLUE, PURPLE, DEEPPINK, GRAY;

    public static Color getColor(Integer index) {
        return Color.valueOf(values()[index].toString());
    }
}
