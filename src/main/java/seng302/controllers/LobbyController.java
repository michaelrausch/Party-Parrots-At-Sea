package seng302.controllers;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;

/**
 * A class describing the actions of the lobby screen
 * Created by wmu16 on 10/07/17.
 */
public class LobbyController {

    @FXML
    private ListView competitorsListView;
    @FXML
    private GridPane lobbyScreen;
    @FXML
    private Text lobbyIpText;

    private static ObservableList competitors;

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

    public void initialize() {
        competitors = FXCollections.observableArrayList();
        competitorsListView.setItems(competitors);
    }


    @FXML
    public void leaveLobbyButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function!
        setContentPane("/views/StartScreenView.fxml");
        System.out.println("Leaving lobby!");
        GameState.setCurrentStage(GameStages.CANCELLED);
        // TODO: 20/07/17 wmu16 - Implement some way of terminating the game
    }


    @FXML
    public void readyButtonPressed() {
        GameState.setCurrentStage(GameStages.RACING);
    }
}
