package seng302;

/*
	Represents a boat in the race.

	@param teamName The name of the team sailing the boat
*/
public class Boat
{
	// The name of the team, this is also the name of the boat
	private String teamName = null;
	private boolean finishedRace = false;

	public Boat(String teamName) {
		this.teamName = teamName;
	}

	/*
		Returns the name of the team sailing the boat
		@returns The name of the team 
	*/
	public String getTeamName(){
		return this.teamName;
	}

	/*
		Sets the name of the team sailing the boat
		@param teamName The name of the team
	*/
	public void setTeamName(String teamName){
		this.teamName = teamName;
	}
}