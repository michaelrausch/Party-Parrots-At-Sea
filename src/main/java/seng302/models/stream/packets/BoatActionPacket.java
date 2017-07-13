package seng302.models.stream.packets;

/**
 * Created by kre39 on 12/07/17.
 */
public class BoatActionPacket {

    BoatActionType actionType;

    public BoatActionPacket(BoatActionType actionType) {
        this.actionType = actionType;
    }

    // Sends the packet to the server
    public void sendPacket(){
        System.out.println(BoatActionType.getBoatPacketType(actionType));
    }
}
