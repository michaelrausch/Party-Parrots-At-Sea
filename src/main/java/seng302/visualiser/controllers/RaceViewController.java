package seng302.visualiser.controllers;

import java.util.concurrent.TimeUnit;
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
import javafx.scene.chart.XYChart.Data;
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
import seng302.model.Corner;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.visualiser.GameView;
import seng302.visualiser.controllers.annotations.Annotation;
import seng302.visualiser.controllers.annotations.ImportantAnnotationController;
import seng302.visualiser.controllers.annotations.ImportantAnnotationDelegate;
import seng302.visualiser.controllers.annotations.ImportantAnnotationsState;
import seng302.visualiser.fxObjects.BoatObject;
import seng302.model.*;
import seng302.model.mark.Mark;

import java.io.IOException;
import java.util.*;

/**
 * Controller class that manages the display of a race
 */
public class RaceViewController extends Thread implements ImportantAnnotationDelegate {

    @FXML
    private LineChart<String, Double> raceSparkLine;
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
    private ComboBox<Yacht> yachtSelectionComboBox;

    //Race Data
    private Map<Integer, Yacht> participants;
    private Map<Integer, Mark> markers;
    private RaceXMLData courseData;
    private GameView gameView;
    private RaceState raceState;

    private Timeline timerTimeline;
    private HashMap<Integer, Series<String, Double>> sparkLineData = new HashMap<>();
    private ImportantAnnotationsState importantAnnotations;

    public void initialize() {
        // Load a default important annotation state
        importantAnnotations = new ImportantAnnotationsState();

        //Formatting the y axis of the sparkline
        raceSparkLine.getYAxis().setRotate(180);
        raceSparkLine.getYAxis().setTickLabelRotation(180);
        raceSparkLine.getYAxis().setTranslateX(-5);
        raceSparkLine.getYAxis().setAutoRanging(false);
        sparklineYAxis.setTickMarkVisible(false);

        selectAnnotationBtn.setOnAction(event -> loadSelectAnnotationView());
    }

