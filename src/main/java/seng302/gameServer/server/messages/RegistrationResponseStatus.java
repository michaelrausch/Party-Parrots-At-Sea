package seng302.gameServer.server.messages;

public enum RegistrationResponseStatus {
    SUCCESS_SPECTATING(0x00),
    SUCCESS_PLAYING(0x01),
    SUCCESS_TUTORIAL(0x02),
    SUCCESS_GHOSTING(0x03),

    FAILURE_GENERAL(0x10),
    FAILURE_FULL(0x11);

    private int code;

    RegistrationResponseStatus(int code){
        this.code = code;
    }

    /**
     * Get the message code (From the API Spec)
     * @return the message code
     */
    int getCode(){
        return this.code;
    }

    public static RegistrationResponseStatus getResponseStatus(int typeCode){
        switch (typeCode){
            case 0x00:
                return SUCCESS_SPECTATING;
            case 0x01:
                return SUCCESS_PLAYING;
            case 0x02:
                return SUCCESS_TUTORIAL;
            case 0x03:
                return SUCCESS_GHOSTING;
            case 0x10:
                return FAILURE_GENERAL;
            case 0x11:
                return FAILURE_FULL;
            default:
                return FAILURE_GENERAL;
        }
    }
}
