package seng302.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import seng302.gameServer.GameServerThread;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServerWithThreading.MainServerThread;
import seng302.gameServerWithThreading.ServerToClientThread;

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

    private GameServerThread gameServerThread;
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
        gameServerThread.terminateGame();
    }

    public static void refreshCompetitors(){
        Collection<String> competitorsIps = MainServerThread.getServerToClientThreads();
        competitors.clear();
        competitors.addAll(competitorsIps);
    }

    @FXML
    public void readyButtonPressed() {
        GameState.setCurrentStage(GameStages.RACING);
    }

    protected void setGameServerThread(GameServerThread gameServerThread) {
        this.gameServerThread = gameServerThread;
    }
}
