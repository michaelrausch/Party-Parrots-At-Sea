package seng302.server;

import seng302.server.messages.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

class StreamingServerSocket {
    private ServerSocketChannel socket;
    private SocketChannel client;
    private short seqNum;
    private boolean isServerStarted;

    StreamingServerSocket(int port) throws IOException{
        socket = ServerSocketChannel.open();
        socket.socket().bind(new InetSocketAddress("localhost", port));
        //socket.setSoTimeout(10000);
        seqNum = 0;
        isServerStarted = false;
    }

    void start(){
        try {
            client = socket.accept();
        } catch (IOException e) {
            e.getMessage();
        }
        if (client.socket() == null){
            start();
        }
        else{
            isServerStarted = true;
        }
    }

    void send(Message message) throws IOException{
        if (client == null){
            return;
        }

        message.send(client);

        seqNum++;
    }

    public short getSequenceNumber(){
        return seqNum;
    }

    public boolean isStarted(){
        return isServerStarted;
    }
}
