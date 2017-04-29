package seng302.models.mark;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import seng302.models.RaceObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJIRWIN on 26/04/2017.
 */
public class MarkGroup extends RaceObject {

    private static int MARK_RADIUS        = 5;
    private static int LINE_THICKNESS     = 2;
    private static double DASHED_GAP_LEN  = 2d;
    private static double DASHED_LINE_LEN = 5d;

    private List<Mark> marks = new ArrayList<>();
    private Mark mainMark;
    private double[] nodePixelVelocitiesX;
    private double[] nodePixelVelocitiesY;
    private Point2D[] nodeDestinations;

    public MarkGroup (Mark mark, Point2D... points) {
        marks.add(mark);
        mainMark = mark;
        Color color = Color.BLACK;
        if (mark.getName().equals("Start")){
            color = Color.GREEN;
        } else if (mark.getName().equals("Finish")){
            color = Color.RED;
        }
        System.out.println("HERE ARE THE CHILDREN LOL");
        if (mark.getMarkType() == MarkType.SINGLE_MARK) {
            super.getChildren().add(new Circle(0, 0, MARK_RADIUS, color));
            nodeDestinations     = new Point2D[]{
                    new Point2D(super.getChildren().get(0).getLayoutX()
                            , super.getChildren().get(0).getLayoutY())};
        } else {
            marks.add(((GateMark) mark).getSingleMark1());
            marks.add(((GateMark) mark).getSingleMark2());
            super.getChildren().add(
                    new Circle(
                            (points[1].getX() - points[0].getX()) / 2d,
                            (points[1].getY() - points[0].getY()) / 2d,
                            MARK_RADIUS,
                            color
                    )
            );
            super.getChildren().add(
                    new Circle(
                            -(points[1].getX() - points[0].getX()) / 2d,
                            -(points[1].getY() - points[0].getY()) / 2d,
                            MARK_RADIUS,
                            color
                    )
            );
            Line line = new Line(
                    (points[1].getX() - points[0].getX()) / 2d,
                    (points[1].getY() - points[0].getY()) / 2d,
                    -(points[1].getX() - points[0].getX()) / 2d,
                    -(points[1].getY() - points[0].getY()) / 2d
            );
            line.setStrokeWidth(LINE_THICKNESS);
            line.setStroke(color);
            if (mark.getMarkType() == MarkType.OPEN_GATE) {
                line.getStrokeDashArray().addAll(DASHED_GAP_LEN, DASHED_LINE_LEN);
            }
            super.getChildren().add(line);
            nodePixelVelocitiesX = new double[]{0d,0d};
            nodePixelVelocitiesY = new double[]{0d,0d};
            nodeDestinations     = new Point2D[]{
                    new Point2D(super.getChildren().get(0).getLayoutX(), super.getChildren().get(0).getLayoutY()),
                    new Point2D(super.getChildren().get(1).getLayoutX(), super.getChildren().get(1).getLayoutY())
            };
        }
        moveTo(points[0].getX(), points[0].getY());
    }

    public void setDestination (double x, double y, double rotation, int... raceIds) {
        setDestination(x, y, raceIds);
        this.rotationalGoal = rotation;
        calculateRotationalVelocity();
    }

    public void setDestination (double x, double y, int... raceIds) {
        int childrenIndex = -1;
        for (Mark mark : marks) {
            for (int id : raceIds)
                if (id == mark.getId() && childrenIndex != -1)
                    setDestinationChild(x, y, childrenIndex);
                else if (id == mark.getId())
                    setDestinationGroup(x, y);
            childrenIndex++;
        }
        updateChildren();
    }


    private void setDestinationChild (double x, double y, int childIndex) {
        double relativeX = x - super.getLayoutX();
        double relativeY = y - super.getLayoutY();
        this.nodeDestinations[childIndex]     = new Point2D(relativeX, relativeY);
        this.nodePixelVelocitiesX[childIndex] = (relativeX - super.getChildren().get(childIndex).getLayoutX()) / expectedUpdateInterval;
        this.nodePixelVelocitiesY[childIndex] = (relativeY - super.getChildren().get(childIndex).getLayoutY()) / expectedUpdateInterval;

    }

    private void setDestinationGroup (double x, double y) {
        double relativeX = x - super.getLayoutX();
        double relativeY = y - super.getLayoutY();
        this.nodeDestinations[0] = new Point2D(relativeX, relativeY);
        pixelVelocityX = (x - super.getLayoutX()) / expectedUpdateInterval;
        pixelVelocityY = (y - super.getLayoutY()) / expectedUpdateInterval;
    }


    public void rotateTo (double rotation) {
        super.getTransforms().clear();
        super.getTransforms().add(new Rotate(rotation));
    }

