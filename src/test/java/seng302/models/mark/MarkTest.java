package seng302.models.mark;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Haoming on 17/3/17.
 */
public class MarkTest {

    private SingleMark singleMark1;
    private SingleMark singleMark2;
    private GateMark gateMark;

    @Before
    public void setUp() throws Exception {
        this.singleMark1 = new SingleMark("testMark_SM1", 12.23234, -34.342, 1, 0);
        this.singleMark2 = new SingleMark("testMark_SM2", 12.23239, -34.352, 2, 1);
        this.gateMark = new GateMark("testMark_GM", MarkType.OPEN_GATE, singleMark1, singleMark2, singleMark1.getLatitude(), singleMark2.getLongitude(), 2);
    }

    @Test
    public void getName() throws Exception {
        assertEquals("testMark_SM1", this.singleMark1.getName());
        assertEquals("testMark_GM", this.gateMark.getName());
    }

    @Test
    public void getMarkType() throws Exception {
        assertTrue(this.singleMark2.getMarkType() == MarkType.SINGLE_MARK);
        assertTrue(this.gateMark.getMarkType() == MarkType.OPEN_GATE);
    }

    @Test
    public void getMarkContent() throws Exception {
        assertEquals(12.23234, this.singleMark1.getLatitude(), 1e-10);
        assertEquals(-34.342, this.singleMark1.getLongitude(), 1e-10);
        assertEquals("testMark_SM1", this.gateMark.getSingleMark1().getName());
        assertEquals(-34.352, this.gateMark.getSingleMark2().getLongitude(), 1e-10);
    }

}