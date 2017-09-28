package seng302.visualiser.fxObjects.assets_3D;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Group;
import seng302.visualiser.fxObjects.MarkArrowFactory;
import seng302.visualiser.fxObjects.MarkArrowFactory.RoundingSide;
import seng302.visualiser.fxObjects.Marker;

/**
 * Visual object for a mark. Contains a coloured circle and any specified arrows.
 */
public class Marker3D extends Marker {

    private Model mark;
    private ModelType markType;
    private ModelType arrowType;

    /**
     * Creates a new Marker containing only a circle. The default colour is black.
     */
    public Marker3D(ModelType modelType) {
        markType = modelType;
        switch (markType) {
            case PLAIN_MARKER:
                arrowType = ModelType.PLAIN_ARROW;
                break;
            case FINISH_MARKER:
                arrowType = ModelType.FINISH_ARROW;
                break;
            case START_MARKER:
                arrowType = ModelType.START_ARROW;
                break;
        }
        mark = ModelFactory.importModel(modelType);
        Platform.runLater(() ->
            this.getChildren().addAll(mark.getAssets())
        );
    }

    /**
     * Adds an exit and entry arrow pair to the mark. Arrows are hidden and shown in the order they
     * are created by calling showNextEnterArrow() or showNextExitArrow()
     * @param roundingSide the side the marker will be from the perspective of the arrow.
     * @param entryAngle The angle the arrow will point towards a marker
     * @param exitAngle The angle the arrow wil point from the marker.
     */
    public void addArrows(RoundingSide roundingSide, double entryAngle,
        double exitAngle) {
        //Change Color.GRAY to this.colour to revert all gray arrows.
        enterArrows.add(
            MarkArrowFactory.constructEntryArrow3D(roundingSide, entryAngle, arrowType).getAssets()
        );
        exitArrows.add(
            MarkArrowFactory.constructExitArrow3D(roundingSide, exitAngle, arrowType).getAssets()
        );
    }

    @Override
    protected void showArrow(List<Group> arrowList, int arrowListIndex) {
        if (arrowListIndex < arrowList.size()) {
            Platform.runLater(() ->
                this.getChildren().setAll(mark.getAssets(), arrowList.get(arrowListIndex))
            );
        }
    }

    /**
     * Hides all arrows.
     */
    public void hideAllArrows() {
        Platform.runLater(() -> this.getChildren().setAll(mark.getAssets()));
    }
}