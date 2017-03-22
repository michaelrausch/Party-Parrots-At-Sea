package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seng302.models.Race;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ptg19 on 20/03/17.
 */
public class RaceResultController implements Initializable{
    @FXML private AnchorPane window;
    @FXML private VBox resultsVBox;
    private Race race;

    RaceResultController(Race race){
        this.race = race;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int boatPosition = this.race.getFinishedBoats().length;

        for (int i = this.race.getFinishedBoats().length - 1; i >= 0; i--){
            resultsVBox.getChildren().add(0, new Text(boatPosition + ": " + this.race.getFinishedBoats()[i].getTeamName()));
            boatPosition--;
        }



    }
}