    public void loadRace (Map<Integer, Yacht> participants, RaceXMLData raceData, RaceState raceState) {
        this.participants = participants;
        this.courseData = raceData;
        this.markers = raceData.getCompoundMarks();
        this.raceState = raceState;

        initializeUpdateTimer();
        initialiseFPSCheckBox();
        initialiseAnnotationSlider();
        initialiseBoatSelectionComboBox();

        gameView = new GameView();
        gameView.setBoats(new ArrayList<>(participants.values()));
        gameView.updateBorder(raceData.getCourseLimit());
        gameView.updateCourse(new ArrayList<>(raceData.getCompoundMarks().values()));
        gameView.startRace();

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
            ImportantAnnotationController controller = new ImportantAnnotationController(
                this, stage
            );
            fxmlLoader.setController(controller);
            // Load FXML and set CSS
            fxmlLoader.setLocation(
                getClass().getResource("/views/importantAnnotationSelectView.fxml")
            );
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
        toggleFps.selectedProperty().addListener((obs, oldVal, newVal) ->
            gameView.setFPSVisibility(toggleFps.isSelected())
        );
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

        annotationSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            setAnnotations((int) annotationSlider.getValue())
        );
        annotationSlider.setValue(2);
    }


    /**
     * Used to add any new yachts into the race that may have started late or not have had data received yet
     */
    private void updateSparkLine(){
        // Collect the racing yachts that aren't already in the chart
        List<Yacht> sparkLineCandidates = new ArrayList<>();
//        participants.forEach((id, yacht) ->{
//            if (!sparkLineData.containsKey(id) && yacht.getPosition() != null && !yacht.getPosition().equals("-"))
//                sparkLineCandidates.add(yacht);
//        });
        participants.forEach((id, yacht) -> sparkLineCandidates.add(yacht));

        sparklineYAxis.setUpperBound(participants.size() + 1);

        // Create a new data series for new yachts
        sparkLineCandidates.stream().filter(yacht -> yacht.getPositionInteger() != null).forEach(yacht -> {
            Series<String, Double> yachtData = new Series<>();
            yachtData.setName(yacht.getSourceId().toString());
            yachtData.getData().add(
                new XYChart.Data<>(
                    Integer.toString(yacht.getLegNumber()),
                    1.0 + participants.size() - yacht.getPositionInteger()
                )
            );
            sparkLineData.put(yacht.getSourceId(), yachtData);
        });

        // Lambda function to sort the series in order of leg (later legs shown more to the right)
        List<XYChart.Series<String, Double>> positions = new ArrayList<>(sparkLineData.values());
        positions.sort((o1, o2) -> {
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
        positions
            .stream()
            .filter(spark -> !raceSparkLine.getData().contains(spark))
            .forEach(spark -> {
                raceSparkLine.getData().add(spark);
                spark.getNode().lookup(".chart-series-line").setStyle("-fx-stroke:" + getBoatColorAsRGB(spark.getName()));
            });
    }


    /**
     * Updates the yachts sparkline of the desired yacht and using the new leg number
     * @param yacht The yacht to be updated on the sparkline
     * @param legNumber the leg number that the position will be assigned to
     */
    public void updateYachtPositionSparkline(Yacht yacht, Integer legNumber){
        for (XYChart.Series<String, Double> positionData : sparkLineData.values()) {
            positionData.getData().add(
                new Data<>(
                    Integer.toString(legNumber),
                    1.0 + participants.size() - yacht.getPositionInteger()
                )
            );
        }
//        XYChart.Series<String, Double> positionData =  sparkLineData.get(yacht.getSourceID());
//        positionData.getData().add(
//            new XYChart.Data<>(
//                Integer.toString(legNumber),
//                1.0 + participants.size() - yacht.getPosition()
//            )
//        );
    }


    /**
     * gets the rgb string of the yachts colour to use for the chart via css
     * @param yachtId id of yacht passed in to get the yachts colour
     * @return the colour as an rgb string
     */
    private String getBoatColorAsRGB(String yachtId){
        Color color = participants.get(Integer.valueOf(yachtId)).getColour();
        if (color == null){
            return String.format("#%02X%02X%02X",255,255,255);
        }
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 )
        );
    }


    /**
     * Initialises a timer which updates elements of the RaceView such as wind direction, yacht
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
                    updateSparkLine();
                })
        );
        // Start the timer
        timerTimeline.playFromStart();
    }


    /**
     * Iterates over all corners until ones SeqID matches with the yachts current leg number.
     * Then it gets the compoundMarkID of that corner and uses it to fetch the appropriate mark
     * Returns null if no next mark found.
     * @param bg The BoatGroup to find the next mark of
     * @return The next Mark or null if none found
     */
    private Mark getNextMark(BoatObject bg) {
//
//        Integer legNumber = bg.getYacht().getLegNumber();
//        List<Corner> markSequence = courseData.getMarkSequence();
//
//        if (legNumber == 0) {
//            return null;
//        } else if (legNumber == markSequence.size() - 1) {
//            return null;
//        }
//
//        for (Corner corner : markSequence) {
//            if (legNumber + 2 == corner.getSeqID()) {
//                return courseData.getCompoundMarks().get(corner.getCompoundMarkID());
//            }
//        }
        return null;
    }


    /**
     * Updates the wind direction arrow and text as from info from the StreamParser
     */
    private void updateWindDirection() {
        windDirectionText.setText(String.format("%.1f°", raceState.getWindDirection()));
        windArrowText.setRotate(raceState.getWindDirection());
    }


    /**
     * Updates the clock for the race
     */
    private void updateRaceTime() {
        if (!raceState.isRaceStarted()) {
            timerLabel.setFill(Color.RED);
            timerLabel.setText("Race Finished!");
        } else {
            timerLabel.setText(raceState.getRaceTimeStr());
        }
    }

    /**
     * Updates the order of the yachts as from the StreamParser and sets them in the yacht order
     * section
     */
    private void updateOrder() {
        positionVbox.getChildren().clear();
        positionVbox.getChildren().removeAll();
        positionVbox.getStylesheets().add(getClass().getResource("/css/master.css").toString());

        // list of racing yacht id
        List<Yacht> sorted = new ArrayList<>(participants.values());
        sorted.sort(Comparator.comparingInt(Yacht::getPositionInteger));

        for (Yacht yacht : sorted) {
            if (yacht.getBoatStatus() == 3) {  // 3 is finish status
                Text textToAdd = new Text(yacht.getPositionInteger() + ". " +
                    yacht.getShortName() + " (Finished)");
                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                positionVbox.getChildren().add(textToAdd);

            } else {
                Text textToAdd = new Text(yacht.getPositionInteger() + ". " +
                    yacht.getShortName() + " ");
                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                textToAdd.setStyle("");
                positionVbox.getChildren().add(textToAdd);
            }
        }
//            participants.forEach((id, yacht) ->{
//                Text textToAdd = new Text(yacht.getPosition() + ". " +
//                    yacht.getShortName() + " ");
//                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
//                textToAdd.setStyle("");
//                positionVbox.getChildren().add(textToAdd);
//            });
    }


