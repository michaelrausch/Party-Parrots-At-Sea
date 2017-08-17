package seng302.gameServer.server;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.XMLMessage;
import seng302.gameServer.messages.XMLMessageSubType;

public class TestMessage {
    private static int XML_MESSAGE_LEN = 14;
    private static int CRC_LEN = 4;


    /**
     * Test output expected is the same as the spec
     */
    @Test
    public void testXmlMessageSize(){
        Message m = new XMLMessage("12345", XMLMessageSubType.BOAT, 1);
        assertTrue(m.getSize() == (XML_MESSAGE_LEN + "12345".length()));
    }

    @Test
    public void testMessageBytesReverse(){
        byte[] bytes = {1,2,3,4,5};
        Message.reverse(bytes);

        int testValue = 1;
        for (int i = 5; i > 0; i--){
            assertEquals((byte) testValue, bytes[i-1]);
            testValue++;
        }
    }

    @Test
    public void testIntToByteArray(){
        long originalValue = 0x5050;
        long testValue = 0;

        byte[] bytes = Message.intToByteArray(originalValue, 6);
        Message.reverse(bytes);

        for (int i = 0; i < bytes.length; i++){
            testValue += ((long) bytes[i] & 0xffL) << (8 * i);
        }

        assertEquals(originalValue, testValue);
    }

}
