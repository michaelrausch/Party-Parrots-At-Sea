package seng302;

public class Leg {
	private int heading;
	private int distance;
	private String startLabel;
	private boolean isFinishingLeg;

	public Leg(int heading, int distance, String label){
		this.heading = heading;
		this.distance = distance;
		this.startLabel = label;
		this.isFinishingLeg = false;
	}

	public void setHeading(int heading){
		this.heading = heading;
	}

	public int getHeading(){
		return this.heading;
	}

	public void setDistance(int distance){
		this.distance = distance;
	}

	public int getDistance(){
		return this.distance;
	}

	public void setLabel(String label){
		this.startLabel = label;
	}

	public String getLabel(){
		return this.startLabel;
	}

	public void setFinishingLeg(boolean isFinishingLeg){
		this.isFinishingLeg = isFinishingLeg;
	}

	public boolean getIsFinishingLeg(){
		return this.isFinishingLeg;
	}
}