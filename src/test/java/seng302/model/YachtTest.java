package seng302.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Mark;

/**
 * Use this link to test geo distances
 * http://www.csgnetwork.com/gpsdistcalc.html
 * Created by wmu16 on 3/08/17.
 */
public class YachtTest {

    private Yacht yacht;
    private CompoundMark compoundMark;
    private Double toleranceRatio = 0.01;
    private GeoPoint p1 = new GeoPoint(57.670333, 11.827833);
    private GeoPoint p2 = new GeoPoint(57.671524, 11.844495);
    private GeoPoint p3 = new GeoPoint(57.670822, 11.843392);
    private GeoPoint p4 = new GeoPoint(25.694829, 98.392049);

    @Before
    public void setup() {
        yacht = new Yacht("Yacht",
            0,
            "0",
            "WillIsCool",
            "HaomingIsOk",
            "NZL");

        yacht.setLocation(57.670333, 11.827833);

        compoundMark = new CompoundMark(0, "HaomingsMark");
        Mark subMark1 = new Mark("H", 57.671524, 11.844495, 0);
        Mark subMark2 = new Mark("H", 57.670822, 11.843392, 0);
        compoundMark.addSubMarks(subMark1, subMark2);

    }


    //This will no longer work as we cant set the next mark any more as we no longer hold it in
    //yacht class as an attribute

//    @Test
//    public void testDistanceToNextMark() {
//        Double actual, expected;
//        actual = yacht.calcDistanceToNextMark();
//        expected = 927d;
//        assertEquals(expected, actual, expected * toleranceRatio);
//    }


}