//    private void updateLaylines(BoatObject bg) {
//
//        Mark nextMark = getNextMark(bg);
//        Boolean isUpwind = null;
//        // Can only calc leg direction if there is a next mark and it is a gate mark
//        if (nextMark != null) {
//            if (nextMark instanceof GateMark) {
//                if (bg.isUpwindLeg(gameViewController, nextMark)) {
//                    isUpwind = true;
//                } else {
//                    isUpwind = false;
//                }
//
//                for(MarkObject mg : gameViewController.getMarkGroups()) {
//
//                    mg.removeLaylines();
//
//                    if (mg.getMainMark().getId() == nextMark.getId()) {
//
//                        SingleMark singleMark1 = ((GateMark) nextMark).getSingleMark1();
//                        SingleMark singleMark2 = ((GateMark) nextMark).getSingleMark2();
//                        Point2D markPoint1 = gameViewController
//                            .findScaledXY(singleMark1.getLatitude(), singleMark1.getLongitude());
//                        Point2D markPoint2 = gameViewController
//                            .findScaledXY(singleMark2.getLatitude(), singleMark2.getLongitude());
//                        HashMap<Double, Double> angleAndSpeed;
//                        if (isUpwind) {
//                            angleAndSpeed = PolarTable.getOptimalUpwindVMG(StreamParser.getWindSpeed());
//                        } else {
//                            angleAndSpeed = PolarTable.getOptimalDownwindVMG(StreamParser.getWindSpeed());
//                        }
//
//                        Double resultingAngle = angleAndSpeed.keySet().iterator().next();
//
//
//                        Point2D yachtCurrentPos = new Point2D(bg.getBoatLayoutX(), bg.getBoatLayoutY());
//                        Point2D gateMidPoint = markPoint1.midpoint(markPoint2);
//                        Integer lineFuncResult = GeoUtility.lineFunction(yachtCurrentPos, gateMidPoint, markPoint2);
//                        Line rightLayline = new Line();
//                        Line leftLayline = new Line();
//                        if (lineFuncResult == 1) {
//                            rightLayline = makeRightLayline(markPoint2, 180 - resultingAngle, StreamParser.getWindDirection());
//                            leftLayline = makeLeftLayline(markPoint1, 180 - resultingAngle, StreamParser.getWindDirection());
//                        } else if (lineFuncResult == -1) {
//                            rightLayline = makeRightLayline(markPoint1, 180 - resultingAngle, StreamParser.getWindDirection());
//                            leftLayline = makeLeftLayline(markPoint2, 180 - resultingAngle, StreamParser.getWindDirection());
//                        }
//
//                        leftLayline.setStrokeWidth(0.5);
//                        leftLayline.setStroke(bg.getBoat().getColour());
//
//                        rightLayline.setStrokeWidth(0.5);
//                        rightLayline.setStroke(bg.getBoat().getColour());
//
//                        bg.setLaylines(leftLayline, rightLayline);
//                        mg.addLaylines(leftLayline, rightLayline);
//
//                    }
//                }
//            }
//        }
//    }


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
     * Initialised the combo box with any yachts currently in the race and adds the required listener
     * for the combobox to take action upon selection
     */
    private void initialiseBoatSelectionComboBox() {
        yachtSelectionComboBox.setItems(
            FXCollections.observableArrayList(participants.values())
        );
        //Null check is if the listener is fired but nothing selected
        yachtSelectionComboBox.valueProperty().addListener((obs, lastSelection, selectedBoat) -> {
            if (selectedBoat != null) {
                gameView.selectBoat(selectedBoat);
            }
        });
    }

    /**
     * Grabs the yachts currently in the race as from the StreamParser and sets them to be selectable
     * in the yacht selection combo box
     */
    private void updateBoatSelectionComboBox() {
        ObservableList<Yacht> observableYachts = FXCollections.observableArrayList();
        observableYachts.addAll(participants.values());
        yachtSelectionComboBox.setItems(observableYachts);
    }


    /**
     * Display the list of yachts in the order they finished the race
     */
    private void loadRaceResultView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FinishView.fxml"));

        try {
            contentAnchorPane.getChildren().removeAll();
            contentAnchorPane.getChildren().clear();
            contentAnchorPane.getChildren().addAll((Pane) loader.load());

        } catch (javafx.fxml.LoadException e) {
            System.err.println(e.getCause().toString());
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private String getMillisToFormattedTime(long milliseconds) {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliseconds),
            TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60, //Modulus 60 minutes per hour
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60  //Modulus 60 seconds per minute
            );
    }

    private void setAnnotations(Integer annotationLevel) {
        switch (annotationLevel) {
            // No Annotations
            case 0:
                gameView.setAnnotationVisibilities(
                    false, false, false, false, false, false
                );
                break;
            // Important Annotations
            case 1:
                gameView.setAnnotationVisibilities(
                    importantAnnotations.getAnnotationState(Annotation.NAME),
                    importantAnnotations.getAnnotationState(Annotation.SPEED),
                    importantAnnotations.getAnnotationState(Annotation.ESTTIMETONEXTMARK),
                    importantAnnotations.getAnnotationState(Annotation.LEGTIME),
                    importantAnnotations.getAnnotationState(Annotation.TRACK),
                    importantAnnotations.getAnnotationState(Annotation.WAKE)
                );
                break;
            // All Annotations
            case 2:
                gameView.setAnnotationVisibilities(
                    true, true, true, true, true, true
                );
                break;
        }
    }


    /**
     * Sets all the annotations of the selected yacht to be visible and all others to be hidden
     *
     * @param yacht The yacht for which we want to view all annotations
     */
    private void setSelectedBoat(Yacht yacht) {
//        for (BoatObject bg : gameViewController.getBoatGroups()) {
//            //We need to iterate over all race groups to get the matching yacht group belonging to this yacht if we
//            //are to toggle its annotations, there is no other backwards knowledge of a yacht to its yachtgroup.
//            if (bg.getBoat().getHullID().equals(yacht.getHullID())) {
////                updateLaylines(bg);
//                bg.setIsSelected(true);
////                selectedBoat = yacht;
//            } else {
//                bg.setIsSelected(false);
//            }
//        }
    }

    public void updateRaceData (RaceXMLData raceData) {
        this.courseData = raceData;
        gameView.updateBorder(raceData.getCourseLimit());
    }

}