package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;

import java.io.IOException;

/**
 * A class describing the actions of the lobby screen
 * Created by wmu16 on 10/07/17.
 */
public class LobbyController {

    @FXML
    private GridPane lobbyScreen;
    @FXML
    private Text lobbyIpText;
    @FXML
    private TableView lobbyTable;
    @FXML
    private TableColumn ipTableColumn;
    @FXML
    private TableColumn colourTableColumn;
    @FXML
    private TableColumn readyTableColumn;


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


    @FXML
    public void leaveLobbyButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function!
        setContentPane("/views/StartScreen2View.fxml");
        System.out.println("Leaving lobby!");

    }


    @FXML
    public void readyButtonPressed() {
        GameState.setCurrentStage(GameStages.RACING);
    }
}
