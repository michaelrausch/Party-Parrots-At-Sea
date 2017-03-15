package seng302.models;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


/**
 * Read team name from a given Json file. So that user can extract information
 * efficiently from external files.
 */

public class FileParser {

    private String filePath;
    private JSONObject content;

    /**
     * used to construct an instance of file parser
     *
     * @param filePath a string like path to show location of desired file to
     *                 be parsed
     */
    public FileParser(String filePath) throws Exception {
        this.filePath = filePath;
        this.readFile();
    }

    /**
     * Reads content from a given file, and return the content as JSONObject.
     * Throws FileNotFoundException, if the given file cannot be found.
     */
    private void readFile() throws FileNotFoundException {
        JSONParser parser = new JSONParser();
        try {
            this.content = (JSONObject) parser.parse(new FileReader(filePath));

        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets time scale setting parameter.
     *
     * @return long time scale. -1 if parameter is invalid (eg. scale is
     * negative number, or containing non numeric character) or cannot be found.
     */
    @SuppressWarnings("unchecked")
    public double getTimeScale() {
        try {
            double timeScale = (double) this.content.get("time-scale");
            return timeScale >= 0 ? timeScale : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Gets race name in the setting file.
     *
     * @return a string of race name. null if race name is invalid or cannot
     * be found.
     */
    @SuppressWarnings("unchecked")
    public String getRaceName() {
        try {
            return (String) this.content.get("race-name");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets an array of teams who participate the race.
     *
     * @return an ArrayList containing strings of team names. null if teams
     * setting is invalid or there is no team.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Map<String, Object>> getTeams() {
        try {
            return (ArrayList<Map<String, Object>>) this.content.get("teams");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the total number of teams.
     *
     * @return the number of teams. 0 if no teams or anything goes wrong.
     */
    @SuppressWarnings("unchecked")
    public long getTotalNumberOfTeams() {
        ArrayList<Map<String, Object>> teams = getTeams();
        try {
            return teams.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Gets the number of boats that would compete during a race. Returns the
     * total number of race size if parameter is invalid or cannot be found.
     *
     * @return an int of the race size.
     */
    @SuppressWarnings("unchecked")
    public long getRaceSize() {
        long totalTeams = this.getTotalNumberOfTeams();
        try {
            long raceSize = (long) this.content.get("race-size");
            return raceSize >= 0 && raceSize <= totalTeams ? raceSize : totalTeams;
        } catch (Exception e) {
            e.printStackTrace();
            return totalTeams;
        }
    }
}