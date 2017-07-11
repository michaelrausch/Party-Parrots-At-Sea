package seng302.models;

import javafx.scene.paint.Color;

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
}
