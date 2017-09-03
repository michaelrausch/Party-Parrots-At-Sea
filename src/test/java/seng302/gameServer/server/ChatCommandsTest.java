package seng302.gameServer.server;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatStatus;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.parser.RaceStatusData;
import seng302.utilities.StreamParser;
import seng302.visualiser.ClientToServerThread;

/**
 * Created by cir27 on 3/09/17.
 */
public class ChatCommandsTest {

//    @Rule
//    public Timeout globalTimeout = new Timeout(3, TimeUnit.SECONDS);

    @Test
    public void sendFinishAsHost () {
        try {
            final MainServerThread mst = new MainServerThread();
            final ClientToServerThread host = new ClientToServerThread("localhost", 4942);
            host.addStreamObserver(() -> {
                while (host.getPacketQueue().peek() != null) {
                    StreamPacket packet = host.getPacketQueue().poll();
                    switch (packet.getType()) {
                        case RACE_STATUS:
                            RaceStatusData rsd = StreamParser.extractRaceStatus(packet);
                            System.out.println("yas");
                            System.out.println(rsd.getBoatData().get(0)[4]);
                            if (rsd.getBoatData().get(0)[4] == BoatStatus.FINISHED.getCode()) {
                                System.out.println("why tho");
                                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");
                                mst.terminate();
                                host.setSocketToClose();
                                Assert.assertTrue(true);
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
            mst.startGame();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            host.sendChatterMessage("[time_prefix] <name_prefix> >finish");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void sendSpeedAsHostValid () {
        MainServerThread mst = new MainServerThread();
        ClientToServerThread host = null;
        try {
            host = new ClientToServerThread("localhost", 4942);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mst.startGame();
        host.sendChatterMessage("[time_prefix] <name_prefix> >speed 5.0");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        mst.terminate();
        host.setSocketToClose();
        Assert.assertEquals(5.0, GameState.getSpeedMultiplier(), 0.00001);
    }

    @Test public void sendSpeedAsHostInvalid () {
        MainServerThread mst = new MainServerThread();
        ClientToServerThread host = null;
        try {
            host = new ClientToServerThread("localhost", 4942);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mst.startGame();
        host.sendChatterMessage("[time_prefix] <name_prefix> >speed fdgdgdfg");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        mst.terminate();
        host.setSocketToClose();
        Assert.assertEquals(1.0, GameState.getSpeedMultiplier(), 0.00001);
    }

    @Test
    public void sendFinishAsClient () {
        MainServerThread mst = new MainServerThread();
        ClientToServerThread host = null;
        ClientToServerThread client = null;
        try {
            host = new ClientToServerThread("localhost", 4942);
            client = new ClientToServerThread("localhost", 4942);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mst.startGame();
        client.sendChatterMessage("[time_prefix] <name_prefix> >speed 5.0");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        mst.terminate();
        host.setSocketToClose();
        client.setSocketToClose();
        Assert.assertEquals(1.0, GameState.getSpeedMultiplier(), 0.00001);
    }

    @Test
    public void receiveFinishedAsClient () {
        MainServerThread mst = new MainServerThread();
        ClientToServerThread host = null;
        try {
            host = new ClientToServerThread("localhost", 4942);
            ClientToServerThread client = new ClientToServerThread("localhost", 4942);
            client.addStreamObserver(() -> {
                while (client.getPacketQueue().peek() != null) {
                    StreamPacket packet = client.getPacketQueue().poll();
                    switch (packet.getType()) {
                        case RACE_STATUS:
                            RaceStatusData rsd = StreamParser.extractRaceStatus(packet);
                            if (rsd.getBoatData().get(0)[4] == BoatStatus.FINISHED.getCode()) {
                                mst.terminate();
                                client.setSocketToClose();
                                Assert.assertTrue(true);
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mst.startGame();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        host.sendChatterMessage("[time_prefix] <name_prefix> >finish");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        host.setSocketToClose();
    }
}
