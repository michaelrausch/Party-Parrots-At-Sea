package seng302.models;

import javafx.scene.paint.Color;

import java.nio.channels.SocketChannel;

/**
 * A Class defining a player and their respective details in the game as held by the model
 * Created by wmu16 on 10/07/17.
 */
public class Player {

    private SocketChannel socketChannel;
    private Color color;
    private Float xPos;
    private Float yPos;
    private Float heading;
    private Float velocity;
    private Integer lastMarkPassed;


    public Player(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Float getxPos() {
        return xPos;
    }

    public void setxPos(Float xPos) {
        this.xPos = xPos;
    }

    public Float getyPos() {
        return yPos;
    }

    public void setyPos(Float yPos) {
        this.yPos = yPos;
    }

    public Float getHeading() {
        return heading;
    }

    public void setHeading(Float heading) {
        this.heading = heading;
    }

    public Float getVelocity() {
        return velocity;
    }

    public void setVelocity(Float velocity) {
        this.velocity = velocity;
    }

    public Integer getLastMarkPassed() {
        return lastMarkPassed;
    }

    public void setLastMarkPassed(Integer lastMarkPassed) {
        this.lastMarkPassed = lastMarkPassed;
    }

}
