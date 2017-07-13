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
import seng302.server.ClientTransmitterThread;
import seng302.server.messages.BoatActionMessage;
import seng302.server.messages.BoatActionType;

public class Controller implements Initializable {

    @FXML
    private AnchorPane contentPane;
    private ClientTransmitterThread clientTransmitterThread;

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
        clientTransmitterThread = new ClientTransmitterThread("RaceVision Test Client Transmitter");


    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        BoatActionMessage boatActionMessage;
        switch (e.getCode()){
            case SPACE: // align with vmg
                boatActionMessage = new BoatActionMessage(BoatActionType.VMG);
                clientTransmitterThread.sendBoatActionMessage(boatActionMessage);
                break;
            case PAGE_UP: // upwind
                boatActionMessage = new BoatActionMessage(BoatActionType.UPWIND);
                clientTransmitterThread.sendBoatActionMessage(boatActionMessage);
                break;
            case PAGE_DOWN: // downwind
                boatActionMessage = new BoatActionMessage(BoatActionType.DOWNWIND);
                clientTransmitterThread.sendBoatActionMessage(boatActionMessage);
                break;
            case ENTER: // tack/gybe
                boatActionMessage = new BoatActionMessage(BoatActionType.TACK_GYBE);
                clientTransmitterThread.sendBoatActionMessage(boatActionMessage);
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
                BoatActionMessage boatActionMessage = new BoatActionMessage(BoatActionType.SAILS_IN);
                clientTransmitterThread.sendBoatActionMessage(boatActionMessage);
                break;
        }
    }
}
