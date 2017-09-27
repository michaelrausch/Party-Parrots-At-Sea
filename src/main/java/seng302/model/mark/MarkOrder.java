package seng302.model.mark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.stream.xml.parser.RaceXMLData;

/**
 * Class to hold the order of the marks in the race.
 */
public class MarkOrder {
    private List<CompoundMark> raceMarkOrder;
    private List<CompoundMark> orderedUniqueCompoundMarks;
    private Logger logger = LoggerFactory.getLogger(MarkOrder.class);
    private List<Mark> allMarks;


    public MarkOrder(RaceXMLData raceXMLData){
        raceMarkOrder = new ArrayList<>();
        for (Corner corner : raceXMLData.getMarkSequence()){
            CompoundMark compoundMark = raceXMLData.getCompoundMarks().get(corner.getCompoundMarkID());
            compoundMark.setRoundingSide(
                RoundingSide.getRoundingSide(corner.getRounding())
            );
            raceMarkOrder.add(compoundMark);
        }
        orderedUniqueCompoundMarks = new ArrayList<>(raceXMLData.getCompoundMarks().values());
    }

    /**
     * @return An ordered list of marks in the race
     *         OR null if the mark order could not be loaded
     */
    public List<CompoundMark> getMarkOrder() {
        if (raceMarkOrder == null){
            logger.warn("Race order accessed but not instantiated");
            return null;
        }
        return Collections.unmodifiableList(raceMarkOrder);
    }

    public List<CompoundMark> getOrderedUniqueCompoundMarks() {
        return orderedUniqueCompoundMarks;
    }

    /**
     * @param seqID The seqID of the current mark the boat is heading to
     * @return A Boolean indicating if this coming mark is the last one (finish line)
     */
    public Boolean isLastMark(Integer seqID) {
        return seqID == raceMarkOrder.size() - 1;
    }

    /**
     * @param currentSeqID The seqID of the current mark the boat is heading to
     * @return The mark last passed
     * @throws IndexOutOfBoundsException if there is no next mark. Check seqID != 0 first
     */
    public CompoundMark getPreviousMark(Integer currentSeqID) throws IndexOutOfBoundsException {
        return raceMarkOrder.get(currentSeqID - 1);
    }

    public CompoundMark getCurrentMark(Integer currentSeqID) {
        return raceMarkOrder.get(currentSeqID);
    }

    /**
     * @param currentSeqID The seqID of the current mark the boat is heading to
     * @return The mark following the mark that the boat is heading to
     * @throws IndexOutOfBoundsException if there is no next mark. Check using {@link
     * #isLastMark(Integer)}
     */
    public CompoundMark getNextMark(Integer currentSeqID) throws IndexOutOfBoundsException {
        return raceMarkOrder.get(currentSeqID + 1);
    }
}