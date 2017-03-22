package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ptg19 on 20/03/17.
 */
public class RaceResultController implements Initializable{
    @FXML private AnchorPane window;
    @FXML private Parent raceView;
    @FXML private RaceController raceViewController;

    public void setResults(){
        System.out.println("HI MOM");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
