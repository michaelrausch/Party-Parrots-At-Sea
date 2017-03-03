package seng302;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.List;


public class Race {
	private ArrayList<Boat> boats;

	public Race(){
		boats = new ArrayList<Boat>();
	}

	/*
		Add a boat to the race
		@param boat the boat to add
	*/
	public void addBoat(Boat boat){
		boats.add(boat);
	}

	/*
		Returns a list of boats in the order that they
		finished the race (0 is first)

		@returns a list of boats
	*/
	public Boat[] getFinishedBoats(){
		// Shuffle the list of boats
		long seed = System.nanoTime();
		Collections.shuffle(this.boats, new Random(seed));

		return boats.toArray(new Boat[boats.size()]);
	}
}