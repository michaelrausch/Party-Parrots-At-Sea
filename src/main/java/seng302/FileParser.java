package seng302;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;


/**
 * Read team name from a given Json file. So that user can extract information
 * efficiently from external files.
 */

public class FileParser {

	private String filePath;
	private JSONObject content;
	/** used to construct an instance of file parser
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
	private void readFile() throws FileNotFoundException{
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(filePath));

			JSONObject jsonObject = (JSONObject) obj;
			this.content = jsonObject;
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
	 * @return long time scale. -1 if parameter is invalid (eg. scale is
	 * negative number, or containing non numeric character) or cannot be found.
	 */
	public long getTimeScale() {
		try {
			long timeScale = (long) this.content.get("time-scale");
			return timeScale >= 0 ? timeScale : -1;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Gets race name in the setting file.
	 * @return a string of race name. null if race name is invalid or cannot
	 * be found.
	 */
	public String getRaceName() {
		try {
			return (String) this.content.get("race-name");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets an array of teams who participate the race.
	 * @return an ArrayList containing strings of team names. null if teams
	 * setting is invalid or there is no team.
	 */
	public ArrayList<String> getTeams() {
		try {
			return (ArrayList<String>) this.content.get("teams");
		} catch (Exception e) {
			return null;
		}
	}

}