package seng302.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.model.stream.parser.RaceStartData;
import seng302.model.stream.parser.RaceStatusData;

/**
 * Class for storing race data that does not relate to specific vessels or marks such as time or wind.
 * Calculates the state of critical race attributes when relevant data is added.
 */
public class RaceState {

//    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private double windSpeed;
    private double windDirection;
    private long serverSystemTime;
    private long expectedStartTime;
    private boolean isRaceStarted = false;
    long timeTillStart;
    private ObservableList<ClientYacht> playerPositions;

    public RaceState() {
        playerPositions = FXCollections.observableArrayList();
    }

    public void updateState (RaceStatusData data) {
        this.windSpeed = data.getWindSpeed();
        this.windDirection = data.getWindDirection();
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
        return windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
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
        playerPositions.sort(Comparator.comparingInt(ClientYacht::getLegNumber));
    }

    public ObservableList<ClientYacht> getPlayerPositions() {
        return FXCollections.unmodifiableObservableList(playerPositions);
    }
}
