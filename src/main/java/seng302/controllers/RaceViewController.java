package seng302.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import seng302.utilities.GeoUtility;
import seng302.controllers.annotations.Annotation;
import seng302.controllers.annotations.ImportantAnnotationController;
import seng302.controllers.annotations.ImportantAnnotationDelegate;
import seng302.controllers.annotations.ImportantAnnotationsState;
import seng302.fxObjects.BoatGroup;
import seng302.fxObjects.MarkGroup;
import seng302.models.*;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.SingleMark;
import seng302.models.stream.StreamParser;
import seng302.models.stream.XMLParser;

import java.io.IOException;
import java.util.*;
import seng302.models.stream.XMLParser.RaceXMLObject.Participant;
import java.util.stream.Collectors;

/**
 * Created by ptg19 on 29/03/17.
 */
public class RaceViewController extends Thread implements ImportantAnnotationDelegate {

    @FXML
    private LineChart raceSparkLine;
    @FXML
    private NumberAxis sparklineYAxis;
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

    private static ArrayList<Yacht> startingBoats = new ArrayList<>();
    private boolean displayFps;
    private Timeline timerTimeline;
    private Stage stage;
    private static HashMap<Integer, Series<String, Double>> sparkLineData = new HashMap<>();
    private static ArrayList<Yacht> racingBoats = new ArrayList<>();
    private ImportantAnnotationsState importantAnnotations;
    private Yacht selectedBoat;

    public void initialize() {
        // Load a default important annotation state
        importantAnnotations = new ImportantAnnotationsState();

        //Formatting the y axis of the sparkline
        raceSparkLine.getYAxis().setRotate(180);
        raceSparkLine.getYAxis().setTickLabelRotation(180);
        raceSparkLine.getYAxis().setTranslateX(-5);
        raceSparkLine.getYAxis().setAutoRanging(false);
        sparklineYAxis.setTickMarkVisible(false);
        startingBoats = new ArrayList<>(StreamParser.getBoats().values());

        includedCanvasController.setup(this);
        includedCanvasController.initializeCanvas();
        initializeUpdateTimer();
        initialiseFPSCheckBox();
        initialiseAnnotationSlider();
        initialiseBoatSelectionComboBox();
        includedCanvasController.timer.start();
        selectAnnotationBtn.setOnAction(event -> loadSelectAnnotationView());
    }


    /**
     * The important annotations have been changed, update this view
     *
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
     * Used to add any new boats into the race that may have started late or not have had data received yet
     */
    void updateSparkLine(){
        // Collect the racing boats that aren't already in the chart
        ArrayList<Yacht> sparkLineCandidates = startingBoats.stream().filter(yacht -> !sparkLineData.containsKey(yacht.getSourceID())
                && yacht.getPosition() != null & yacht.getPosition() != "-").collect(Collectors.toCollection(ArrayList::new));

        // Obtain the qualifying boats to set the max on the Y axis
        racingBoats = startingBoats.stream().filter(yacht ->
        yacht.getPosition() != null & yacht.getPosition() != "-").collect(Collectors.toCollection(ArrayList::new));
        sparklineYAxis.setUpperBound(racingBoats.size() + 1);

        // Create a new data series for new boats
        sparkLineCandidates.stream().filter(yacht -> yacht.getPosition() != null).forEach(yacht -> {
            Series<String, Double> yachtData = new Series<>();
            yachtData.setName(yacht.getBoatName());
            yachtData.getData().add(new XYChart.Data<>(Integer.toString(yacht.getLegNumber()), 1 + racingBoats.size() - Double.parseDouble(yacht.getPosition())));
            sparkLineData.put(yacht.getSourceID(), yachtData);
        });

        // Lambda function to sort the series in order of leg (later legs shown more to the right)
        List<XYChart.Series<String, Double>> positions = new ArrayList<>(sparkLineData.values());
        Collections.sort(positions, (o1, o2) -> {
            Integer leg1 =  Integer.parseInt(o1.getData().get(o1.getData().size()-1).getXValue());
            Integer leg2 =  Integer.parseInt(o2.getData().get(o2.getData().size()-1).getXValue());
            if (leg2 < leg1){
                return 1;
            } else {
                return -1;
            }
        });

        // Adds the new data series to the sparkline (and set the colour of the series)
        raceSparkLine.setCreateSymbols(false);
        positions.stream().filter(spark -> !raceSparkLine.getData().contains(spark)).forEach(spark -> {
            raceSparkLine.getData().add(spark);
            spark.getNode().lookup(".chart-series-line").setStyle("-fx-stroke:" + getBoatColorAsRGB(spark.getName()));
        });
    }


