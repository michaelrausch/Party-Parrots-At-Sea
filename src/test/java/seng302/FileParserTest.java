package seng302;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/** Unit test for FileParser class
 * Created by Haoming on 5/03/17.
 */
public class FileParserTest {

	/*
		test if it fails from reading non existed file
	 */
	@Test (expected = FileNotFoundException.class)
	public void readNonExistedFile() throws Exception {
		FileParser fileParser = new FileParser("test/java/seng302/non-existed.json");
	}

	/*
		test a valid json file with valid content.
	 */
	@Test
	public void readValidFile() throws Exception{
		FileParser fileParser = new FileParser("src/test/java/seng302/valid.json");

		assertEquals(fileParser.getRaceName(), "IDK");

		ArrayList<String> teams = new ArrayList<>();
		teams.add("team1");
		teams.add("team2");
		teams.add("team3");
		assertTrue(teams.equals(fileParser.getTeams()));
	}

	/*
		test an invalid json file within wrong type value and misnamed
		variable name.
	 */
	@Test
	public void readInvaldFile() throws Exception {
		FileParser fileParser = new FileParser("src/test/java/seng302/invalid.json");

		assertEquals(fileParser.getRaceName(), null);
		assertEquals(fileParser.getTeams(), null);
		assertEquals(fileParser.getTimeScale(), -1);
		assertEquals(fileParser.getTeams(), null);
	}

}