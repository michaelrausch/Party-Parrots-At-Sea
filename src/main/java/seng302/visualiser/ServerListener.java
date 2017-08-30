package seng302.visualiser;

import seng302.gameServer.ServerAdvertiser;
import seng302.gameServer.ServerDescription;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServerListener{
    private static ServerListener instance;
    private ServerListenerDelegate delegate;
    private JmDNS jmdns = null;
    GameServeMonitor listener;

    private class GameServeMonitor implements ServiceListener {
        private List<ServerDescription> servers;

        GameServeMonitor(){
            servers = new ArrayList<>();
        }

        @Override
        public void serviceAdded(ServiceEvent event) {
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            Integer serverId = -1;

            for (int i = 0; i < servers.size(); i++){
                ServerDescription server = servers.get(i);
                if (server.getName().equals(event.getInfo().getName())){
                    serverId = i;
                    break;
                }
            }

            if (serverId > 0){
                servers.remove(serverId);
            }

            delegate.serverRemoved(servers);
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            String address = event.getInfo().getServer();
            Integer portNum = event.getInfo().getPort();

            String serverName = event.getInfo().getName();
            String mapName = event.getInfo().getPropertyString("map");
            Integer spacesLeft = Integer.parseInt(event.getInfo().getPropertyString("spacesLeft"));

            ServerDescription serverDescription = new ServerDescription(serverName, mapName, spacesLeft, address, portNum);
            servers.add(serverDescription);

            delegate.serverDetected(serverDescription, Collections.unmodifiableList(servers));
        }
    }

    private ServerListener() throws IOException {
        jmdns = JmDNS.create(InetAddress.getLocalHost());
        listener = new GameServeMonitor();
        jmdns.addServiceListener(ServerAdvertiser.SERVICE_TYPE, listener);
    }

    public static ServerListener getInstance() throws IOException {
        if (instance == null){
            instance = new ServerListener();
        }

        return instance;
    }

    public void setDelegate(ServerListenerDelegate delegate){
        this.delegate = delegate;
    }


}
