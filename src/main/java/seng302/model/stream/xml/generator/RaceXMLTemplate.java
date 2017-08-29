package seng302.model.stream.xml.generator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import seng302.model.ServerYacht;
import seng302.model.token.Token;

/**
 * A Race object that can be parsed into XML
 */
public class RaceXMLTemplate {

    private List<ServerYacht> yachts;
    private LocalDateTime startTime;
    private List<Token> tokens;

    public RaceXMLTemplate(List<ServerYacht> yachts, List<Token> tokens) {
        this.yachts = yachts;
        this.tokens = tokens;
        startTime = LocalDateTime.now();
    }

    /**
     * Get a list of boats in the race
     * @return A List of boats
     */
    public List<ServerYacht> getBoats() {
        return Collections.unmodifiableList(yachts);
    }

    /**
     * Get a list of tokens in the race
     *
     * @return A list of tokens
     */
    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
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
