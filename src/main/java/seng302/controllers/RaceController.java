package seng302.controllers;

import seng302.models.Boat;
import seng302.models.OldFileParser;
import seng302.models.Race;
import seng302.models.parsers.CourseParser;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 * Created by zyt10 on 17/03/17.
 * run before CanvasController to initialize race events
 * the CanvasController then uses the event data to make the animations
 */
public class RaceController {
    Race race = null;

    public void initializeRace() {
        String raceConfigFile;
        raceConfigFile = "doc/examples/config.json";

        try {
            race = createRace(raceConfigFile);
        } catch (Exception e) {
            System.out.println("There was an error creating the race.");
        }

        if (race != null) {
            race.startRace();
        } else {
            System.out.println("There was an error creating the race. Exiting.");
        }
    }

    public Race createRace(String configFile) throws Exception {
        Race race = new Race();
        OldFileParser fp;

        // Read team names from file
        try{
            fp = new OldFileParser(configFile);
        }
        catch (FileNotFoundException e){
            System.out.println("Config file does not exist");
            return null;
        }

        ArrayList<String> boatNames = new ArrayList<>();
        ArrayList<Map<String, Object>> teams = fp.getTeams();

        //get race size
        int numberOfBoats = (int) fp.getRaceSize();

        //get time scale
        double timeScale = fp.getTimeScale();
        race.setTimeScale(timeScale);

        for (Map<String, Object> team : teams) {
            boatNames.add((String) team.get("team-name"));
        }

        // Shuffle team names
        long seed = System.nanoTime();
        Collections.shuffle(boatNames, new Random(seed));

        if (numberOfBoats > Array.getLength(boatNames.toArray())) {
            return null;
        }

        // Add boats to the race
        for (int i = 0; i < numberOfBoats; i++) {
            race.addBoat(new Boat(boatNames.get(i), (Double) (teams.get(i).get("velocity"))));
        }

        CourseParser cp = new CourseParser("doc/examples/course.xml");
        race.addCourse(cp.getCourse());

        return race;
    }

    public Race getRace() {
        return race;
    }
}
