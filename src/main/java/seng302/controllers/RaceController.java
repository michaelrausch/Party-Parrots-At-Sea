package seng302.controllers;

import seng302.models.Boat;
import seng302.models.Race;
import seng302.models.parsers.ConfigParser;
import seng302.models.parsers.CourseParser;
import seng302.models.parsers.TeamsParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by zyt10 on 17/03/17.
 * run before CanvasController to initialize race events
 * the CanvasController then uses the event data to make the animations
 */
public class RaceController {
    Race race = null;

    public void initializeRace() {
        String raceConfigFile = "/config/config.xml";
        String teamsConfigFile = "/config/teams.xml";

        try {
            race = createRace(raceConfigFile, teamsConfigFile);
        } catch (Exception e) {
            System.out.println("There was an error creating the race.");
        }

        if (race != null) {
            race.startRace();
        } else {
            System.out.println("There was an error creating the race. Exiting.");
        }
    }

    public Race createRace(String configFile, String teamsConfigFile) throws Exception {
        Race race = new Race();

        // Read team names from file
        TeamsParser tp = new TeamsParser(teamsConfigFile);

        // Read course from file
        ConfigParser config = new ConfigParser(configFile);

        ArrayList<String> boatNames = new ArrayList<>();
        ArrayList<Boat> teams = tp.getBoats();

        //get race size
        int numberOfBoats = teams.size();

        //get time scale
        double timeScale = config.getTimeScale();
        race.setTimeScale(timeScale);

        for (Boat boat : teams) {
            boatNames.add(boat.getTeamName());
            race.addBoat(boat);
        }

        // Shuffle team names
        long seed = System.nanoTime();
        Collections.shuffle(boatNames, new Random(seed));

        if (numberOfBoats > Array.getLength(boatNames.toArray())) {
            return null;
        }

        CourseParser course = new CourseParser("/config/course.xml");
        race.addCourse(course.getCourse());

        return race;
    }

    public Race getRace() {
        return race;
    }
}
