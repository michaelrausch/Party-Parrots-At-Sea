package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ImportantAnnotationController implements Initializable {
    /*
     * JavaFX Outlets
     */
    @FXML
    private CheckBox boatWakeSelect;

    @FXML
    private CheckBox boatSpeedSelect;

    @FXML
    private CheckBox boatTrackSelect;

    @FXML
    private CheckBox boatNameSelect;

    @FXML
    private AnchorPane annotationSelectWindow;

    @FXML
    private Button closeButton;

    private RaceViewController parent;
    private Map<String, Boolean> importantAnnotations;
    private Stage stage;

    ImportantAnnotationController(RaceViewController parent, Stage stage){
        this.parent = parent;
        importantAnnotations = new HashMap<>();
        this.stage = stage;
    }

    /**
     * Sets whether or not an annotation is considered important
     * @param name The annotation name
     * @param isSet True if annotation is important
     */
    private void setAnnotation(String name, Boolean isSet){
        importantAnnotations.put(name, isSet);
    }

    /**
     * Sends an update to the parent controller when the important
     * annotations have changed
     */
    private void sendUpdate(){
        this.parent.importantAnnotationsChanged(this.importantAnnotations);
    }

    /**
     * Load the current state of the 'important annotations'
     * @param currentState hashmap containing the states of each annotation
     */
    void loadState(Map<String, Boolean> currentState){
        this.importantAnnotations = currentState;

        // Initialise checkboxes
        for (String key : importantAnnotations.keySet()){
            switch (key){
                case "BoatWake":
                    boatWakeSelect.setSelected(importantAnnotations.get(key));
                    break;

                case "BoatSpeed":
                    boatSpeedSelect.setSelected(importantAnnotations.get(key));
                    break;

                case "BoatTrack":
                    boatTrackSelect.setSelected(importantAnnotations.get(key));
                    break;

                case "BoatName":
                    boatNameSelect.setSelected(importantAnnotations.get(key));
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * View did load
     * @param location .
     * @param resources .
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boatWakeSelect.setOnAction(event -> {
            setAnnotation("BoatWake", boatWakeSelect.isSelected());
            sendUpdate();
        });

        boatSpeedSelect.setOnAction(event -> {
            setAnnotation("BoatSpeed", boatSpeedSelect.isSelected());
            sendUpdate();
        });

        boatTrackSelect.setOnAction(event -> {
            setAnnotation("BoatTrack", boatTrackSelect.isSelected());
            sendUpdate();
        });

        boatNameSelect.setOnAction(event -> {
            setAnnotation("BoatName", boatNameSelect.isSelected());
            sendUpdate();
        });

        closeButton.setOnAction(event -> stage.close());
    }
}
