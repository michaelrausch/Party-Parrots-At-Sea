package seng302.gameServer.messages;

// TODO: 14/08/17 ajm412: this may eventually need adjusting due to conforming to the agreed spec.
public enum CustomizeRequestType {
    NAME(0x00),
    COLOR(0x01),
    SHAPE(0x02);

    private int type;

    CustomizeRequestType(int type) {
        this.type = type;
    }

    int getType() {
        return this.type;
    }

    public static CustomizeRequestType getRequestType(int typeCode) {
        switch (typeCode) {
            case 0x00:
                return NAME;
            case 0x01:
                return COLOR;
            case 0x02:
                return SHAPE;
            default:
                return null;
        }
    }
}
