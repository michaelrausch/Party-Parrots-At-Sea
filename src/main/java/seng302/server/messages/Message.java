package seng302.server.messages;

import java.io.DataOutputStream;

public abstract class Message {
    Header header;

    /**
     * @param header Set the header for this message
     */
    public void setHeader(Header header){
        this.header = header;
    }

    /**
     * @return the header specified for this message
     */
    public Header getHeader(){
        return header;
    }

    /**
     * @return the size of the message
     */
    public abstract int getSize();

    /**
     * Send the message as through the outputStream
     */
    public abstract void send(DataOutputStream outputStream);
}
