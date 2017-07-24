package seng302.model.stream.parsers.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import seng302.model.Corner;
import seng302.model.Limit;
import seng302.model.mark.Mark;

/**
 * Process a Document object containing race data in XML format and stores the data.
 */
public class RaceXMLData {

    private List<Integer> participants;
    private Map<Integer, Mark> compoundMarks;
    private List<Corner> markSequence;
    private List<Limit> courseLimit;
    private Map<Integer, Mark> individualMarks;

    RaceXMLData(List<Integer> participants, List<Mark> compoundMarks, List<Corner> markSequence,
        List<Limit> courseLimit) {
        this.participants = participants;
        this.markSequence = markSequence;
        this.courseLimit = courseLimit;
        this.compoundMarks = new HashMap<>();
        for (Mark mark : compoundMarks)
            this.compoundMarks.put(mark.getId(), mark);
        for (Mark mark : compoundMarks) {
        }
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public Map<Integer, Mark> getCompoundMarks() {
        return compoundMarks;
    }

    public List<Corner> getMarkSequence() {
        return markSequence;
    }

    public List<Limit> getCourseLimit() {
        return courseLimit;
    }
}
