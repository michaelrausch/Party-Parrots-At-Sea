package seng302;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.List;


public class Race {
	private ArrayList<Boat> boats;
	private int numberOfBoats = 0;

	public Race(){
		boats = new ArrayList<Boat>();
	}

	/*
		Add a boat to the race
		@param boat, the boat to add
	*/
	public void addBoat(Boat boat){
		boats.add(boat);
		numberOfBoats += 1;
	}

	/*
		Returns a list of boats in the order that they
		finished the race (position 0 is first place)

		@returns a list of boats
	*/
	public Boat[] getFinishedBoats(){
		// Shuffle the list of boats
		long seed = System.nanoTime();
		Collections.shuffle(this.boats, new Random(seed));

		return boats.toArray(new Boat[boats.size()]);
	}

	/*
		Returns the number of boats in the race

		@returns the number of boats in the race
	*/
	public int getNumberOfBoats(){
		return numberOfBoats;
	}

	/*
		Returns a list of boats in the race

		@returns a list of the boats competing in the race
	*/
	public Boat[] getBoats(){
		return boats.toArray(new Boat[boats.size()]);
	}

	/*
		Prints the order in which the boats finished
	*/ 
	public void displayFinishingOrder(){
		int numberOfBoats = this.getNumberOfBoats();
		Boat[] boats = this.getFinishedBoats();

		System.out.println("--- Finishing Order ---");

		for (int i = 0; i < numberOfBoats; i++) {
			System.out.println("#" + Integer.toString(i+1) + " - " + boats[i].getTeamName());
		}
	}

	/*
		Prints the list of boats competing in the race

	*/ 
	public void displayStartingBoats(){
		int numberOfBoats = this.getNumberOfBoats();
		Boat[] boats = this.getBoats();

		System.out.println("--- Competing Boats ---");

		for (int i = 0; i < numberOfBoats; i++) {
			System.out.println(boats[i].getTeamName());
		}
	}
}