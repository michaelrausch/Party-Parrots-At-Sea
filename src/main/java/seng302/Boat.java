package seng302;

/*
	Represents a boat in the race.

	@param teamName The name of the team sailing the boat
*/
public class Boat
{
	// The name of the team, this is also the name of the boat
	private String teamName = null;
	private float velocity = 70; // please set this one to a reasonable num!!!!!, i set it just for testing ;)

	public Boat(String teamName) {
		this.teamName = teamName;
	}
	public Boat(String teamName, float boatVelocity) {
		this.teamName = teamName;
		this.velocity = boatVelocity;
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

	/**
	 * Sets velocity of the boat
	 * @param velocity The velocity of boat
	 */
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	/**
	 * Gets velocity of the boat
	 * @return a float number of the boat velocity
	 */
	public float getVelocity() {
		return this.velocity;
	}
}