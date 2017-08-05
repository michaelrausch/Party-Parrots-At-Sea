package seng302;

import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;
import seng302.model.Colors;

public class ColorsTest {

    @Test
    public void testNextColor() {
        Color expectedColors[] = {Color.RED, Color.PERU, Color.GOLD, Color.GREEN, Color.BLUE,
            Color.PURPLE, Color.DEEPPINK, Color.GRAY};
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals(expectedColors[i], Colors.getColor());
        }
    }
}
