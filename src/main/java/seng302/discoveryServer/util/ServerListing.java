package seng302.discoveryServer.util;

public class ServerListing {
    public final static int SERVER_TTL_DEFAULT = 5;

    private String serverName = "";
    private String mapName = "";
    private String address = "";
    private int portNumber = 0;
    private int capacity = 0;
    private int players = 0;
    private String roomCode = "";
    private int ttl = SERVER_TTL_DEFAULT;


    public ServerListing(String serverName, String mapName, String address, int portNumber, int capacity){
        this.serverName = serverName;
        this.mapName = mapName;
        this.address = address;
        this.portNumber = portNumber;
        this.capacity = capacity;
    }

    public ServerListing setNumberOfPlayers(int players){
        this.players = players;
        return this;
    }

    public ServerListing setRoomCode(String roomCode){
        this.roomCode = roomCode;
        return this;
    }

    public void refreshTtl(){
        ttl = SERVER_TTL_DEFAULT;
    }

    public void decrementTtl(){
        ttl--;
    }

    public boolean hasTtlExpired(){
        return ttl < 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!ServerListing.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final ServerListing other = (ServerListing) obj;

        if (this.getPortNumber() != other.getPortNumber()){
            return false;
        }

        if (!this.getMapName().equals(other.getMapName())){
            return false;
        }

        if (!this.getServerName().equals(other.getServerName())){
            return false;
        }

        if (this.getCapacity() != other.getCapacity()){
            return false;
        }

        if (!this.getAddress().equals(other.getAddress())){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.getServerName().hashCode() +
                this.getAddress().hashCode() + this.getMapName().hashCode();
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getMapName() {
        return mapName;
    }

    public String getServerName() {
        return serverName;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getAddress() {
        return address;
    }

    public void setTtl(Integer ttl){
        this.ttl = ttl;
    }

    public boolean isMaxPlayersReached() {
        return players >= capacity;
    }
}
