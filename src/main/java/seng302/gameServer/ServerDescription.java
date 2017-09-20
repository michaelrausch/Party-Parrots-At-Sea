package seng302.gameServer;

public class ServerDescription {
    private Integer capacity;
    private String address;
    private Integer portNum;
    private String serverName;
    private String mapName;
    private Integer numPlayers;
    private Long lastUpdated;
    private Long lastRefreshed;

    private static Long EXPIRY_INTERVAL = 5000L;

    public ServerDescription(String serverName, String mapName, Integer numPlayers, Integer capacity, String address, Integer portNum){
        this.serverName = serverName;
        this.mapName = mapName;
        this.numPlayers = numPlayers;
        this.address = address;
        this.portNum = portNum;
        this.capacity = capacity;
        lastUpdated = System.currentTimeMillis();
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

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public Integer getCapacity(){
        return capacity;
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

        if (!this.getCapacity().equals(other.getCapacity())){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + this.getAddress().hashCode() +
                this.portNumber().hashCode() + this.getMapName().hashCode();
    }

    public Boolean hasExpired(){
        return System.currentTimeMillis() - lastUpdated > EXPIRY_INTERVAL;
    }

    public Boolean serverShouldBeRemoved() {
        if (lastRefreshed == null) return false;

        System.out.println("SBR" + (System.currentTimeMillis() - lastRefreshed > EXPIRY_INTERVAL));
        return System.currentTimeMillis() - lastRefreshed > EXPIRY_INTERVAL;
    }

    public void hasBeenRefreshed(){
        System.out.println("Was refreshed");
        lastRefreshed = System.currentTimeMillis();
    }
}
