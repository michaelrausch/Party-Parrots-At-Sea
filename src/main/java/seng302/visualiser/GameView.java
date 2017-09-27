package seng302.visualiser;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import seng302.model.Limit;
import seng302.model.ScaledPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;

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

    public abstract Node getAssets();
    public abstract void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence);
    public abstract void updateBorder(List<Limit> border);
}
