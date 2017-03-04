package seng302;

import java.util.ArrayList;

public class App 
{
    public static void main( String[] args )
    {
    	Race race = new Race();
    	race.addBoat(new Boat("Team 1"));
    	race.addBoat(new Boat("Team 2"));
 
 		race.displayStartingBoats();

 		System.out.println("");

    	race.displayFinishingOrder();
    }
}