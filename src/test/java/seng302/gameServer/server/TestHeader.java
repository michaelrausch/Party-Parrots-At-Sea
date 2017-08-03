package seng302.gameServer.server;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import seng302.gameServer.server.messages.Header;
import seng302.gameServer.server.messages.MessageType;

/**
 * Tests message header
 */
public class TestHeader {

    @Test
    public void testHeaderSizeEqualsActualSize(){
        Header h = new Header(MessageType.DISPLAY_TEXT_MESSAGE, 1, (short) 1);
        assertTrue(h.getSize() == h.getByteBuffer().array().length);

    }

    @Test
    public void headerSizeIsSameAsSpec(){
        Header h = new Header(MessageType.DISPLAY_TEXT_MESSAGE, 1, (short) 1);
        assertTrue(h.getSize() == 15); // Spec specifies 15 bytes
    }

}
