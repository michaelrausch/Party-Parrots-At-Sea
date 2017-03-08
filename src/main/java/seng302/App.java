package seng302;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.io.FileNotFoundException;

public class App {

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

        race.addLeg(new Leg(35, 100, "Start"));
        race.addLeg(new Leg(10, 300, "Marker 1"));
        race.addLeg(new Leg(350, 400, "Leeward Gate"));
        race.addLeg(new Leg(10, 400, "Windward Gate"));

        Leg finishingLeg = new Leg(10, 400, "Leeward Gate");
        finishingLeg.setFinishingLeg(true);

        race.addLeg(finishingLeg);

        return race;
    }

    public static void main(String[] args) {
        Race race = null;
        String raceConfigFile;

        if (args.length == 2 && args[0].equals("-f")){
            raceConfigFile = args[1];
        }
        else{
            // Use default config
            raceConfigFile = "doc/examples/config.json";
        }

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

            race.showRaceMarkerResults();
            race.displayFinishingOrder();

        } else {
            System.out.println("There was an error creating the race. Exiting.");
        }
    }
}