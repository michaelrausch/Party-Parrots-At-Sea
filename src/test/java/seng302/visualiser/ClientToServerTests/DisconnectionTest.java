package seng302.visualiser.ClientToServerTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.gameServer.MainServerThread;
import seng302.visualiser.ClientToServerThread;

/**
 * Created by cir27 on 17/08/17.
 */
public class DisconnectionTest {
    @Test
    public void testServerDisconnection () throws Exception {
        MainServerThread serverThread = new MainServerThread();
        ClientToServerThread clientThread = new ClientToServerThread("localhost", serverThread.getPortNumber());
        Thread.sleep(1000);
        clientThread.addDisconnectionListener(message -> Assert.assertTrue(message != null));
        serverThread.terminate();
    }
}
