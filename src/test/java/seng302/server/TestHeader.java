package seng302.server;

import org.junit.Test;
import seng302.server.messages.*;

import static junit.framework.TestCase.assertTrue;

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
