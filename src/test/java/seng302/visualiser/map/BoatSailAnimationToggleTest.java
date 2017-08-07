package seng302.visualiser.map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seng302.model.Yacht;
import seng302.visualiser.fxObjects.BoatObject;

/**
 * Created by kre39 on 6/08/17.
 */
public class BoatSailAnimationToggleTest {

    private Yacht yacht;

    @Before
    public void setup() throws Exception{
        yacht = new Yacht("Yacht", 1, "YACHT", "YAC", "Test Yacht", "NZ");
    }

    @Test
    public void sailToggleTest() throws Exception {
        assertFalse(yacht.getSailIn());
        yacht.toggleClientSail();
        assertFalse(yacht.getSailIn());
    }

}
