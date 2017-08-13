package seng302.model.mark;

import java.util.ArrayList;
import java.util.List;
import seng302.model.GeoPoint;
import seng302.utilities.GeoUtility;

public class CompoundMark {

	private int compoundMarkId;
	private String name;

	private List<Mark> marks = new ArrayList<>();
    private GeoPoint midPoint;

    public CompoundMark(int markID, String name, List<Mark> marks) {
        this.compoundMarkId = markID;
        this.name = name;
        this.marks.addAll(marks);
        if (marks.size() > 1) {
            this.midPoint = GeoUtility.getDirtyMidPoint(marks.get(0), marks.get(1));
        } else {
            this.midPoint = marks.get(0);
        }
    }

	/**
	 * Prints out compoundMark's info and its marks, good for testing
	 * @return a string showing its details
	 */
	@Override
	public String toString(){
		String info = String.format(
			"CompoundMark: %d (%s), [%s", compoundMarkId, name, marks.get(0).toString()
		);
		if (marks.size() > 1) {
			info += String.format(", %s", marks.get(1).toString());
		}
		return info + "]";
	}

	public int getId() {
		return compoundMarkId;
	}

	public void setId (int markID) {
		this.compoundMarkId = markID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    /**
     * Returns the mark contained in the compound mark. Marks are numbered 1 to n;
     * @param singleMarkId the id of the desired mark contained in this compound mark.
     * @return the desired mark. Returns null if the ID is not in range (1, NUM_MARKS)
     */
	public Mark getSubMark(int singleMarkId) {
	    try {
            return marks.get(singleMarkId - 1);
        } catch (IndexOutOfBoundsException e) {
	        return null;
        }
    }

    /**
     * NOTE: This is a 'dirty' mid point as it is simply calculated as an xy point would be.
     * NO CHECKING FOR LAT / LNG WRAPPING IS DONE IN CREATION OF THIS MIDPOINT
     *
     * @return GeoPoint of the midpoint of the two marks, or the one mark if there is only one
     */
    public GeoPoint getMidPoint() {
        return midPoint;
    }

    /**
     * Returns whether or not this CompoundMark is a Gate. It is generally cleaner to program to a
     * specific singleMark or the list of marks.
     *
     * @return True if the compound mark is a gate, false otherwise.
     */
    public boolean isGate () {
	    return marks.size() > 1;
    }

    /**
     * Returns the list of marks in the compoundMark
     *
     * @return All marks contained in this mark.
     */
    public List<Mark> getMarks () {
        return marks;
    }

	@Override
	public int hashCode() {
    	int hash = 0;
    	for (Mark mark : marks) {
    		hash += Double.hashCode(mark.getSourceID()) + Double.hashCode(mark.getLat())
				+ Double.hashCode(mark.getLng()) + mark.getName().hashCode();
		}
		return hash + getName().hashCode() + Integer.hashCode(getId());
	}
}
