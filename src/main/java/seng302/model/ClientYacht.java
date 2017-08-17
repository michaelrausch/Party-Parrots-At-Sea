package seng302.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.model.mark.CompoundMark;

/**
 * Yacht class for the racing boat. <p> Class created to store more variables (eg. boat statuses)
 * compared to the XMLParser boat class, also done outside Boat class because some old variables are
 * not used anymore.
 */
public class ClientYacht extends Observable {

    @FunctionalInterface
    public interface YachtLocationListener {
        void notifyLocation(ClientYacht clientYacht, double lat, double lon, double heading,
            Boolean sailsIn, double velocity);
    }

    @FunctionalInterface
    public interface MarkRoundingListener {
        void notifyRounding(ClientYacht yacht, CompoundMark markPassed, int legNumber);
    }

    private Logger logger = LoggerFactory.getLogger(ClientYacht.class);


    private String boatType;
    private Integer sourceId;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;
    private Integer position;

    private Long estimateTimeAtFinish;
    private Boolean sailIn = true;
    private Integer currentMarkSeqID = 0;
    private Long markRoundTime;
    private Long timeTillNext;
    private Double heading;
    private Integer legNumber = 0;
    private GeoPoint location;
    private Integer boatStatus;
    private Double currentVelocity;

    private List<YachtLocationListener> locationListeners = new ArrayList<>();
    private List<MarkRoundingListener> markRoundingListeners = new ArrayList<>();
    private ReadOnlyDoubleWrapper velocityProperty = new ReadOnlyDoubleWrapper();
    private ReadOnlyLongWrapper timeTillNextProperty = new ReadOnlyLongWrapper();
    private ReadOnlyLongWrapper timeSinceLastMarkProperty = new ReadOnlyLongWrapper();
    private ReadOnlyIntegerWrapper placingProperty = new ReadOnlyIntegerWrapper();
    private CompoundMark lastMarkRounded;
    private Color colour;

    public ClientYacht(String boatType, Integer sourceId, String hullID, String shortName,
        String boatName, String country) {
        this.boatType = boatType;
        this.sourceId = sourceId;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
        this.location = new GeoPoint(57.670341, 11.826856);
        this.heading = 120.0;   //In degrees
        this.currentVelocity = 0d;
        this.boatStatus = 1;
    }

    /**
     * Add ServerToClientThread as the observer, this observer pattern mainly server for the boat
     * rounding package.
     */
    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);
    }

    public String getBoatType() {
        return boatType;
    }

    public Integer getSourceId() {
        //@TODO Remove and merge with Creating Game Loop
        if (sourceId == null) {
            return 0;
        }
        return sourceId;
    }

    public String getHullID() {
        if (hullID == null) {
            return "";
        }
        return hullID;
    }

    public String getShortName() {
        return shortName;
    }

    public String getBoatName() {
        return boatName;
    }

    public String getCountry() {
        if (country == null) {
            return "";
        }
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

    public void setEstimateTimeTillNextMark(Long estimateTimeTillNextMark) {
        timeTillNext = estimateTimeTillNextMark;
    }

    public String getEstimateTimeAtFinish() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(estimateTimeAtFinish);
    }

    public void setEstimateTimeAtFinish(Long estimateTimeAtFinish) {
        this.estimateTimeAtFinish = estimateTimeAtFinish;
    }

    public Integer getPlacing() {
        return placingProperty.get();
    }

    public void setPlacing(Integer position) {
        placingProperty.set(position);
    }

    public ReadOnlyIntegerProperty placingProperty() {
        return placingProperty.getReadOnlyProperty();
    }

    public void updateVelocityProperty(double velocity) {
        this.velocityProperty.set(velocity);
    }

    public void setMarkRoundingTime(Long markRoundingTime) {
        this.markRoundTime = markRoundingTime;
    }

    public ReadOnlyDoubleProperty getVelocityProperty() {
        return velocityProperty.getReadOnlyProperty();
    }

    public ReadOnlyLongProperty timeTillNextProperty() {
        return timeTillNextProperty.getReadOnlyProperty();
    }

    public Long getTimeTillNext() {
        return timeTillNext;
    }

    public Long getMarkRoundTime() {
        return markRoundTime;
    }

    public CompoundMark getLastMarkRounded() {
        return lastMarkRounded;
    }

    public void setLastMarkRounded(CompoundMark lastMarkRounded) {
        this.lastMarkRounded = lastMarkRounded;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void toggleSail() {
        sailIn = !sailIn;
    }
    //// TODO: 15/08/17 asd

    /**
     * Sets the current location of the boat in lat and long whilst preserving the last location
     *
     * @param lat Latitude
     * @param lng Longitude
     */
    public void setLocation(Double lat, Double lng) {
        location.setLat(lat);
        location.setLng(lng);
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    @Override
    public String toString() {
        return boatName;
    }

    public void updateTimeSinceLastMarkProperty(long timeSinceLastMark) {
        this.timeSinceLastMarkProperty.set(timeSinceLastMark);
    }

    public ReadOnlyLongProperty timeSinceLastMarkProperty() {
        return timeSinceLastMarkProperty.getReadOnlyProperty();
    }

    public void setTimeTillNext(Long timeTillNext) {
        this.timeTillNext = timeTillNext;
    }


    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }


    public void updateLocation(double lat, double lng, double heading, double velocity) {
        setLocation(lat, lng);
        this.heading = heading;
//        this.currentVelocity = velocity;
        updateVelocityProperty(velocity);
        for (YachtLocationListener yll : locationListeners) {
            yll.notifyLocation(this, lat, lng, heading, sailIn, velocity);
        }
    }

    public void addLocationListener(YachtLocationListener listener) {
        locationListeners.add(listener);
    }

    public void addMarkRoundingListener(MarkRoundingListener listener) {
        markRoundingListeners.add(listener);
    }

    public void removeMarkRoundingListener(MarkRoundingListener listener) {
        markRoundingListeners.remove(listener);
    }

    public boolean getSailIn () {
        return sailIn;
    }

    public void roundMark(CompoundMark mark, long markRoundTime, long timeSinceLastMark) {
        this.markRoundTime = markRoundTime;
        timeSinceLastMarkProperty.set(timeSinceLastMark);
        lastMarkRounded = mark;
        legNumber++;
        for (MarkRoundingListener listener : markRoundingListeners) {
            listener.notifyRounding(this, lastMarkRounded, legNumber);
        }
    }
}
