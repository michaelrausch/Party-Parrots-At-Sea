package seng302.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import seng302.controllers.annotations.Annotation;
import seng302.controllers.annotations.ImportantAnnotationController;
import seng302.controllers.annotations.ImportantAnnotationDelegate;
import seng302.controllers.annotations.ImportantAnnotationsState;
import seng302.models.*;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkGroup;
import seng302.models.stream.StreamParser;

import java.io.IOException;
import java.util.*;

/**
 * Created by ptg19 on 29/03/17.
 */
public class RaceViewController extends Thread implements ImportantAnnotationDelegate {

    @FXML
    private VBox positionVbox;
    @FXML
    private CheckBox toggleFps;
    @FXML
    private Text timerLabel;
    @FXML
    private AnchorPane contentAnchorPane;
    @FXML
    private Text windArrowText, windDirectionText;
    @FXML
    private Slider annotationSlider;
    @FXML
    private Button selectAnnotationBtn;
    @FXML
    private ComboBox boatSelectionComboBox;
    @FXML
    private CanvasController includedCanvasController;

    private ArrayList<Yacht> startingBoats = new ArrayList<>();
    private boolean displayFps;
    private Timeline timerTimeline;
    private Stage stage;

    private ImportantAnnotationsState importantAnnotations;
    private Yacht selectedBoat;

    public void initialize() {
        // Load a default important annotation state
        importantAnnotations = new ImportantAnnotationsState();

        includedCanvasController.setup(this);
        includedCanvasController.initializeCanvas();
        initializeUpdateTimer();
        initialiseFPSCheckBox();
        initialiseAnnotationSlider();
        initialiseBoatSelectionComboBox();
        includedCanvasController.timer.start();

        selectAnnotationBtn.setOnAction(event -> {
            loadSelectAnnotationView();
        });
    }

    /**
     * The important annotations have been changed, update this view
     * @param importantAnnotationsState The current state of the selected annotations
     */
    public void importantAnnotationsChanged(ImportantAnnotationsState importantAnnotationsState) {
        this.importantAnnotations = importantAnnotationsState;
        setAnnotations((int) annotationSlider.getValue()); // Refresh the displayed annotations
    }

    /**
     * Loads the "select annotations" view in a new window
     */
    private void loadSelectAnnotationView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = new Stage();

            // Set controller
            ImportantAnnotationController controller = new ImportantAnnotationController(this,
                stage);
            fxmlLoader.setController(controller);

