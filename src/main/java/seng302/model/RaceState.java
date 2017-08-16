package seng302.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.TimeZone;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.model.stream.parser.RaceStartData;
import seng302.model.stream.parser.RaceStatusData;

/**
 * Class for storing race data that does not relate to specific vessels or marks such as time or wind.
 * Calculates the state of critical race attributes when relevant data is added.
 */
public class RaceState {

    @FunctionalInterface
    public interface CollisionListener {
        void notifyCollision(GeoPoint location);
    }

//    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private ReadOnlyDoubleWrapper windSpeed = new ReadOnlyDoubleWrapper();
    private ReadOnlyDoubleWrapper windDirection = new ReadOnlyDoubleWrapper();
    private long serverSystemTime;
    private long expectedStartTime;
    private boolean isRaceStarted = false;
    long timeTillStart;
    private ObservableList<ClientYacht> playerPositions;
    private List<ClientYacht> collisions = new ArrayList<>();
    private List<CollisionListener> collisionListeners = new ArrayList<>();

    public RaceState() {
        playerPositions = FXCollections.observableArrayList();
    }

    public void updateState (RaceStatusData data) {
        this.windSpeed.set(data.getWindSpeed());
        this.windDirection.set(data.getWindDirection());
        this.serverSystemTime = data.getCurrentTime();
        this.expectedStartTime = data.getExpectedStartTime();
        this.isRaceStarted = data.isRaceStarted();
    }

    public void setTimeZone (TimeZone timeZone) {
        DATE_TIME_FORMAT.setTimeZone(timeZone);
    }

    public void updateState (RaceStartData data) {
        this.timeTillStart = data.getRaceStartTime();
    }

    public String getRaceTimeStr () {
        long raceTime = serverSystemTime - expectedStartTime;
        if (raceTime < 0) {
            return "-" + DATE_TIME_FORMAT.format(-1 * (raceTime - 1000));
        } else {
            return DATE_TIME_FORMAT.format(serverSystemTime - expectedStartTime);
        }
    }

    public long getTimeTillStart () {
        return (expectedStartTime - serverSystemTime);
    }

    public double getWindSpeed() {
        return windSpeed.doubleValue();
    }

    public ReadOnlyDoubleProperty windSpeedProperty() {
        return windSpeed.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty windDirectionProperty() {
        return windDirection.getReadOnlyProperty();
    }

    public long getRaceTime() {
        return serverSystemTime;
    }

    public boolean isRaceStarted () {
        return isRaceStarted;
    }

    public void setBoats(Collection<ClientYacht> clientYachts) {
        playerPositions.setAll(clientYachts);
    }

    public void sortPlayers() {
        playerPositions.sort((yacht1, yacht2) -> Integer.compare(yacht2.getLegNumber(),
            yacht1.getLegNumber()));
    }

    public ObservableList<ClientYacht> getPlayerPositions() {
        return playerPositions;
    }

    public void storeCollision(ClientYacht yacht) {
        collisions.add(yacht);
        for (CollisionListener collisionListener : collisionListeners) {
            collisionListener.notifyCollision(yacht.getLocation());
        }
    }

    public void addCollisionListener(CollisionListener collisionListener) {
        collisionListeners.add(collisionListener);
    }

    public void removeCollisionListener(CollisionListener collisionListener) {
        collisionListeners.remove(collisionListener);
    }
}
