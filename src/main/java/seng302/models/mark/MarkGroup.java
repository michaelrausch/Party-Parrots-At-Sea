package seng302.models.mark;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import seng302.models.RaceObject;

/**
 * Created by CJIRWIN on 26/04/2017.
 */
public class MarkGroup extends RaceObject {

    private static int MARK_RADIUS = 5;

    private Mark mark;
    private double pixelVelocityXM1;
    private double pixelVelocityYM1;
    private double pixelVelocityXM2;
    private double pixelVelocityYM3;


    public MarkGroup (Mark mark, Point2D... points) {
        Color color = Color.BLACK;
        if (mark.getName().equals("Start")){
            color = Color.GREEN;
        } else if (mark.getName().equals("Finish")){
            color = Color.RED;
        }
        if (mark.getMarkType() == MarkType.SINGLE_MARK) {
            super.getChildren().add(new Circle(0, 0, MARK_RADIUS, color));
        } else {
            super.getChildren().add(new Circle(0, 0, MARK_RADIUS, color));
            super.getChildren().add(
                    new Circle(
                            points[1].getX() - points[0].getX(),
                            points[1].getY() - points[0].getY(),
                            MARK_RADIUS,
                            color
                    )
            );
            Line line = new Line(
                    0,
                    0,
                    points[1].getX() - points[0].getX(),
                    points[1].getY() - points[0].getY()
            );
            line.setStrokeWidth(2);
            if (mark.getMarkType() == MarkType.OPEN_GATE) {
                line.getStrokeDashArray().addAll(3d, 5d);
            }
            super.getChildren().add(line);
        }
        this.mark = mark;
        moveTo(points[0].getX(), points[0].getY());
    }

    public void setDestination (double x, double y, double rotation) {

    }

    public void setDestination (double x, double y) {

    }

    public void setMarkDestination (int markId, double x, double y) {

    }
    public void updatePosition (double timeInterval) {

    }

    public void moveTo (double x, double y, double rotation) {
        moveTo(x, y);
        super.getTransforms().clear();
        super.getTransforms().add(
                new Rotate(
                        rotation,
                        super.getChildren().get(1).getLayoutX() - super.getChildren().get(0).getLayoutX(),
                        super.getChildren().get(1).getLayoutY() - super.getChildren().get(0).getLayoutY()
                )
        );
    }

    public void moveTo (double x, double y) {
        super.setLayoutX(x);
        super.setLayoutY(y);
    }

    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds) {
            if (id == mark.getId())
                return true;
            if (mark.getMarkType() != MarkType.SINGLE_MARK) {
                if (id == ((GateMark) mark).getSingleMark1().getId() || id == ((GateMark) mark).getSingleMark2().getId())
                    return true;
            }
        }
        return false;
    }
    public void toggleAnnotations () {

    }

    public static int getMarkRadius() {
        return MARK_RADIUS;
    }

    public static void setMarkRadius(int markRadius) {
        MARK_RADIUS = markRadius;
    }

}
