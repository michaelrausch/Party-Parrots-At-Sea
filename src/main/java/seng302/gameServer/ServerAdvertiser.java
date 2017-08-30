package seng302.gameServer;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

public class ServerAdvertiser {
    private static String SERVICE = "_partyatsea";
    private static String PROTOCOL = "_tcp";
    public static String SERVICE_TYPE = SERVICE + "." + PROTOCOL + ".local.";
    private static Integer PROTO_VERSION = 1;


    private static ServerAdvertiser instance = null;
    private static JmDNS jmdnsInstance = null;

    private ServerAdvertiser() throws IOException{
        jmdnsInstance = JmDNS.create(InetAddress.getLocalHost());
    }

    public static ServerAdvertiser getInstance() throws IOException {
        if (instance == null){
            instance = new ServerAdvertiser();
        }

        return instance;
    }

    public void registerGame(Integer portNo, String serverName, Integer spacesLeft, String mapName) {
        Hashtable<String ,String> props = new Hashtable<>();

        props.put("map", mapName);
        props.put("spacesLeft", spacesLeft.toString());

        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serverName, portNo, 0, 0, props);

        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        jmdnsInstance.registerService(serviceInfo);
                    } catch (IOException e) {
                        System.out.println("Failed");
                    }
                }
            }, 0
        );
    }

    public void unregister(){
        jmdnsInstance.unregisterAllServices();
        jmdnsInstance = null;
    }
}
