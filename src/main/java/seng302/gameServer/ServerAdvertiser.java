package seng302.gameServer;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class ServerAdvertiser {
    private static String SERVICE = "_partyatsea_";
    private static String PROTOCOL = "_tcp";
    public static String SERVICE_TYPE = SERVICE + "." + PROTOCOL + ".local";
    private static Integer PROTO_VERSION = 1;


    private static ServerAdvertiser instance = null;
    private static JmDNS jmdnsInstance = null;

    private ServerAdvertiser() throws IOException{
        jmdnsInstance = JmDNS.create(InetAddress.getLocalHost(), InetAddress.getByName(InetAddress.getLocalHost().getHostName()).toString());
    }

    public static ServerAdvertiser getInstance() throws IOException {
        if (instance == null){
            instance = new ServerAdvertiser();
        }

        return instance;
    }

    public void registerGame(Integer portNo, String serverName, Integer spacesLeft, String mapName) throws IOException {
        String serviceData = packageServerData(spacesLeft, mapName, PROTO_VERSION);
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serverName, portNo, serviceData);

        jmdnsInstance.registerService(serviceInfo);
    }

    private String packageServerData(Integer spacesLeft, String mapName, Integer version){
        return spacesLeft.toString() + "|" + mapName + "|" + version.toString();
    }
}
