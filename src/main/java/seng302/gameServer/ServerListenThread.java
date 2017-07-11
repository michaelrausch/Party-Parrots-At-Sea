package seng302.gameServer;

import com.sun.corba.se.spi.activation.Server;
import seng302.models.Player;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * A class for a thread to listen to connections
 * Created by wmu16 on 11/07/17.
 */
public class ServerListenThread extends Thread{
    private ServerSocketChannel socketChannel;
    private ClientConnectionDelegate delegate;

    public ServerListenThread(ServerSocketChannel socketChannel, ClientConnectionDelegate delegate){
        this.socketChannel = socketChannel;
        this.delegate = delegate;
    }

    /**
     * Listens for a connection and upon finding one, creates a Player object and adds it to the universal GameState
     */
    private void acceptConnection() {
        try {
            SocketChannel thisClient = socketChannel.accept();
            if (thisClient.socket() != null){
                Player thisPlayer = new Player(thisClient);
                GameState.addPlayer(thisPlayer);

                delegate.clientConnected(thisPlayer);
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
