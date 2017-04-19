package seng302.server;

import seng302.server.messages.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class StreamingServerSocket {
    private java.net.ServerSocket socket;
    private Socket client;
    private List<Socket> clients;
    private short seqNum;

    StreamingServerSocket(int port) throws IOException{
        socket = new java.net.ServerSocket(port);
        clients = new ArrayList<>();
        socket.setSoTimeout(10000);
        seqNum = 0;
    }

    void start(){
        System.out.println("Listening For Connections");
        try {
            client = socket.accept();
        } catch (IOException e) {
            e.getMessage();
        }
        if (client == null){
            start();
        }
        else{
            System.out.println("client connected from " + client.getInetAddress());
        }
    }

    void send(Message message) throws IOException{
        if (client == null){
            return;
        }

        DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
        message.send(outputStream);

        seqNum++;
    }

    public short getSequenceNumber(){
        return seqNum;
    }
}
