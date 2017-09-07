package seng302.visualiser.fxObjects.assets_2D;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Visual object for a mark. Contains a coloured circle and any specified arrows.
 */
public class Marker extends Group {

    private Circle mark = new Circle();
    private Paint colour = Color.BLACK;
    private List<Group> enterArrows = new ArrayList<>();
    private List<Group> exitArrows = new ArrayList<>();
    private int enterArrowIndex = 0;
    private int exitArrowIndex = 0;

    /**
     * Creates a new Marker containing only a circle. The default colour is black.
     */
    public Marker() {
        mark.setRadius(5);
        mark.setCenterX(0);
        mark.setCenterY(0);
        Platform.runLater(() -> this.getChildren().addAll(mark, new Group())); //Empty group placeholder or arrows.
    }

    /**
     * Creates a new Marker containing only a circle of the given colour.
     * @param colour the desired colour for the marker.
     */
    public Marker(Paint colour) {
        this();
        this.colour = colour;
        mark.setFill(colour);
    }

    /**
     * Adds an exit and entry arrow pair to the mark. Arrows are hidden and shown in the order they
     * are created by calling showNextEnterArrow() or showNextExitArrow()
     * @param roundingSide the side the marker will be from the perspective of the arrow.
     * @param entryAngle The angle the arrow will point towards a marker
     * @param exitAngle The angle the arrow wil point from the marker.
     */
    public void addArrows(MarkArrowFactory.RoundingSide roundingSide, double entryAngle,
                          double exitAngle) {
        //Change Color.GRAY to this.colour to revert all gray arrows.
        enterArrows.add(
            MarkArrowFactory.constructEntryArrow(roundingSide, entryAngle, exitAngle, Color.GRAY)
        );
        exitArrows.add(
            MarkArrowFactory.constructExitArrow(roundingSide, exitAngle, Color.GRAY)
        );
    }

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

    private void showArrow(List<Group> arrowList, int arrowListIndex) {
        if (arrowListIndex < arrowList.size()) {
            if (arrowListIndex == 1) {;
            }
            Platform.runLater(() -> {
                this.getChildren().remove(1);
                this.getChildren().add(arrowList.get(arrowListIndex));
            });
        }
    }

    /**
     * Hides all arrows.
     */
    public void hideAllArrows() {
        Platform.runLater(() -> this.getChildren().setAll(mark, new Group()));
    }
}