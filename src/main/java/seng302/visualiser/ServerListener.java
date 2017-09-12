package seng302.visualiser;

import seng302.gameServer.ServerAdvertiser;
import seng302.gameServer.ServerDescription;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static seng302.gameServer.ServerAdvertiser.getLocalHostIp;

/**
 * Listens for servers on the local network
 */
public class ServerListener{
    private static ServerListener instance;
    private ServerListenerDelegate delegate;
    private JmDNS jmdns = null;
    GameServeMonitor listener;

    private class GameServeMonitor implements ServiceListener {
        private Set<ServerDescription> servers;

        GameServeMonitor(){
            servers = new HashSet<>();
        }

        /**
         * A Service has been detected but not resolved
         * @param event The ServiceEvent
         */
        @Override
        public void serviceAdded(ServiceEvent event) {
        }

        /**
         * A Service has been removed / unregistered
         * @param event The ServiceEvent
         */
        @Override
        public void serviceRemoved(ServiceEvent event) {
            String serverName = event.getInfo().getName();

            ServerDescription toRemove = null;

            for (ServerDescription server : servers){
                if (server.getName().equals(serverName)){
                    toRemove = server;
                }
            }

            if (toRemove != null){
                servers.remove(toRemove);
            }

            delegate.serverRemoved(new ArrayList<ServerDescription>(servers));

            // Get all other servers with the same name to respond if they are up
            jmdns.requestServiceInfo(ServerAdvertiser.SERVICE_TYPE, serverName);

        }

        /**
         * A Service has been added and resolved
         * @param event The ServiceEvent
         */
        @Override
        public void serviceResolved(ServiceEvent event) {
            String address = event.getInfo().getServer();
            Integer portNum = event.getInfo().getPort();

            String serverName = event.getInfo().getName();
            String mapName = event.getInfo().getPropertyString("map");

            Integer capacity = Integer.parseInt(event.getInfo().getPropertyString("capacity"));
            Integer numPlayers = Integer.parseInt(event.getInfo().getPropertyString("players"));

            ServerDescription serverDescription = new ServerDescription(serverName, mapName, numPlayers, capacity, address, portNum);

            servers.remove(serverDescription);
            servers.add(serverDescription);

            delegate.serverDetected(serverDescription, new ArrayList<>(servers));
        }
    }

    private ServerListener() throws IOException {
        jmdns = JmDNS.create(InetAddress.getByName(getLocalHostIp()));
        listener = new GameServeMonitor();
        jmdns.addServiceListener(ServerAdvertiser.SERVICE_TYPE, listener);
    }

    public static ServerListener getInstance() throws IOException {
        if (instance == null){
            instance = new ServerListener();
        }

        return instance;
    }

    /**
     * Set the delegate to handle events
     * @param delegate .
     */
    public void setDelegate(ServerListenerDelegate delegate){
        this.delegate = delegate;
    }
}
