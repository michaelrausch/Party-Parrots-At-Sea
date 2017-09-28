package seng302.visualiser.fxObjects;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import seng302.visualiser.fxObjects.MarkArrowFactory.RoundingSide;

/**
 * Created by cir27 on 28/09/17.
 */
public abstract class Marker extends Group{

    protected List<Group> enterArrows = new ArrayList<>();
    protected List<Group> exitArrows = new ArrayList<>();
    protected int enterArrowIndex = 0;
    protected int exitArrowIndex = 0;

    public abstract void addArrows(RoundingSide roundingSide, double entryAngle, double exitAngle);
    /**
     * Shows the next EnterArrow. Does nothing if there are no more enter arrows. Other arrows become hidden.
     */
    public void showNextEnterArrow() {
        showArrow(enterArrows, enterArrowIndex);
        enterArrowIndex++;
    }

    /**
     * Shows the next ExitArrow. Does nothing if there are no more enter arrows. Other arrows become hidden.
     */
    public void showNextExitArrow() {
        showArrow(exitArrows, exitArrowIndex);
        exitArrowIndex++;
    }

    protected abstract void showArrow(List<Group> arrowList, int arrowListIndex);

    public abstract void hideAllArrows();
}
