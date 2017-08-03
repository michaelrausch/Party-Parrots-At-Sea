package seng302.model.mark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundMark {

	private int compoundMarkId;
	private String name;

	private List<Mark> marks = new ArrayList<>();

	public CompoundMark(int markID, String name) {
		this.compoundMarkId = markID;
		this.name = name;
	}

	public void addSubMarks(Mark... marks) {
		this.marks.addAll(Arrays.asList(marks));
	}

	public void addSubMarks(List<Mark> marks) {
	    this.marks.addAll(marks);
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
}
