package seng302.gameServer.messages;

// TODO: 14/08/17 ajm412: this may eventually need adjusting due to conforming to the agreed spec.
public enum CustomizeResponseType {
    SUCCESS(0x00),
    FAILURE(0x01),
    FAILURE_MALFORMED_DATA(0x02),
    FAILURE_INCOMPATIBLE(0x03);

    private int type;

    CustomizeResponseType(int type) {
        this.type = type;
    }

    int getType() {
        return this.type;
    }

    public static CustomizeResponseType getResponseType(int typeCode) {
        switch (typeCode) {
            case 0x00:
                return SUCCESS;
            case 0x01:
                return FAILURE;
            case 0x02:
                return FAILURE_MALFORMED_DATA;
            case 0x03:
                return FAILURE_INCOMPATIBLE;
            default:
                return null;
        }
    }
}