    /**
     * Updates the yachts sparkline of the desired boat and using the new leg number
     * @param yacht The yacht to be updated on the sparkline
     * @param legNumber the leg number that the position will be assigned to
     */
    public static void updateYachtPositionSparkline(Yacht yacht, Integer legNumber){
        XYChart.Series<String, Double> positionData =  sparkLineData.get(yacht.getSourceID());
        positionData.getData().add(new XYChart.Data<>(Integer.toString(legNumber), 1 + racingBoats.size() - Double.parseDouble(yacht.getPosition())));
    }


    /**
     * gets the rgb string of the boats colour to use for the chart via css
     * @param boatName boat passed in to get the boats colour
     * @return the colour as an rgb string
     */
    private String getBoatColorAsRGB(String boatName){
        Color color = Color.WHITE;
        for (Yacht yacht: startingBoats){
            if (Objects.equals(yacht.getBoatName(), boatName)){
                color = yacht.getColour();
            }
        }
        if (color == null){
            return String.format( "#%02X%02X%02X",255,255,255);
        }
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ) );
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
                })
        );

        // Start the timer
        timerTimeline.playFromStart();
    }


    /**
     * Iterates over all corners until ones SeqID matches with the boats current leg number.
     * Then it gets the compoundMarkID of that corner and uses it to fetch the appropriate mark
     * Returns null if no next mark found.
     * @param bg The BoatGroup to find the next mark of
     * @return The next Mark or null if none found
     */
    private Mark getNextMark(BoatGroup bg) {
        Integer legNumber = bg.getBoat().getLegNumber();

        List<XMLParser.RaceXMLObject.Corner> markSequence = StreamParser.getXmlObject().getRaceXML().getCompoundMarkSequence();

        if (legNumber == 0) {
            return null;
        } else if (legNumber == markSequence.size() - 1) {
            return null;
        }

        for (XMLParser.RaceXMLObject.Corner corner : markSequence) {
            if (legNumber + 2 == corner.getSeqID()) {
                Integer thisCompoundMarkID = corner.getCompoundMarkID();

                for (Mark mark : StreamParser.getXmlObject().getRaceXML().getAllCompoundMarks()) {
                    if (mark.getCompoundMarkID() == thisCompoundMarkID) {
                        return mark;
                    }
                }
            }
        }

        return null;
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

        // list of racing boat id
        ArrayList<Participant> participants = StreamParser.getXmlObject().getRaceXML()
            .getParticipants();
        ArrayList<Integer> participantIDs = new ArrayList<>();
        for (Participant p : participants) {
            participantIDs.add(p.getsourceID());
        }

        if (StreamParser.isRaceStarted()) {
            for (Yacht boat : StreamParser.getBoatsPos().values()) {
                if (participantIDs.contains(boat.getSourceID())) {  // check if the boat is racing
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
        } else {
            for (Yacht boat : StreamParser.getBoats().values()) {
                if (participantIDs.contains(boat.getSourceID())) {  // check if the boat is racing
                    Text textToAdd = new Text(boat.getPosition() + ". " +
                        boat.getShortName() + " ");
                    textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                    textToAdd.setStyle("");
                    positionVbox.getChildren().add(textToAdd);
                }
            }
        }
    }


    private void updateLaylines(BoatGroup bg) {

        Mark nextMark = getNextMark(bg);
        Boolean isUpwind = null;
        // Can only calc leg direction if there is a next mark and it is a gate mark
        if (nextMark != null) {
            if (nextMark instanceof GateMark) {
                if (bg.isUpwindLeg(includedCanvasController, nextMark)) {
                    isUpwind = true;
                } else {
                    isUpwind = false;
                }

                for(MarkGroup mg : includedCanvasController.getMarkGroups()) {

                    mg.removeLaylines();

                    if (mg.getMainMark().getId() == nextMark.getId()) {

                        SingleMark singleMark1 = ((GateMark) nextMark).getSingleMark1();
                        SingleMark singleMark2 = ((GateMark) nextMark).getSingleMark2();
                        Point2D markPoint1 = includedCanvasController.findScaledXY(singleMark1.getLatitude(), singleMark1.getLongitude());
                        Point2D markPoint2 = includedCanvasController.findScaledXY(singleMark2.getLatitude(), singleMark2.getLongitude());
                        HashMap<Double, Double> angleAndSpeed;
                        if (isUpwind) {
                            angleAndSpeed = PolarTable.getOptimalUpwindVMG(StreamParser.getWindSpeed());
                        } else {
                            angleAndSpeed = PolarTable.getOptimalDownwindVMG(StreamParser.getWindSpeed());
                        }

                        Double resultingAngle = angleAndSpeed.keySet().iterator().next();


                        Point2D boatCurrentPos = new Point2D(bg.getBoatLayoutX(), bg.getBoatLayoutY());
                        Point2D gateMidPoint = markPoint1.midpoint(markPoint2);
                        Integer lineFuncResult = GeoUtility.lineFunction(boatCurrentPos, gateMidPoint, markPoint2);
                        Line rightLayline = new Line();
                        Line leftLayline = new Line();
                        if (lineFuncResult == 1) {
                            rightLayline = makeRightLayline(markPoint2, 180 - resultingAngle, StreamParser.getWindDirection());
                            leftLayline = makeLeftLayline(markPoint1, 180 - resultingAngle, StreamParser.getWindDirection());
                        } else if (lineFuncResult == -1) {
                            rightLayline = makeRightLayline(markPoint1, 180 - resultingAngle, StreamParser.getWindDirection());
                            leftLayline = makeLeftLayline(markPoint2, 180 - resultingAngle, StreamParser.getWindDirection());
                        }

                        leftLayline.setStrokeWidth(0.5);
                        leftLayline.setStroke(bg.getBoat().getColour());

                        rightLayline.setStrokeWidth(0.5);
                        rightLayline.setStroke(bg.getBoat().getColour());

                        bg.setLaylines(leftLayline, rightLayline);
                        mg.addLaylines(leftLayline, rightLayline);

                    }
                }
            }
        }
    }


    private Point2D getPointRotation(Point2D ref, Double distance, Double angle){
        Double newX = ref.getX() + (ref.getX() + distance -ref.getX())*Math.cos(angle) - (ref.getY() + distance -ref.getY())*Math.sin(angle);
        Double newY = ref.getY() + (ref.getX() + distance -ref.getX())*Math.sin(angle) + (ref.getY() + distance -ref.getY())*Math.cos(angle);

        return new Point2D(newX, newY);
    }


    public Line  makeLeftLayline(Point2D startPoint, Double layLineAngle, Double baseAngle) {

        Point2D ep = getPointRotation(startPoint, 50.0, baseAngle + layLineAngle);
        Line line = new Line(startPoint.getX(), startPoint.getY(), ep.getX(), ep.getY());
        return line;

    }


    public Line makeRightLayline(Point2D startPoint, Double layLineAngle, Double baseAngle) {

        Point2D ep = getPointRotation(startPoint, 50.0, baseAngle - layLineAngle);
        Line line = new Line(startPoint.getX(), startPoint.getY(), ep.getX(), ep.getY());
        return line;

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


    boolean isDisplayFps() {
        return displayFps;
    }

    private void setAnnotations(Integer annotationLevel) {
        switch (annotationLevel) {
            // No Annotations
            case 0:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                    bg.setVisibility(false, false, false, false, false, false);
                }
                break;
            // Important Annotations
            case 1:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                    bg.setVisibility(
                            importantAnnotations.getAnnotationState(Annotation.NAME),
                            importantAnnotations.getAnnotationState(Annotation.SPEED),
                            importantAnnotations.getAnnotationState(Annotation.ESTTIMETONEXTMARK),
                            importantAnnotations.getAnnotationState(Annotation.LEGTIME),
                            importantAnnotations.getAnnotationState(Annotation.TRACK),
                            importantAnnotations.getAnnotationState(Annotation.WAKE)
                    );
                }
                break;
            // All Annotations
            case 2:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                    bg.setVisibility(true, true, true, true, true, true);
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
                updateLaylines(bg);
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

    /**
     * Used for when the boat attempts to add data to the sparkline (first checks if the sparkline contains info on it)
     * @param yachtId
     * @return
     */
    public static boolean sparkLineStatus(Integer yachtId) {
        return sparkLineData.containsKey(yachtId);
    }
}