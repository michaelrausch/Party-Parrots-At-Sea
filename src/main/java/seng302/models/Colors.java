package seng302.models;

import javafx.scene.paint.Color;

/**
 * Created by ryan_ on 16/03/2017.
 */
public enum Colors {
    RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE;

    static Integer index = 0;

    public static Color getColor() {
        index++;
        if (index > 6) {
            index = 1;
        }
        return Color.valueOf(values()[index-1].toString());
    }
}
