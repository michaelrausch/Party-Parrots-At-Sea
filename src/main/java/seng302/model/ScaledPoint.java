package seng302.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Point2D;
import seng302.utilities.GeoUtility;

/**
 * Created by cir27 on 26/09/17.
 */
public class ScaledPoint extends GeoPoint {

    public enum ScaleDirection {
        HORIZONTAL,
        VERTICAL
    }

    private double x, y, scaleFactor;
    private ScaleDirection scaleDirection;

    private ScaledPoint(double lat, double lng, double x, double y, double scaleFactor, ScaleDirection direction) {
        super(lat, lng);
        this.x = x;
        this.y = y;
        this.scaleFactor = scaleFactor;
        this.scaleDirection = direction;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public ScaleDirection getScaleDirection() {
        return scaleDirection;
    }

    public Point2D findScaledXY(GeoPoint unscaled) {
        return findScaledXY(unscaled.getLat(), unscaled.getLng());
    }

    public Point2D findScaledXY(double unscaledLat, double unscaledLon) {
        double distanceFromReference;
        double angleFromReference;
        double xReference = this.getX();
        double yReference = this.getY();

        angleFromReference = GeoUtility.getBearingRad(
            this, new GeoPoint(unscaledLat, unscaledLon)
        );
        distanceFromReference = GeoUtility.getDistance(
            this, new GeoPoint(unscaledLat, unscaledLon)
        );
        if (angleFromReference >= 0 && angleFromReference <= Math.PI / 2) {
            xReference += scaleFactor * Math.sin(angleFromReference) * distanceFromReference;
            yReference -= scaleFactor * Math.cos(angleFromReference) * distanceFromReference;
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xReference += scaleFactor * Math.cos(angleFromReference) * distanceFromReference;
            yReference += scaleFactor * Math.sin(angleFromReference) * distanceFromReference;
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xReference -= scaleFactor * Math.sin(angleFromReference) * distanceFromReference;
            yReference -= scaleFactor * Math.cos(angleFromReference) * distanceFromReference;
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xReference -= scaleFactor * Math.cos(angleFromReference) * distanceFromReference;
            yReference += scaleFactor * Math.sin(angleFromReference) * distanceFromReference;
        }
        return new Point2D(xReference, yReference);
    }

    public static ScaledPoint makeScaledPoint(double width, double height,
        List<? extends GeoPoint> points, boolean centered) {

        double referencePointX, referencePointY, scaleFactor, lat, lng;
        ScaleDirection scaleDirection;
        points = new ArrayList<>(points);
        points.sort(Comparator.comparingDouble(GeoPoint::getLat));
        GeoPoint minLatPoint = points.get(0);
        GeoPoint maxLatPoint = points.get(points.size() - 1);

        points.sort(Comparator.comparingDouble(GeoPoint::getLng));
        GeoPoint minLonPoint = points.get(0);
        GeoPoint maxLonPoint = points.get(points.size() - 1);

        referencePointX = centered ? 0 : width / 2;
        referencePointY = centered ? 0 : height / 2;

        lat = (maxLatPoint.getLat() - minLatPoint.getLat()) / 2 + minLatPoint.getLat();
        lng = (maxLonPoint.getLng() - minLonPoint.getLng()) / 2 + minLonPoint.getLng();

        GeoPoint ref = new GeoPoint(lat, lng);

        double vertDistance = GeoUtility.getDistance(
            ref, new GeoPoint(ref.getLat(), maxLonPoint.getLng())
        ) * 2.1;

        double horiDistance = GeoUtility.getDistance(
            ref, new GeoPoint(maxLatPoint.getLat(), ref.getLng())
        ) * 2.1;

        double vertScale = height / vertDistance;

        if (horiDistance * vertScale > width) {
            scaleFactor = width / horiDistance;
            scaleDirection = ScaleDirection.HORIZONTAL;
        } else {
            scaleFactor = vertScale;
            scaleDirection = ScaleDirection.VERTICAL;
        }
        return new ScaledPoint(lat, lng, referencePointX, referencePointY, scaleFactor, scaleDirection);
    }
}
