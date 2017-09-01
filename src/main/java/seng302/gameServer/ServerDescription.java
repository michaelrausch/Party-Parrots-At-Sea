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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!ServerDescription.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final ServerDescription other = (ServerDescription) obj;

        if (!this.getAddress().equals(other.getAddress()) ) {
            return false;
        }

        if (!this.portNumber().equals(other.portNumber())){
            return false;
        }

        if (!this.getMapName().equals(other.getMapName())){
            return false;
        }

        if (!this.getName().equals(other.getName())){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + this.getAddress().hashCode() +
                this.portNumber().hashCode() + this.getMapName().hashCode();
    }
}
