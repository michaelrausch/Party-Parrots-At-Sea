package seng302.visualiser;

import seng302.gameServer.ServerAdvertiser;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerListener{
    private static ServerListener instance;
    private ServerListenerDelegate delegate;
    private JmDNS jmdns = null;

    private class GameServerListener implements ServiceListener {
        GameServerListener(){
            System.out.println("Created GSL instance");
        }

        @Override
        public void serviceAdded(ServiceEvent event) {
            delegate.serverDetected(event.getName(), "", 123, event.getInfo().getInet4Addresses().toString());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            delegate.serverRemoved(event.getName());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            // Do nothing
        }
    }

    private ServerListener() throws IOException {
        jmdns = JmDNS.create(InetAddress.getLocalHost());
        jmdns.addServiceListener(ServerAdvertiser.SERVICE_TYPE, new GameServerListener());
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
