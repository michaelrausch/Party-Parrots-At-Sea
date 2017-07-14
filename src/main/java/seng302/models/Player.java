package seng302.models;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * A Class defining a player and their respective details in the game as held by the model
 * Created by wmu16 on 10/07/17.
 */
public class Player {

    private SocketChannel socketChannel;
    private Yacht yacht;
    private Integer lastMarkPassed;


    public Player(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
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

        if (socketChannel == null){
            return "Disconnected Player";
        }

        try {
            playerAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        return ((Player) obj).socketChannel.equals(socketChannel);
    }

    @Override
    public int hashCode(){
        return socketChannel.hashCode();
    }
}
