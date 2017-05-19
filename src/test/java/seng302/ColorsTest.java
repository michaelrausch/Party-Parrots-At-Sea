package seng302;

import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;
import seng302.models.Colors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ColorsTest {

    @Test
    public void testNextColor() {
        Color expectedColors[] = {Color.RED, Color.PERU, Color.SEAGREEN, Color.GREEN, Color.BLUE, Color.PURPLE};
        for (int i = 0; i<6; i++)
        {
            Assert.assertEquals(expectedColors[i], Colors.getColor());
        }
    }
}
