package seng302.utilities;

import org.junit.Assert;
import org.junit.Test;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;

/**
 * Basic tests for the next and previous methods
 * Created by kre39 on 20/09/17.
 */
public class BoatMeshTypeTest {


    @Test
    public void testNextBoatMeshType() {
        BoatMeshType currentBoat = BoatMeshType.DINGHY;
        BoatMeshType nextBoat = BoatMeshType.getNextBoatType(currentBoat);
        Assert.assertEquals(BoatMeshType.CATAMARAN, nextBoat);
    }

    @Test
    public void testPreviousBoatMeshType() {
        BoatMeshType currentBoat = BoatMeshType.CATAMARAN;
        BoatMeshType prevBoat = BoatMeshType.getPrevBoatType(currentBoat);
        Assert.assertEquals(BoatMeshType.DINGHY, prevBoat);
    }

}