            // Load FXML and set CSS
            fxmlLoader
                .setLocation(getClass().getResource("/views/importantAnnotationSelectView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 469, 298);
            scene.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(scene);
            stage.show();

            controller.loadState(importantAnnotations);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initialiseFPSCheckBox() {
        displayFps = true;
        toggleFps.selectedProperty().addListener(
            (observable, oldValue, newValue) -> displayFps = !displayFps);
    }

    private void initialiseAnnotationSlider() {
        annotationSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0) {
                    return "None";
                }
                if (n == 1) {
                    return "Important";
                }
                if (n == 2) {
                    return "All";
                }

                return "All";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "None":
                        return 0d;
                    case "Important":
                        return 1d;
                    case "All":
                        return 2d;

                    default:
                        return 2d;
                }
            }
        });

        annotationSlider.valueProperty().addListener((obs, oldval, newVal) ->
            setAnnotations((int) annotationSlider.getValue()));

        annotationSlider.setValue(2);
    }


    /**
     * Initalises a timer which updates elements of the RaceView such as wind direction, boat
     * orderings etc.. which are dependent on the info from the stream parser constantly.
     * Updates of each of these attributes are called ONCE EACH SECOND
     */
    private void initializeUpdateTimer() {
        timerTimeline = new Timeline();
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        // Run timer update every second
        timerTimeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1),
                event -> {
                    updateRaceTime();
                    updateWindDirection();
                    updateOrder();
                    updateBoatSelectionComboBox();

                    for (Yacht yacht : StreamParser.getBoatsPos().values()) {

                        if (yacht.getNextMark() != null){
                            System.out.println("next Mark: " + yacht.getNextMark().getName());
                            for (BoatGroup bg : includedCanvasController.getBoatGroups()) {

                                Boolean isUpwindLeg = null;
                                // Can only calc leg direction if there is a next mark and it is a gate mark
                                Mark nextMark = bg.getBoat().getNextMark();
                                if (!(nextMark == null || !(nextMark instanceof GateMark))) {
                                    isUpwindLeg = bg.isUpwindLeg(includedCanvasController);
                                }

                                for (MarkGroup mg : includedCanvasController.getMarkGroups()) {
                                    if (mg.getMainMark().equals(nextMark)) {

                                    }
                                }
                                if (isUpwindLeg != null) {
                                    if (isUpwindLeg) {

                                    }
                                }


                            }

                        }
                    }

                })
        );

        // Start the timer
        timerTimeline.playFromStart();
    }


    /**
     * Updates the wind direction arrow and text as from info from the StreamParser
     */
    private void updateWindDirection() {
        windDirectionText.setText(String.format("%.1f°", StreamParser.getWindDirection()));
        windArrowText.setRotate(StreamParser.getWindDirection());
    }


    /**
     * Updates the clock for the race
     */
    private void updateRaceTime() {
        if (StreamParser.isRaceFinished()) {
            timerLabel.setFill(Color.RED);
            timerLabel.setText("Race Finished!");
        } else {
            timerLabel.setText(getTimeSinceStartOfRace());
        }
    }


    /**
     * Grabs the boats currently in the race as from the StreamParser and sets them to be selectable
     * in the boat selection combo box
     */
    private void updateBoatSelectionComboBox() {
        ObservableList<Yacht> observableBoats = FXCollections
            .observableArrayList(StreamParser.getBoatsPos().values());
        boatSelectionComboBox.setItems(observableBoats);
    }


    /**
     * Updates the order of the boats as from the StreamParser and sets them in the boat order
     * section
     */
    private void updateOrder() {
        positionVbox.getChildren().clear();
        positionVbox.getChildren().removeAll();
        positionVbox.getStylesheets().add(getClass().getResource("/css/master.css").toString());

        for (Yacht boat : StreamParser.getBoatsPos().values()) {
            if (boat.getBoatStatus() == 3) {  // 3 is finish status
                Text textToAdd = new Text(boat.getPosition() + ". " +
                    boat.getShortName() + " (Finished)");
                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                positionVbox.getChildren().add(textToAdd);

            } else {
                Text textToAdd = new Text(boat.getPosition() + ". " +
                    boat.getShortName() + " ");
                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                textToAdd.setStyle("");
                positionVbox.getChildren().add(textToAdd);
            }

        }
    }


    /**
     * Initialised the combo box with any boats currently in the race and adds the required listener
     * for the combobox to take action upon selection
     */
    private void initialiseBoatSelectionComboBox() {
        updateBoatSelectionComboBox();
        boatSelectionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            //This listener is fired whenever the combo box changes. This means when the values are updated
            //We dont want to set the selected value if the values are updated but nothing clicked (null)
            if (newValue != null && newValue != selectedBoat) {
                Yacht thisYacht = (Yacht) newValue;
                setSelectedBoat(thisYacht);
            }
        });
    }


    /**
     * Display the list of boats in the order they finished the race
     */
    private void loadRaceResultView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FinishView.fxml"));

        try {
            contentAnchorPane.getChildren().removeAll();
            contentAnchorPane.getChildren().clear();
            contentAnchorPane.getChildren().addAll((Pane) loader.load());

        } catch (javafx.fxml.LoadException e) {
            System.err.println(e.getCause());
        } catch (IOException e) {
            System.err.println(e);
        }
    }


    /**
     * Convert seconds to a string of the format mm:ss
     *
     * @param time the time in seconds
     * @return a formatted string
     */
    public String convertTimeToMinutesSeconds(int time) {
        if (time < 0) {
            return String.format("-%02d:%02d", (time * -1) / 60, (time * -1) % 60);
        }
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    private String getTimeSinceStartOfRace() {
        String timerString = "0:00";
        if (StreamParser.getTimeSinceStart() > 0) {
            String timerMinute = Long.toString(StreamParser.getTimeSinceStart() / 60);
            String timerSecond = Long.toString(StreamParser.getTimeSinceStart() % 60);
            if (timerSecond.length() == 1) {
                timerSecond = "0" + timerSecond;
            }
            timerString = "-" + timerMinute + ":" + timerSecond;
        } else {
            String timerMinute = Long.toString(-1 * StreamParser.getTimeSinceStart() / 60);
            String timerSecond = Long.toString(-1 * StreamParser.getTimeSinceStart() % 60);
            if (timerSecond.length() == 1) {
                timerSecond = "0" + timerSecond;
            }
            timerString = timerMinute + ":" + timerSecond;
        }
        return timerString;
    }


    public boolean isDisplayFps() {
        return displayFps;
    }

    /**
     * Display the important annotations for a specific BoatGroup
     * @param bg The boat group to set the annotations for
     */
    private void setBoatGroupImportantAnnotations(BoatGroup bg) {
        if (importantAnnotations.getAnnotationState(Annotation.NAME)) {
            bg.setTeamNameObjectVisible(true);
        } else {
            bg.setTeamNameObjectVisible(false);
        }

        if (importantAnnotations.getAnnotationState(Annotation.SPEED)) {
            bg.setVelocityObjectVisible(true);
        } else {
            bg.setVelocityObjectVisible(false);
        }

        if (importantAnnotations.getAnnotationState(Annotation.TRACK)) {
            bg.setLineGroupVisible(true);
        } else {
            bg.setLineGroupVisible(false);
        }

        if (importantAnnotations.getAnnotationState(Annotation.WAKE)) {
            bg.setWakeVisible(true);
        } else {
            bg.setWakeVisible(false);
        }
        //TODO fix boat annotations with new boatgroup
        if (importantAnnotations.getAnnotationState(Annotation.ESTTIMETONEXTMARK)) {
            bg.setEstTimeToNextMarkObjectVisible(true);
        } else {
            bg.setEstTimeToNextMarkObjectVisible(false);
        }

        if (importantAnnotations.getAnnotationState(Annotation.LEGTIME)) {
            bg.setLegTimeObjectVisible(true);
        } else {
            bg.setLegTimeObjectVisible(false);
        }
    }

    private void setAnnotations(Integer annotationLevel) {
        switch (annotationLevel) {
            // No Annotations
            case 0:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                    bg.setTeamNameObjectVisible(false);
                    bg.setVelocityObjectVisible(false);
                    bg.setEstTimeToNextMarkObjectVisible(false);
                    bg.setLegTimeObjectVisible(false);
                    bg.setLineGroupVisible(false);
                    bg.setWakeVisible(false);
                }
                break;
            // Important Annotations
            case 1:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                    setBoatGroupImportantAnnotations(bg);
                }
                break;
            // All Annotations
            case 2:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                    bg.setTeamNameObjectVisible(true);
                    bg.setVelocityObjectVisible(true);
                    bg.setEstTimeToNextMarkObjectVisible(true);
                    bg.setLegTimeObjectVisible(true);
                    bg.setLineGroupVisible(true);
                    bg.setWakeVisible(true);
                }
                break;
        }
    }


    /**
     * Sets all the annotations of the selected boat to be visible and all others to be hidden
     *
     * @param yacht The yacht for which we want to view all annotations
     */
    private void setSelectedBoat(Yacht yacht) {
        for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
            //We need to iterate over all race groups to get the matching boat group belonging to this boat if we
            //are to toggle its annotations, there is no other backwards knowledge of a yacht to its boatgroup.
            if (bg.getBoat().getHullID().equals(yacht.getHullID())) {
                bg.setIsSelected(true);
                selectedBoat = yacht;
            } else {
                bg.setIsSelected(false);
            }
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    Stage getStage() {
        return stage;
    }
}