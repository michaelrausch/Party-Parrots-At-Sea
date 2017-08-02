package seng302.gameServer;

import seng302.models.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * A class for a thread to listen to connections
 * Created by wmu16 on 11/07/17.
 */
public class ServerListenThread extends Thread{
    private ServerSocket serverSocket;
    private ClientConnectionDelegate delegate;

    public ServerListenThread(ServerSocket serverSocket, ClientConnectionDelegate delegate){
        this.serverSocket = serverSocket;
        this.delegate = delegate;
    }

    /**
     * Listens for a connection and upon finding one, creates a Player object and adds it to the universal GameState
     */
    private void acceptConnection() {
        try {
            Socket thisClient = serverSocket.accept();
            if (thisClient != null && GameState.getCurrentStage().equals(GameStages.LOBBYING)) {
                ServerToClientThread thisConnection = new ServerToClientThread(thisClient);
                delegate.clientConnected(thisConnection);
            } else {
                thisClient.close();
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public void run(){
        while (true){
            acceptConnection();
        }
    }
}
