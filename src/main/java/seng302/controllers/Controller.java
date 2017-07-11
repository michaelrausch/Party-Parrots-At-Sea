package seng302.controllers;

import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import seng302.models.stream.StreamParser;

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
        switch (e.getCode()){
            case SPACE: // align with vmg
                System.out.println("Key pressed = space");
                break;
            case PAGE_UP: // upwind
                System.out.println("Key pressed = page up");
                break;
            case PAGE_DOWN: // downwind
                System.out.println("Key pressed = page down");
                break;
            case ENTER: // tack/gybe
                System.out.println("Key pressed = enter");
                break;
            case Z:  // zoom in
                System.out.println("Key pressed = z");
                break;
            case X:  // zoom out
                System.out.println("Key pressed = x");
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getCode()) {
            case SHIFT:  // sails in/sails out
                System.out.println("Key pressed = shift");
                break;
        }
    }
}
