package seng302.models.xml;

import seng302.models.Yacht;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Race object that can be parsed into XML
 */
public class Race {
    private List<Yacht> yachts;
    private LocalDateTime startTime;

    public Race(){
        yachts = new ArrayList<>();
        startTime = LocalDateTime.now();
    }

    /**
     * Add a boat to the race
     * @param yacht The boat to add
     */
    public void addBoat(Yacht yacht){
        yachts.add(yacht);
    }

    /**
     * Get a list of boats in the race
     * @return A List of boats
     */
    public List<Yacht> getBoats(){
        return Collections.unmodifiableList(yachts);
    }

    /**
     * Set the time until the race starts
     * @param seconds The time in seconds until the race starts
     */
    public void setRaceStartDelay(Integer seconds){
        startTime = startTime.plusMinutes(seconds);
    }

    /**
     * Get the time the race starts
     * @return The time the race starts
     */
    public String getRaceStartTime(){
        return startTime.toString();
    }
}
