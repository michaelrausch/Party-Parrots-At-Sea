package seng302.controllers;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import seng302.client.ClientState;
import seng302.client.ClientStateQueryingRunnable;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;

/**
 * A class describing the actions of the lobby screen
 * Created by wmu16 on 10/07/17.
 */
public class LobbyController implements Initializable, Observer{

//    @FXML
//    private ListView competitorsListView;
    @FXML
    private GridPane lobbyScreen;
    @FXML
    private Text lobbyIpText;
    @FXML
    private ImageView firstImageView;
    @FXML
    private ImageView secondImageView;
    @FXML
    private ImageView thirdImageView;
    @FXML
    private ImageView fourthImageView;
    @FXML
    private ImageView fifthImageView;
    @FXML
    private ImageView sixthImageView;
    @FXML
    private ImageView seventhImageView;
    @FXML
    private ImageView eighthImageView;

    private static ObservableList competitors;
    private ClientStateQueryingRunnable clientStateQueryingRunnable;

    private void setContentPane(String jfxUrl) {
        try {
            AnchorPane contentPane = (AnchorPane) lobbyScreen.getParent();
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            contentPane.getChildren()
                    .addAll((Pane) FXMLLoader.load(getClass().getResource(jfxUrl)));
        } catch (javafx.fxml.LoadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ClientState.isHost())
            lobbyIpText.setText("Lobby Host IP: " + getLocalHostIp());
        else
            lobbyIpText.setText("Connected to IP: ");
        initialiseImageView();

        competitors = FXCollections.observableArrayList();
//        competitorsListView.setItems(competitors);

        // set up client state query thread, so that when it receives the race-started packet
        // it can switch to the race view
        ClientStateQueryingRunnable clientStateQueryingRunnable = new ClientStateQueryingRunnable();
        clientStateQueryingRunnable.addObserver(this);
        Thread clientStateQueryingThread = new Thread(clientStateQueryingRunnable, "Client State querying thread");
        clientStateQueryingThread.setDaemon(true);
        clientStateQueryingThread.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switchToRaceView();
                clientStateQueryingRunnable.terminate();
            }
        });
    }

    private void initialiseImageView() {
        Image image1 = new Image(getClass().getResourceAsStream("/ParrotGif/alistair.gif"));
        firstImageView.setImage(image1);
        Image image2 = new Image(getClass().getResourceAsStream("/ParrotGif/calum.gif"));
        secondImageView.setImage(image2);
        Image image3 = new Image(getClass().getResourceAsStream("/ParrotGif/haoming.gif"));
        thirdImageView.setImage(image3);
        Image image4 = new Image(getClass().getResourceAsStream("/ParrotGif/kusal.gif"));
        fourthImageView.setImage(image4);
        Image image5 = new Image(getClass().getResourceAsStream("/ParrotGif/michael.gif"));
        fifthImageView.setImage(image5);
        Image image6 = new Image(getClass().getResourceAsStream("/ParrotGif/peter.gif"));
        sixthImageView.setImage(image6);
        Image image7 = new Image(getClass().getResourceAsStream("/ParrotGif/ryan.gif"));
        seventhImageView.setImage(image7);
        Image image8 = new Image(getClass().getResourceAsStream("/ParrotGif/will.gif"));
        eighthImageView.setImage(image8);
    }

    private String getLocalHostIp() {
        String ipAddress = null;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                if (ni.isLoopback())
                    continue;
                if(ni.isPointToPoint())
                    continue;
                if(ni.isVirtual())
                    continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if(address instanceof Inet4Address) {    // skip all ipv6
                        ipAddress = address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ipAddress == null) {
            System.out.println("[HOST] Cannot obtain local host ip address.");
        }
        return ipAddress;
    }

    @FXML
    public void leaveLobbyButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function!
        setContentPane("/views/StartScreenView.fxml");
        GameState.setCurrentStage(GameStages.CANCELLED);
        // TODO: 20/07/17 wmu16 - Implement some way of terminating the game
        ClientState.setHost(false);
    }

    @FXML
    public void readyButtonPressed() {
        GameState.setCurrentStage(GameStages.RACING);
    }

    private void switchToRaceView() {
        setContentPane("/views/RaceView.fxml");
    }
}
