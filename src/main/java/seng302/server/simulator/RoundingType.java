package seng302.server.simulator;

public enum RoundingType {

	// the mark should be rounded to port (boat's left)
	PORT("Port"),

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

	public static RoundingType typeOf(String type) {
		switch (type) {
			case "Port":
				return PORT;
			case "Stbd":
				return STARBOARD;
			case "SP":
				return SP;
			case "PS":
				return PS;
			default:
				return null;
		}
	}
}
