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
        nodePixelVelocitiesX = new double[points.length];
        nodePixelVelocitiesY = new double[points.length];
        nodeDestinations     = new Point2D[points.length];
        marks.add(mark);
        mainMark = mark;
        Color color = Color.BLACK;
        if (mark.getName().equals("Start")){
            color = Color.GREEN;
        } else if (mark.getName().equals("Finish")){
            color = Color.RED;
        }
        Circle markCircle;
        if (mark.getMarkType() == MarkType.SINGLE_MARK) {
            markCircle = new Circle(
                    points[0].getX(),
                    points[0].getY(),
                    MARK_RADIUS,
                    color
            );
            nodeDestinations = new Point2D[]{
                    new Point2D(markCircle.getCenterX(), markCircle.getCenterY()
                    )
            };
            super.getChildren().add(markCircle);
        } else {
            marks.add(((GateMark) mark).getSingleMark1());
            marks.add(((GateMark) mark).getSingleMark2());
            nodePixelVelocitiesX = new double[]{0d,0d};
            nodePixelVelocitiesY = new double[]{0d,0d};
            nodeDestinations     = new Point2D[2];

            markCircle = new Circle(
                    points[0].getX(),
                    points[0].getY(),
                    MARK_RADIUS,
                    color
            );
            nodeDestinations[0] = new Point2D(markCircle.getCenterX(), markCircle.getCenterY());
            super.getChildren().add(markCircle);

            markCircle = new Circle(
                    points[1].getX(),
                    points[1].getY(),
                    MARK_RADIUS,
                    color
            );
            nodeDestinations[1] = new Point2D(markCircle.getCenterX(), markCircle.getCenterY());
            super.getChildren().add(markCircle);
            Line line = new Line(
                    points[0].getX(),
                    points[0].getY(),
                    points[1].getX(),
                    points[1].getY()
            );
            line.setStrokeWidth(LINE_THICKNESS);
            line.setStroke(color);
            if (mark.getMarkType() == MarkType.OPEN_GATE) {
                line.getStrokeDashArray().addAll(DASHED_GAP_LEN, DASHED_LINE_LEN);
            }
            super.getChildren().add(line);
        }
    }

    public void setDestination (double x, double y, double rotation, double groundSpeed, int... raceIds) {
        for (int i = 0; i < marks.size(); i++)
            for (int id : raceIds)
                if (id == marks.get(i).getId())
                    setDestinationChild(x, y, 0, Math.max(0, i-1));
        this.rotationalGoal = rotation;
        calculateRotationalVelocity();
    }


    private void setDestinationChild (double x, double y, double speed, int childIndex) {
        //double relativeX = x - super.getLayoutX();
        //double relativeY = y - super.getLayoutY();
        Circle markCircle = (Circle) super.getChildren().get(childIndex);
        this.nodeDestinations[childIndex] = new Point2D(x, y);
        //if (Math.abs(relativeX - markCircle.getCenterX()) > 30 && Math.abs(relativeY - markCircle.getCenterY()) > 30) {
            this.nodePixelVelocitiesX[childIndex] = (x - markCircle.getCenterX()) / expectedUpdateInterval;
            this.nodePixelVelocitiesY[childIndex] = (y - markCircle.getCenterY()) / expectedUpdateInterval;
        //}
    }

    public void rotateTo (double rotation) {
        if (mainMark.getMarkType() != MarkType.SINGLE_MARK) {
            Line line = (Line) super.getChildren().get(2);
            double xCenter = Math.abs(line.getEndX() - line.getStartX());
            double yCenter = Math.abs(line.getEndY() - line.getStartY());
            super.getTransforms().setAll(new Rotate(rotation, xCenter, yCenter));
        }
    }

    public void updatePosition (long timeInterval) {
        Circle markCircle = (Circle) super.getChildren().get(0);

        if (nodePixelVelocitiesX[0] > 0 && markCircle.getCenterX() > nodeDestinations[0].getX() ||
                nodePixelVelocitiesX[0] < 0 && markCircle.getCenterX() < nodeDestinations[0].getY())
            nodePixelVelocitiesX[0] = 0;
        else if (nodePixelVelocitiesX[0] != 0)
            markCircle.setCenterX(markCircle.getCenterX() + nodePixelVelocitiesX[0] * timeInterval);

        if (nodePixelVelocitiesY[0] > 0 && markCircle.getCenterY() > nodeDestinations[0].getY() ||
                nodePixelVelocitiesY[0] < 0 && markCircle.getCenterY() < nodeDestinations[0].getY())
            nodePixelVelocitiesY[0] = 0;
        else if (nodePixelVelocitiesY[0] != 0)
            markCircle.setCenterY(markCircle.getCenterY() + nodePixelVelocitiesY[0] * timeInterval);

        if (mainMark.getMarkType() != MarkType.SINGLE_MARK) {

            Line line = (Line) super.getChildren().get(2);
            line.setStartX(markCircle.getCenterX());
            line.setStartY(markCircle.getCenterY());

            markCircle = (Circle) super.getChildren().get(1);

            if (nodePixelVelocitiesX[1] > 0 && markCircle.getCenterX() >= nodeDestinations[1].getX() ||
                    nodePixelVelocitiesX[1] < 0 && markCircle.getCenterX() <= nodeDestinations[1].getX())
                nodePixelVelocitiesX[1] = 0;
            else if (nodePixelVelocitiesX[1] != 0)
                markCircle.setCenterX(markCircle.getCenterX() + nodePixelVelocitiesX[1] * timeInterval);

            if (nodePixelVelocitiesY[1] > 0 && markCircle.getCenterY() > nodeDestinations[1].getY() ||
                    nodePixelVelocitiesY[1] < 0 && markCircle.getCenterY() < nodeDestinations[1].getY())
                nodePixelVelocitiesY[1] = 0;
            else if (nodePixelVelocitiesY[1] != 0)
                markCircle.setCenterY(markCircle.getCenterY() + nodePixelVelocitiesY[1] * timeInterval);
            line.setEndX(markCircle.getCenterX());
            line.setEndY(markCircle.getCenterY());
        }
    }

    public void moveGroupBy (double x, double y, double rotation) {
        if (mainMark.getMarkType() != MarkType.SINGLE_MARK) {
            Line line = (Line) super.getChildren().get(2);
            for (int childIndex = 0; childIndex < 2; childIndex++){
                Circle mark = (Circle) super.getChildren().get(childIndex);
                mark.setCenterY(mark.getCenterY() + y);
                mark.setCenterX(mark.getCenterX() + x);
            }
            line.setStartX(line.getStartX() + x);
            line.setStartY(line.getStartY() + y);
            line.setEndX(line.getEndX() + x);
            line.setEndY(line.getEndY() + y);
        } else {
            Circle mark = (Circle) super.getChildren().get(0);
            mark.setCenterY(mark.getCenterY() + y);
            mark.setCenterX(mark.getCenterX() + x);
        }
        rotateTo(currentRotation + rotation);
    }

    public void moveTo (double x, double y, double rotation) {
        moveTo(x, y);
        rotateTo(rotation);
    }

    public void moveTo (double x, double y) {
        Circle markCircle = (Circle) super.getChildren().get(0);
        markCircle.setCenterX(x);
        markCircle.setCenterY(y);
        if (mainMark.getMarkType() != MarkType.SINGLE_MARK) {
            markCircle = (Circle) super.getChildren().get(1);
            markCircle.setCenterX(x);
            markCircle.setCenterY(y);
            Line line = (Line) super.getChildren().get(2);
            line.setStartX(x);
            line.setStartY(y);
            line.setEndX(x);
            line.setEndY(y);
        }
    }

    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds)
            for (Mark mark : marks)
            if (id == mark.getId())
                return true;
        return false;
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
