package seng302.utilities;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import seng302.model.GeoPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Mark;
import seng302.model.token.Token;

/**
 * Created by wmu16 on 27/09/17.
 */
public class RandomSpawnTest {

    private RandomSpawn randomSpawn;

    Mark mark1 = new Mark("mark1", 0, 57.670333, 11.827833, 0);
    Mark mark2 = new Mark("mark2", 1, 57.671829, 11.842049, 1);
    CompoundMark compoundMark1 = new CompoundMark(0, "mark1",
        new ArrayList<>(Arrays.asList(mark1)));
    CompoundMark compoundMark2 = new CompoundMark(0, "mark1",
        new ArrayList<>(Arrays.asList(mark2)));

    List<CompoundMark> markOrder = new ArrayList<>(Arrays.asList(compoundMark1, compoundMark2));

    @Before
    public void setup() {
        randomSpawn = new RandomSpawn(markOrder);
    }

    @Test
    public void testGetRandomTokenLocation() {
        GeoPoint testMidPoint = GeoUtility
            .getDirtyMidPoint(compoundMark1.getMidPoint(), compoundMark2.getMidPoint());
        Double maxDistance = GeoUtility.getDistance(testMidPoint, compoundMark2.getMidPoint());
        for (int i = 0; i < 1000; i++) {
            Token token = randomSpawn.getRandomToken();
            Double distanceFromCentreRadius = GeoUtility.getDistance(testMidPoint, token);
            assertTrue("Out of bounds token", distanceFromCentreRadius <= maxDistance);
        }


    }

}