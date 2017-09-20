package seng302.serverRepository;

import com.sun.corba.se.spi.activation.Server;

import java.util.*;

public class ServerTable {
    private List<ServerListing> servers;
    private int lastRoomCode = 4020;

    public ServerTable(){
        servers = new ArrayList<>();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updateServers();
            }
        }, 0, 1000);

    }

    private void updateServers() {
        List<ServerListing> serversToRemove = new ArrayList<>();

        for (ServerListing server : servers){
            server.decrementTtl();

            if (server.hasTtlExpired()){
                serversToRemove.add(server);
            }
        }

        for (ServerListing server : serversToRemove){
            System.out.println("Removing " + server.getServerName());
            servers.remove(server);
        }
    }

    public void addServer(ServerListing server){
        if (servers.contains(server)){
           updateTtlForServer(server);
           return;
        }

        servers.add(server);
    }

    private void updateTtlForServer(ServerListing server) {
        for (ServerListing serverListing : servers){
            if (server.equals(serverListing)){
                System.out.println("Refreshing TTL For "  + server.getServerName());
                serverListing.refreshTtl();
            }
        }
    }

    public List<ServerListing> getAllServers(){
        return Collections.unmodifiableList(servers);
    }

    public ServerListing getServerByRoomCode(String roomCode){
        for (ServerListing serverListing : servers){
            if (serverListing.getRoomCode().equals(roomCode)){
                return serverListing;
            }
        }

        return null;
    }

    public Integer getNextRoomCode(){
        System.out.println(lastRoomCode);
        lastRoomCode += 1;
        return lastRoomCode;
    }


}
