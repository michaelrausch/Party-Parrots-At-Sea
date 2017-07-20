package seng302.models;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * A Class defining a player and their respective details in the game as held by the model
 * Created by wmu16 on 10/07/17.
 */
public class Player {

    private Socket socket;
    private Yacht yacht;
    private Integer lastMarkPassed;


    public Player(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public Integer getLastMarkPassed() {
        return lastMarkPassed;
    }

    public void setLastMarkPassed(Integer lastMarkPassed) {
        this.lastMarkPassed = lastMarkPassed;
    }

    public Yacht getYacht() {
        return yacht;
    }

    @Override
    public String toString() {
        String playerAddress = null;

        if (socket == null){
            return "Disconnected Player";
        }

        playerAddress = socket.getRemoteSocketAddress().toString();


        return playerAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }

        if (!(obj instanceof Player)){
            return false;
        }

        return ((Player) obj).socket.equals(socket);
    }

    @Override
    public int hashCode(){
        return socket.hashCode();
    }
}
