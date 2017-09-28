package seng302.serverDiscovery;

import org.junit.BeforeClass;
import org.junit.Test;
import seng302.discoveryServer.util.ServerListing;
import seng302.discoveryServer.util.ServerTable;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class ServerTableTest {
    private static ServerTable serverTable;

    @BeforeClass
    public static void setup(){
        serverTable = new ServerTable();
    }

    @Test
    public void testAddServer(){
        ServerListing listing = new ServerListing("", "", "", 12, 12);
        serverTable.addServer(listing);

        assertTrue(serverTable.getAllServers().contains(listing));
    }

    @Test
    public void testGetNextRoomCodeIsUnique(){
        assertTrue(!Objects.equals(serverTable.getNextRoomCode(), serverTable.getNextRoomCode()));
    }

    @Test
    public void testGetServerRoomCode(){
        ServerListing listing = new ServerListing("123", "", "", 12, 12);
        listing.setRoomCode(serverTable.getNextRoomCode().toString());
        serverTable.addServer(listing);

        ServerListing result = serverTable.getServerByRoomCode(listing.getRoomCode());

        assertTrue(result.equals(listing));
    }

    @Test
    public void testServersRemovedOnExpiry() throws InterruptedException {
        ServerListing listing = new ServerListing("432", "221", "", 12, 12);
        listing.setTtl(1);

        serverTable.addServer(listing);

        listing.decrementTtl();
        listing.decrementTtl();

        Thread.sleep(1500);

        assertTrue(!serverTable.getAllServers().contains(listing));
    }
}
