package seng302;

import javafx.scene.paint.Color;
import org.junit.Test;
import seng302.models.Boat;
import seng302.models.Colors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by ryan_ on 16/03/2017.
 */
public class ColorsTest {
    @Test
    public void testNextColor() {
        List<Boat> boats = new ArrayList<>();
        boats.add(new Boat("Team 1"));
        boats.add(new Boat("Team 2"));
        boats.add(new Boat("Team 3"));
        boats.add(new Boat("Team 4"));
        boats.add(new Boat("Team 5"));
        boats.add(new Boat("Team 6"));

        int count = 0;
        List<Color> enumColors = new ArrayList<>();
        while (count < 6) {
            Color color = Colors.getColor();
            enumColors.add(color);
            count++;
        }

        List<Color> boatColors = new ArrayList<>();
        for (Boat boat : boats) {
            Color color = boat.getColor();
            boatColors.add(color);
        }

        assertEquals(enumColors, boatColors);
    }
}
