package seng302.visualiser.map;

import static junit.framework.TestCase.assertFalse;

import org.junit.Before;
import org.junit.Test;
import seng302.model.ClientYacht;

/**
 * Created by kre39 on 6/08/17.
 */
public class BoatSailAnimationToggleTest {

    private ClientYacht yacht;

    @Before
    public void setup() throws Exception{
        yacht = new ClientYacht("Yacht", 1, "YACHT", "YAC", "Test Yacht", "NZ");
    }

    @Test
    public void sailToggleTest() throws Exception {
        assertFalse(yacht.getSailIn());
        yacht.toggleClientSail();
        assertFalse(yacht.getSailIn());
    }

}
