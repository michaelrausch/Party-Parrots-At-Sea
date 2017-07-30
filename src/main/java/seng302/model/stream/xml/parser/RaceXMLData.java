package seng302.model.stream.xml.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;

/**
 * Process a Document object containing race data in XML format and stores the data.
 */
public class RaceXMLData {

    private List<Integer> participants;
    private Map<Integer, CompoundMark> compoundMarks;
    private List<Corner> markSequence;
    private List<Limit> courseLimit;

    public RaceXMLData(List<Integer> participants, List<CompoundMark> compoundMarks,
        List<Corner> markSequence, List<Limit> courseLimit) {
        this.participants = participants;
        this.markSequence = markSequence;
        this.courseLimit = courseLimit;
        this.compoundMarks = new HashMap<>();
        for (CompoundMark cMark : compoundMarks) {
            this.compoundMarks.put(cMark.getId(), cMark);
        }
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public Map<Integer, CompoundMark> getCompoundMarks() {
        return compoundMarks;
    }

    public List<Corner> getMarkSequence() {
        return markSequence;
    }

    public List<Limit> getCourseLimit() {
        return courseLimit;
    }

}
