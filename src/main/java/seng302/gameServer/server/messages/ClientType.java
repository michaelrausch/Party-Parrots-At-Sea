package seng302.gameServer.server.messages;

public enum ClientType {
    SPECTATOR(0x00),
    PLAYER(0x01),
    CONTROL_TUTORIAL(0x02),
    GHOST_MODE(0x03);

    private int type;

    ClientType(int type){
        this.type = type;
    }

    public int getCode(){
        return type;
    }

    public static ClientType getClientType(int typeCode){
        switch (typeCode){
            case 0x00:
                return SPECTATOR;
            case 0x01:
                return PLAYER;
            case 0x02:
                return CONTROL_TUTORIAL;
            case 0x03:
                return GHOST_MODE;
            default:
                return PLAYER;
        }
    }
}
