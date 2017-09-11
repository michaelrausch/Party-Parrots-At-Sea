package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.ServerDescription;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StartScreenController implements Initializable{

    //--------FXML BEGIN--------//
    @FXML
    private Label headText;
    @FXML
    private JFXButton startBtn;
    //---------FXML END---------//

    private Node serverList;
    private Logger logger = LoggerFactory.getLogger(StartScreenController.class);
    private List<ServerDescription> servers;
    private GameClient gameClient;
//    public void initialize(URL url,  ResourceBundle resourceBundle) {
//        Sounds.stopMusic();
//        Sounds.stopSoundEffects();
//        Sounds.playMenuMusic();
//        if (Sounds.isMusicMuted()) {
//            muteMusicButton.setText("UnMute Music");
//        } else {
//            muteMusicButton.setText("Mute Music");
//        }
//        if (Sounds.isSoundEffectsMuted()) {
//            muteSoundsButton.setText("UnMute Sounds");
//        } else {
//            muteSoundsButton.setText("Mute Sounds");
//        }
//        Sounds.setMutes();
////        gameClient = new GameClient(holder);
//    }

    public void initialize(URL location, ResourceBundle resources) {
        startBtn.setOnMousePressed(event -> startBtn.setText("LOADING..."));
        startBtn.setOnMouseReleased(event -> goToServerBrowser());

        setInitialDropShadow();
        preloadServerListView();
    }

    /**
     *
     */
    private void setInitialDropShadow() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        headText.setEffect(dropShadow);
    }

    /**
     *
     */
    private void preloadServerListView(){
        try {
            serverList = FXMLLoader
                    .load(StartScreenController.class.getResource("/views/ServerListView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not preload server list view");
        }
    }

    /**
     *
     */
    private void goToServerBrowser() {
        try {
            ViewManager.getInstance().setScene(serverList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleMusic(ActionEvent actionEvent) {
        Sounds.toggleMuteMusic();
        Sounds.playButtonClick();
        if (Sounds.isMusicMuted()) {
//            muteMusicButton.setText("UnMute Music");
        } else {
//            muteMusicButton.setText("Mute Music");
        }
    }

    public void toggleSounds(ActionEvent actionEvent) {
        Sounds.toggleMuteEffects();
        Sounds.playButtonClick();
        if (Sounds.isSoundEffectsMuted()) {
//            muteSoundsButton.setText("UnMute Sounds");
        } else {
//            muteSoundsButton.setText("Mute Sounds");
        }
    }

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }
}
