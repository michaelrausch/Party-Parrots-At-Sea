package seng302;

import java.util.*;
import java.lang.reflect.Array;

public class App 
{
	/**
	* Builds a race object for the AC35 course
	*/
	public static Race createRace() throws Exception{
		Race race = new Race();

		// Read team names from file
		FileParser fp = new FileParser("doc/examples/config.json");
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

		if (numberOfBoats > Array.getLength(boatNames.toArray())){
			return null;
		}

		for (int i = 0; i < numberOfBoats; i++) {
			race.addBoat(new Boat(boatNames.get(i), (Double)(teams.get(i).get("velocity"))));
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

    public static void main( String[] args )
    {
    	Race race = null;

    	try{
    		 race = createRace();
    	}
    	catch (Exception e){
    		System.out.println(e);
    	}

    	// If race was created
    	if (race != null){
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
    	}
    	else{
    		System.out.println("There was an error creating the race.");
    	}
    }
}