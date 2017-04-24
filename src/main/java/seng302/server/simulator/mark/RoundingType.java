package seng302.server.simulator.mark;

public enum RoundingType{

	// the mark should be rounded to port (boat's left)
	PORT("PS"),

	// the mark should be rounded to starboard (boat's right)
	STARBOARD("Stbd"),

	// the boat within the compound mark with the SeqID of 1 should be rounded
	// to starboard and the boat within the compound mark with the SeqID of 2
	// should be rounded to port.
	SP("SP"),

	// the opposite of SP
	PS("PS");

	private String type;

	RoundingType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}
