package seng302.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import seng302.models.stream.StreamParser;
import seng302.models.stream.packets.BoatActionPacket;
import seng302.models.stream.packets.BoatActionType;

public class Controller implements Initializable {

    @FXML
    private AnchorPane contentPane;

    private void setContentPane(String jfxUrl) {
        try {
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            contentPane.getChildren()
                .addAll((Pane) FXMLLoader.load(getClass().getResource(jfxUrl)));
        } catch (javafx.fxml.LoadException e) {
            System.err.println(e.getCause());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
        setContentPane("/views/StartScreenView.fxml");
        StreamParser.boatLocations.clear();


    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        BoatActionPacket boatActionPacket;
        switch (e.getCode()){
            case SPACE: // align with vmg
                boatActionPacket = new BoatActionPacket(BoatActionType.VMG);
                boatActionPacket.sendPacket();
                break;
            case PAGE_UP: // upwind
                boatActionPacket = new BoatActionPacket(BoatActionType.UPWIND);
                boatActionPacket.sendPacket();
                break;
            case PAGE_DOWN: // downwind
                boatActionPacket = new BoatActionPacket(BoatActionType.DOWNWIND);
                boatActionPacket.sendPacket();
                break;
            case ENTER: // tack/gybe
                boatActionPacket = new BoatActionPacket(BoatActionType.TACK_GYBE);
                boatActionPacket.sendPacket();
                break;
            //TODO Allow a zoom in and zoom out methods
            case Z:  // zoom in
                System.out.println("Zoom in");
                break;
            case X:  // zoom out
                System.out.println("Zoom out");
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getCode()) {
            //TODO 12/07/17 Determine the sail state and send the appropriate packet (eg. if sails are in, send a sail out packet)
            case SHIFT:  // sails in/sails out
                BoatActionPacket boatActionPacket = new BoatActionPacket(BoatActionType.SAILS_IN);
                boatActionPacket.sendPacket();
                break;
        }
    }
}
