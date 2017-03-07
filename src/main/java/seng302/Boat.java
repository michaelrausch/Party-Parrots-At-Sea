package seng302;

/**
*	Represents a boat in the race.
*
*	@param teamName The name of the team sailing the boat
*	@param boatVelocity The speed of the boat in meters/second
*/
public class Boat
{
	
	private String teamName; // The name of the team, this is also the name of the boat
	private double velocity; // In meters/second

	public Boat(String teamName) {
		this.teamName = teamName;
		this.velocity = 10; // Default velocity
	}

	public Boat(String teamName, double boatVelocity) {
		this.teamName = teamName;
		this.velocity = boatVelocity;
	}

	/**
	*	Returns the name of the team sailing the boat
	*	@return The name of the team 
	*/
	public String getTeamName(){
		return this.teamName;
	}

	/**
	*	Sets the name of the team sailing the boat
	*	@param teamName The name of the team
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
	public double getVelocity() {
		return this.velocity;
	}
}