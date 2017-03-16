package seng302.models;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.io.FileNotFoundException;

public class OldApp {

    /**
     * Builds a race object for the AC35 course
     *
     * @return a Race object for the AC35 course
     */
    public static Race createRace(String configFile) throws Exception {
        Race race = new Race();
        FileParser fp;

        // Read team names from file
        try{
            fp = new FileParser(configFile);
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

        // Add marks to race in order
        race.addMark(new Mark("Start", 32.296038,-64.854401  ));
        race.addMark(new Mark("Mid Mark", 32.292881,-64.843231  ));
        race.addMark(new Mark("Leeward Gate", 32.283808,-64.850012  ));
        race.addMark(new Mark("Windward Gate", 32.309908,-64.833665  ));
        race.addMark(new Mark("Finish", 32.318439,-64.837367  ));

        return race;
    }

    public static void main() {
        Race race = null;
        String raceConfigFile;

        raceConfigFile = "doc/examples/config.json";


        try {
            race = createRace(raceConfigFile);
        } catch (Exception e) {
            System.out.println("There was an error creating the race.");
        }

        // If race was created
        if (race != null) {
            race.displayStartingBoats();

            System.out.println("\n\n");
            System.out.println("######################");
            System.out.println("# Live Race Updates   ");
            System.out.println("######################");

            race.startRace();

            System.out.println("\n\n");
            System.out.println("######################");
            System.out.println("# Race Results   ");
            System.out.println("######################");

            //race.showRaceMarkerResults();
            race.displayFinishingOrder();

        } else {
            System.out.println("There was an error creating the race. Exiting.");
        }
    }
}