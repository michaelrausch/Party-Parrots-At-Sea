package seng302.gameServer;

public class ServerDescription {
    private String address;
    private Integer portNum;

    private String serverName;
    private String mapName;
    private Integer spacesLeft;

    public ServerDescription(String serverName, String mapName, Integer spacesLeft, String address, Integer portNum){
        this.serverName = serverName;
        this.mapName = mapName;
        this.spacesLeft = spacesLeft;
        this.address = address;
        this.portNum = portNum;
    }


    public String getName() {
        return serverName;
    }

    public String getMapName() {
        return mapName;
    }

    public Integer portNumber() {
        return portNum;
    }

    public String getAddress(){
        return address;
    }

    public Integer spacesLeft() {
        return spacesLeft;
    }
}
