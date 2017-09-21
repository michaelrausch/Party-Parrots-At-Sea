package seng302.model.stream.xml.generator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import seng302.model.Limit;
import seng302.model.ServerYacht;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.token.Token;

/**
 * A Race object that can be parsed into XML
 */
public class RaceXMLTemplate {

    private List<ServerYacht> yachts;
    private LocalDateTime startTime;
    private List<Token> tokens;
    private List<Corner> roundings;
    private List<Limit> courseLimit;
    private List<CompoundMark> course;

    public RaceXMLTemplate(List<ServerYacht> yachts, List<Token> tokens, List<Corner> roundings,
        List<Limit> limit, List<CompoundMark> course) {
        this.yachts = yachts;
        this.tokens = tokens;
        this.roundings = roundings;
        this.courseLimit = limit;
        this.course = course;
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

    public List<CompoundMark> getCompoundMarks() {
        return Collections.unmodifiableList(course);
    }

    public List<Limit> getCourseLimit() {
        return Collections.unmodifiableList(courseLimit);
    }

    public List<Corner> getRoundings() {
        return Collections.unmodifiableList(roundings);
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
