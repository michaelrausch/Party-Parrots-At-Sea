package seng302.model;

import java.net.Socket;

/**
 * A Class defining a player and their respective details in the game as held by the model
 * Created by wmu16 on 10/07/17.
 */
public class Player {

    private Socket socket;
    private ServerYacht yacht;
    private Integer lastMarkPassed;


    public Player(Socket socket, ServerYacht yacht) {
        this.socket = socket;
        this.yacht = yacht;
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

    public ServerYacht getYacht() {
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
