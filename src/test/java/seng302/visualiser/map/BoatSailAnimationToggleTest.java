package seng302.visualiser.map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import seng302.model.ClientYacht;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;

/**
 * Created by kre39 on 6/08/17.
 */
public class BoatSailAnimationToggleTest {

    private ClientYacht yacht;

    @Before
    public void setup() throws Exception{
        yacht = new ClientYacht(BoatMeshType.DINGHY, 1, "YACHT", "YAC", "Test Yacht", "NZ");
    }

    @Test
    public void sailToggleTest() throws Exception {
//        assertTrue(yacht.getSailIn());
//        yacht.toggleSail();
//        assertFalse(yacht.getSailIn());
    }

}
