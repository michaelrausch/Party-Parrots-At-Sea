package seng302.model.mark;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import seng302.model.GeoPoint;

/**
 * A class to test the compound mark calss
 * Created by wmu16 on 10/08/17.
 */
public class CompoundMarkTest {

    private Mark mark1;
    private Mark mark2;
    private CompoundMark gateMark;
    private CompoundMark singleMark;

    private static Double TOLERANCE_RATIO = 0.01;


    @Before
    public void setUp() throws Exception {
        mark1 = new Mark("Mark1", 57.670333, 11.842833, 0);
        mark2 = new Mark("Mark2", 57.671524, 11.844495, 1);

        List<Mark> gateMarks = new ArrayList<Mark>();
        gateMarks.add(mark1);
        gateMarks.add(mark2);

        List<Mark> singleMarks = new ArrayList<Mark>();
        singleMarks.add(mark1);

        gateMark = new CompoundMark(0, "Fun Mark", gateMarks);
        singleMark = new CompoundMark(1, "Awesome Mark", singleMarks);
    }


    @Test
    public void getSubMark() throws Exception {
        assertEquals(mark1, gateMark.getSubMark(1));
        assertEquals(mark2, gateMark.getSubMark(2));

        assertEquals(mark1, singleMark.getSubMark(1));
    }

    @Test
    public void getMidPoint() throws Exception {
        GeoPoint result = gateMark.getMidPoint();
        assertEquals(57.6709285, result.getLat(), result.getLat() * TOLERANCE_RATIO);
        assertEquals(11.843664, result.getLng(), result.getLng() * TOLERANCE_RATIO);

        result = singleMark.getMidPoint();
        assertEquals(result, mark1);
    }

    @Test
    public void isGate() throws Exception {
        assertTrue(gateMark.isGate());
        assertFalse(singleMark.isGate());
    }

}