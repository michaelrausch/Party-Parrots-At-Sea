package seng302.gameServer;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.serverRepository.ServerListing;
import seng302.serverRepository.ServerRepositoryClient;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Advertises the game server on the local network
 */
public class ServerAdvertiser {
    /*
    Our service name & protocol

    This must be in the format _Service._Proto.Name as per http://www.ietf.org/rfc/rfc2782.txt
    Where Service is unique on the network, and protocol is usually _tcp.

    The pseudo-domain 'local.' must end in a full-stop. This is used to indicate that
    the lookup should be performed using an IP multicast query on the local IP network.

    Read this before changing any of the following values
    https://developer.apple.com/library/content/documentation/Cocoa/Conceptual/NetServices/Articles/domainnames.html#//apple_ref/doc/uid/TP40002460-SW1
     */
    private static String SERVICE = "_partyatsea";
    private static String PROTOCOL = "_tcp";
    public static String SERVICE_TYPE = SERVICE + "." + PROTOCOL + ".local.";

    private static ServerAdvertiser instance = null;
    private static JmDNS jmdnsInstance = null;
    private ServiceInfo serviceInfo; // Note: Whenever this is changed, our service will be re-registered on the network.
    private ServerRepositoryClient repositoryClient;

    private Hashtable<String ,String> props;

    private ServerAdvertiser() throws IOException{
        jmdnsInstance = JmDNS.create(InetAddress.getByName(getLocalHostIp()));

        repositoryClient = new ServerRepositoryClient();

        props = new Hashtable<>();
        props.put("map", "");
        props.put("spacesLeft", "0");
        props.put("capacity", "0");
        props.put("players", "0");
    }

    /**
     * Get an instance of the ServerAdvertiser, create an instance if there isn't already one
     * @return A ServerAdvertiser Instance
     * @throws IOException If there was an exception creating the instance
     */
    public static ServerAdvertiser getInstance() throws IOException {
        if (instance == null){
            instance = new ServerAdvertiser();
        }

        return instance;
    }

    /**
     * Set the map name and broadcast an update on the network
     * @param mapName The new map name
     * @return The current ServerAdvertiser instance
     */
    public ServerAdvertiser setMapName(String mapName){
        props.replace("map", mapName);

        if (serviceInfo != null){
            serviceInfo.setText(props);
        }

        return instance;
    }

    /**
     * Set the number of players on the server and broadcast an update on the network
     * @param numPlayers The number of players on the server
     * @return The current ServerAdvertiser instance
     */
    public ServerAdvertiser setNumberOfPlayers(Integer numPlayers){
        props.replace("players", numPlayers.toString());

        if (serviceInfo != null){
            serviceInfo.setText(props);
        }

        return instance;
    }

    /**
     * Set the max capacity of the server and broadcast an update on the network
     * @param capacity The maximum capacity of the server
     * @return The current ServerAdvertiser instance
     */
    public ServerAdvertiser setCapacity(Integer capacity){
        props.replace("capacity", capacity.toString());

        if (serviceInfo != null){
            serviceInfo.setText(props);
        }

        return instance;
    }

    /**
     * Register this service on the network
     *
     * Note: other parameters (map name/spaces left etc) are set after the
     * service has been registered
     * @param portNo The servers port number
     * @param serverName The servers name
     */
    public void registerGame(Integer portNo, String serverName) {

        serviceInfo = ServiceInfo.create(SERVICE_TYPE, serverName, portNo, 0, 0, props);

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
            }, 0);

        ServerListing serverListing = new ServerListing(serverName, props.get("map"), getLocalHostIp(), portNo, Integer.parseInt(props.get("capacity")));
        repositoryClient.register(serverListing);
    }

    /**
     * Unregister the service
     */
    public void unregister(){
        if (serviceInfo != null)
            jmdnsInstance.unregisterService(serviceInfo);
    }

    /**
     * Gets the local host ip address.
     *
     * @return the localhost ip address
     */
    public static String getLocalHostIp() {
        String ipAddress = null;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                if (ni.isLoopback())
                    continue;
                if(ni.isPointToPoint())
                    continue;
                if(ni.isVirtual())
                    continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if(address instanceof Inet4Address) {    // skip all ipv6
                        ipAddress = address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ipAddress == null) {
            System.out.println("[HOST] Cannot obtain local host ip address.");
        }
        return ipAddress;
    }
}
