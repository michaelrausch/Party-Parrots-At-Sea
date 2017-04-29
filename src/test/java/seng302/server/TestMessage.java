package seng302.server;

import org.junit.Test;
import seng302.server.messages.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class TestMessage {
    private static int XML_MESSAGE_LEN = 14;
    private static int RACE_STATUS_BASE_LEN = 24;
    private static int BOAT_SUB_MESSAGE_LEN = 20;
    private static int CRC_LEN = 4;


    /**
     * Test output expected is the same as the spec
     */
    @Test
    public void testXmlMessageSize(){
        Message m = new XMLMessage("12345", XMLMessageSubType.BOAT, 1);
        assertTrue(m.getSize() == (XML_MESSAGE_LEN + "12345".length()));
    }


}
