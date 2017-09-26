package seng302.discoveryServer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ServerTable {
    private List<ServerListing> servers;
    private int lastRoomCode = 4020;
    private Logger logger = LoggerFactory.getLogger(ServerTable.class);

    public ServerTable(){
        servers = new ArrayList<>();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updateServers();
            }
        }, 0, 1000);
    }

    /**
     * Update the servers TTL values, and then remove expired servers
     */
    private void updateServers() {
        List<ServerListing> serversToRemove = new ArrayList<>();

        for (ServerListing server : servers){
            server.decrementTtl();

            if (server.hasTtlExpired()){
                logger.debug("Removed expired server - " + server.getServerName());
                serversToRemove.add(server);
            }
        }

        servers.removeAll(serversToRemove);
    }

    /**
     * Add a server to the table
     * @param server The server to add
     */
    public void addServer(ServerListing server){
        if (servers.contains(server)){
           updateTtlForServer(server);
           return;
        }
        logger.debug("Added new server - " + server.getServerName() + " at address: " + server.getAddress() + ":" + server.getPortNumber());
        servers.add(server);
    }

    /**
     * Update the TTL for a given server to the default TTL value
     * @param server The server to update
     */
    private void updateTtlForServer(ServerListing server) {
        for (ServerListing serverListing : servers){
            if (server.equals(serverListing)){
                serverListing.refreshTtl();
            }
        }
    }

    /**
     * @return All the servers in the table
     */
    public List<ServerListing> getAllServers(){
        return Collections.unmodifiableList(servers);
    }

    /**
     * Get a server from the table given its room code
     * @param roomCode The room code to search for
     * @return The ServerListing of the found server, or null
     *         the server wasn't found
     */
    public ServerListing getServerByRoomCode(String roomCode){
        for (ServerListing serverListing : servers){
            if (serverListing.getRoomCode().equals(roomCode)){
                return serverListing;
            }
        }

        return null;
    }

    /**
     * @return The next available room code
     */
    public Integer getNextRoomCode(){
        lastRoomCode += 1;
        return lastRoomCode;
    }
}
