package seng302.gameServer.server;

import org.junit.Test;
import seng302.gameServer.ServerDescription;

import static org.junit.Assert.assertTrue;

public class TestServerDesc {
    @Test
    public void testEquals(){
        ServerDescription one = new ServerDescription("a", "b", 10, 10, "asd",  1234);
        ServerDescription two = new ServerDescription("a", "b", 10, 10, "asd",  1234);

        assertTrue(one.equals(two));
    }

    @Test
    public void testNotEqualName(){
        ServerDescription one = new ServerDescription("a", "b", 10, 10, "asd",  1234);
        ServerDescription two = new ServerDescription("a2", "b", 10, 10, "asd",  1234);

        assertTrue(!one.equals(two));
    }

    @Test
    public void testNotEqualMap(){
        ServerDescription one = new ServerDescription("a", "b", 10, 10, "asd",  1234);
        ServerDescription two = new ServerDescription("a", "ba", 10, 10, "asd",  1234);

        assertTrue(!one.equals(two));
    }

    @Test
    public void testNotEqualPort(){
        ServerDescription one = new ServerDescription("a", "b", 10, 10, "asd",  1234);
        ServerDescription two = new ServerDescription("a", "b", 10, 10, "asd",  12341);

        assertTrue(!one.equals(two));
    }

    @Test
    public void testNotEqualAddress(){
        ServerDescription one = new ServerDescription("a", "b", 10, 10, "as1d",  1234);
        ServerDescription two = new ServerDescription("a", "b", 10, 10, "asd",  1234);

        assertTrue(!one.equals(two));
    }
}
