package seng302;

import java.util.ArrayList;
import java.lang.reflect.Array;
import java.util.Random;
import java.util.Collections;
import java.util.List;

public class App 
{
	public static Race createRace(int numberOfBoats) throws Exception{
		Race race = new Race();

		// Read team names from file
		FileParser fp = new FileParser("src/test/java/seng302/valid.json");
		ArrayList<String> boatNames = fp.getTeams();

		// Shuffle team names
		long seed = System.nanoTime();
		Collections.shuffle(boatNames, new Random(seed));

		if (numberOfBoats > Array.getLength(boatNames.toArray())){
			return null;
		}

		for (int i = 0; i < numberOfBoats; i++) {
			race.addBoat(new Boat(boatNames.get(i)));
		}

		race.addLeg(new Leg(035, 100, "Start"));
		race.addLeg(new Leg(010, 300, "Marker 1"));
		race.addLeg(new Leg(350, 400, "Leeward Gate"));
		race.addLeg(new Leg(010, 400, "Windward Gate"));
		race.addLeg(new Leg(010, 400, "Leeward Gate"));

		return race;
	}

    public static void main( String[] args )
    {
    	Race race = null;

    	try{
    		 race = createRace(2);
    		
    	}
    	catch (Exception e){
    		System.out.println(e);
    	}

    	if (race != null){
    		race.startRace();

	    	race.displayFinishingOrder();	

	    	
    	}
    	else{
    		System.out.println("e");
    	}
    }
}