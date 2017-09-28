package seng302.visualiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import seng302.model.ClientYacht;
import seng302.model.Limit;
import seng302.model.ScaledPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.utilities.Sounds;
import seng302.visualiser.fxObjects.Marker;

/**
 * Abstract class for keeping functionality common between race visualisation.
 */
public abstract class GameView {

    double canvasWidth, canvasHeight;
    ScaledPoint scaledPoint;

    List<Limit> borderPoints;
    Group gameObjects = new Group();
    Group markers = new Group();
    Group tokens = new Group();
    List<CompoundMark> course = new ArrayList<>();
    List<CompoundMark> compoundMarks = new ArrayList<>();
    List<Corner> courseOrder = new ArrayList<>();
    HashMap<Mark, Marker> markerObjects = new HashMap<>();

    public abstract Node getAssets();
    public abstract void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence);
    public abstract void updateBorder(List<Limit> border);

    void updateMarkArrows (ClientYacht yacht, int legNumber) {
        CompoundMark compoundMark;
        if (legNumber - 1 >= 0 && legNumber-1 < course.size()) {
            Sounds.playMarkRoundingSound();
            compoundMark = course.get(legNumber-1);
            for (Mark mark : compoundMark.getMarks()) {
                markerObjects.get(mark).showNextExitArrow();
            }
        }
        CompoundMark nextMark = null;
        if (legNumber < course.size()) {
            Sounds.playMarkRoundingSound();
            nextMark = course.get(legNumber);
            for (Mark mark : nextMark.getMarks()) {
                markerObjects.get(mark).showNextEnterArrow();
            }
        }
        if (legNumber - 2 >= 0) {
            CompoundMark lastMark = course.get(Math.max(0, legNumber - 2));
            if (lastMark != nextMark) {
                for (Mark mark : lastMark.getMarks()) {
                    markerObjects.get(mark).hideAllArrows();
                }
            }
        }
    }
}
