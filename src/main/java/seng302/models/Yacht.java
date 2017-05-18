package seng302.models;

import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Yacht class for the racing boat.
 *
 * Class created to store more variables (eg. boat statuses) compared to the XMLParser boat class,
 *  also done outside Boat class because some old variables are not used anymore.
 */
public class Yacht {
    // Used in boat group
    private Color colour;
    private double velocity;

    private int index;
    private ConcurrentLinkedQueue<Number> dataQ1;

    private String boatType;
    private Integer sourceID;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;
    // Boat status
    private Integer boatStatus;
    private Integer legNumber;
    private Integer penaltiesAwarded;
    private Integer penaltiesServed;
    private Long estimateTimeAtNextMark;
    private Long estimateTimeAtFinish;
    private String position;
    // Mark rounding
    private Long markRoundingTime;

    // Used for sparkline
    private XYChart.Series<Number, Number> sparklinePosition;

    /**
     * Used in EventTest and RaceTest.
     *
     * @param boatName Create a yacht object with name.
     */
    public Yacht (String boatName) {
        this.boatName = boatName;
    }

    /**
     * Used in BoatGroupTest.
     *
     * @param boatName     The name of the team sailing the boat
     * @param boatVelocity The speed of the boat in meters/second
     * @param shortName    A shorter version of the teams name
     */
    public Yacht(String boatName, double boatVelocity, String shortName, int id) {
        this.boatName = boatName;
        this.velocity = boatVelocity;
        this.shortName = shortName;
        this.sourceID = id;
        this.sparklinePosition = new XYChart.Series<>();
        this.sparklinePosition.setName(boatName);
        index = 0;
        dataQ1 = new ConcurrentLinkedQueue<>();

    }

    public Yacht(String boatType, Integer sourceID, String hullID, String shortName, String boatName, String country) {
        this.boatType = boatType;
        this.sourceID = sourceID;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
        this.sparklinePosition = new XYChart.Series<>();
        this.sparklinePosition.setName(boatName);
        index = 0;
        dataQ1 = new ConcurrentLinkedQueue<>();
    }

    public String getBoatType() {
        return boatType;
    }
    public Integer getSourceID() {
        return sourceID;
    }
    public String getHullID() {
        return hullID;
    }
    public String getShortName() {
        return shortName;
    }
    public String getBoatName() {
        return boatName;
    }
    public String getCountry() {
        return country;
    }

    public Integer getBoatStatus() {
        return boatStatus;
    }

    public void setBoatStatus(Integer boatStatus) {
        this.boatStatus = boatStatus;
    }

    public Integer getLegNumber() {
        return legNumber;
    }

    public void setLegNumber(Integer legNumber) {
        this.legNumber = legNumber;
    }

    public Integer getPenaltiesAwarded() {
        return penaltiesAwarded;
    }

    public void setPenaltiesAwarded(Integer penaltiesAwarded) {
        this.penaltiesAwarded = penaltiesAwarded;
    }

    public Integer getPenaltiesServed() {
        return penaltiesServed;
    }

    public void setPenaltiesServed(Integer penaltiesServed) {
        this.penaltiesServed = penaltiesServed;
    }

    public Long getEstimateTimeAtNextMark() {
//        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//        return format.format(estimateTimeAtNextMark);
        return estimateTimeAtNextMark;
    }

    public void setEstimateTimeAtNextMark(Long estimateTimeAtNextMark) {
        this.estimateTimeAtNextMark = estimateTimeAtNextMark;
    }

    public String getEstimateTimeAtFinish() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(estimateTimeAtFinish);
    }

    public void setEstimateTimeAtFinish(Long estimateTimeAtFinish) {
        this.estimateTimeAtFinish = estimateTimeAtFinish;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
            try {
                if (Integer.parseInt(position) != Integer.parseInt(this.position)){
                dataQ1.add(Integer.parseInt(position));
                sparklinePosition.getData().add(new XYChart.Data<>(index++, dataQ1.remove()));
                System.out.println("new position found for " + boatName + " in position " + position + " old(" + this.position + ")");
                }
            } catch (NumberFormatException e) {
                System.out.println("No position found");
            }
        this.position = position;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public Long getMarkRoundingTime() {
        return markRoundingTime;
    }

    public void setMarkRoundingTime(Long markRoundingTime) {
        this.markRoundingTime = markRoundingTime;
    }

    public Series<Number, Number> getSparklinePosition() {
        return sparklinePosition;
    }

    @Override
    public String toString() {
        return boatName;
    }
}
