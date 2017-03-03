package seng302;

import java.util.ArrayList;

public class App 
{
	/*
		Displays the order in which the boats finished

		@param race The current race
	*/ 
	public static void displayFinishingOrder(Race race){
		int numberOfBoats = race.getNumberOfBoats();
		Boat[] boats = race.getFinishedBoats();

		System.out.println("--- Finishing Order ---");

		for (int i = 0; i < numberOfBoats; i++) {
			System.out.println("#" + Integer.toString(i+1) + " - " + boats[i].getTeamName());
		}
	}




    public static void main( String[] args )
    {
    	Race race = new Race();
 
    	displayFinishingOrder(race);
    }
}