    public void updatePosition (long timeInterval) {
//        double x = pixelVelocityX * timeInterval;
//        double y = pixelVelocityY * timeInterval;
//        double rotation = rotationalVelocity * timeInterval;
//        moveGroupBy(x, y, rotation);
//        updateChildren(timeInterval);
    }

    public void moveGroupBy (double x, double y, double rotation) {
        super.setLayoutX(super.getLayoutX() + x);
        super.setLayoutY(super.getLayoutY() + y);
        rotateTo(rotation);
    }

    private void updateChildren () {

        if (mainMark.getMarkType() != MarkType.SINGLE_MARK) {
            Line line = (Line) super.getChildren().get(2);
            for (int childIndex = 0; childIndex < 2; childIndex++){
                Circle mark = (Circle) super.getChildren().get(childIndex);
                Point2D dest = nodeDestinations[childIndex];
                mark.setCenterY(dest.getY());
                mark.setCenterX(dest.getX());
            }
            line.setStartX(nodeDestinations[0].getX());
            line.setStartY(nodeDestinations[0].getY());
            line.setEndX(nodeDestinations[1].getX());
            line.setEndY(nodeDestinations[1].getY());
//            Circle mark = (Circle) super.getChildren().get(0);
//            if (nodePixelVelocitiesX[0] > 0 && mark.getLayoutX() >= nodeDestinations[0].getX()) {
//                nodePixelVelocitiesX[0] = 0;
//            } else if (nodePixelVelocitiesX[0] < 0 && mark.getLayoutX() <= nodeDestinations[0].getX()) {
//                nodePixelVelocitiesX[0] = 0;
//            } else {
//                mark.setLayoutX(mark.getLayoutX() + nodePixelVelocitiesX[0] * timeInterval);
//                mark.setLayoutY(mark.getLayoutY() + nodePixelVelocitiesY[0] * timeInterval);
//            }
//            if (nodePixelVelocitiesY[0] >= 0 && mark.getLayoutY() > nodeDestinations[0].getY()) {
//                nodePixelVelocitiesY[0] = 0;
//            } else if (nodePixelVelocitiesY[0] < 0 && mark.getLayoutY() <= nodeDestinations[0].getY()) {
//                nodePixelVelocitiesY[0] = 0;
//            } else {
//                mark.setLayoutX(mark.getLayoutX() + nodePixelVelocitiesX[0] * timeInterval);
//                mark.setLayoutY(mark.getLayoutY() + nodePixelVelocitiesY[0] * timeInterval);
//            }
//            mark = (Circle) super.getChildren().get(1);
//            if (nodePixelVelocitiesX[1] > 0 && mark.getLayoutX() >= nodeDestinations[1].getX()) {
//                nodePixelVelocitiesX[1] = 0;
//            } else if (nodePixelVelocitiesX[1] < 0 && mark.getLayoutX() <= nodeDestinations[1].getX()) {
//                nodePixelVelocitiesX[1] = 0;
//            } else {
//                mark.setLayoutX(mark.getLayoutX() + nodePixelVelocitiesX[1] * timeInterval);
//                mark.setLayoutY(mark.getLayoutY() + nodePixelVelocitiesY[1] * timeInterval);
//            }
//            if (nodePixelVelocitiesY[1] >= 0 && mark.getLayoutY() > nodeDestinations[1].getY()) {
//                nodePixelVelocitiesY[1] = 0;
//            } else if (nodePixelVelocitiesY[1] < 0 && mark.getLayoutY() <= nodeDestinations[1].getY()) {
//                nodePixelVelocitiesY[1] = 0;
//            } else {
//                mark.setLayoutX(mark.getLayoutX() + nodePixelVelocitiesX[1] * timeInterval);
//                mark.setLayoutY(mark.getLayoutY() + nodePixelVelocitiesY[1] * timeInterval);
//            }
        } else {
            Circle mark = (Circle) super.getChildren().get(0);
            Point2D dest = nodeDestinations[0];
            mark.setCenterY(dest.getY());
            mark.setCenterX(dest.getX());
        }
    }

    public void moveTo (double x, double y, double rotation) {
        moveTo(x, y);
        rotateTo(rotation);
    }

    public void moveTo (double x, double y) {
        super.setLayoutX(x);
        super.setLayoutY(y);
    }

    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds)
            for (Mark mark : marks)
            if (id == mark.getId())
                return true;
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

    public int[] getRaceIds () {
        int[] idArray = new int[marks.size()];
        int i = 0;
        for (Mark mark : marks)
            idArray[i++] = mark.getId();
        return idArray;
    }

}
