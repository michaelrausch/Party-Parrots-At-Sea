package seng302.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Yacht class for the racing boat.
 *
 * Class created to store more variables (eg. boat statuses) compared to the XMLParser boat class,
 *  also done outside Boat class because some old variables are not used anymore.
 */
public class Yacht {
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

    public Yacht(String boatType, Integer sourceID, String hullID, String shortName, String boatName, String country) {
        this.boatType = boatType;
        this.sourceID = sourceID;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
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
        this.position = position;
    }
}
