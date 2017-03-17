package seng302;

import org.junit.Test;
import seng302.models.OldFileParser;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for FileParser class
 * Created by Haoming on 5/03/17.
 */
public class OldFileParserTest {

    /**
     * test if it fails from reading non existed file
     */
    @Test(expected = FileNotFoundException.class)
    public void readNonExistedFile() throws Exception {
        OldFileParser fileParser = new OldFileParser("test/java/seng302/non-existed.json");
    }

    /**
     * test a valid json file with valid content.
     */
    @Test
    public void readValidFile() throws Exception {
        OldFileParser fileParser = new OldFileParser("src/test/java/seng302/valid.json");

        assertEquals("AC35", fileParser.getRaceName());

        assertEquals("Oracle Team USA", fileParser.getTeams().get(0).get("team-name"));
        assertEquals(20.9, fileParser.getTeams().get(0).get("velocity"));
        assertEquals(2, fileParser.getRaceSize());
        assertEquals(6, fileParser.getTotalNumberOfTeams());
    }

    /**
     * test an invalid json file within wrong type value and misnamed
     * variable name.
     */
    @Test
    public void readInvalidFile() throws Exception {
        OldFileParser fileParser = new OldFileParser("src/test/java/seng302/invalid.json");

        assertEquals(null, fileParser.getRaceName());
        assertEquals(null, fileParser.getTeams());
        //assertEquals(-1, fileParser.getTimeScale());
        assertEquals(null, fileParser.getTeams());
    }

}