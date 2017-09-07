package seng302.visualiser.fxObjects.assets_2D;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2
 * dimensional boat. It contains a single polygon for the boat, a group of lines to show it's path,
 * a wake object and two text labels to annotate the boat teams name and the boats velocity. The
 * boat will update it's position onscreen everytime UpdatePosition is called unless the window is
 * minimized in which case it attempts to store animations and apply them when the window is
 * maximised.
 */
public class BoatObject extends Group {


    private static final double MODEL_SCALE_FACTOR = 400;
    private static final double MODEL_X_OFFSET = 0; // standard
    private static final double MODEL_Y_OFFSET = 0; // standard

    private static final int VIEWPORT_SIZE = 800;

    private static final Color lightColor = Color.rgb(244, 255, 250);
    private static final Color jewelColor = Color.rgb(0, 190, 222);


    @FunctionalInterface
    public interface SelectedBoatListener {

        void notifySelected(BoatObject boatObject, Boolean isSelected);
    }

    //Constants for drawing
    private static final float BOAT_HEIGHT = 15f;
    private static final float BOAT_WIDTH = 10f;

    private double xVelocity;
    private double yVelocity;
    private double lastHeading;
    private double sailState;
    //Graphical objects
    private Polyline trail = new Polyline();
//    private Polygon boatPoly;
    private Shape3D boatPoly;
    private Polygon sail;
    private Wake wake;
    private Line leftLayLine;
    private Line rightLayline;
    private double distanceTravelled, lastRotation;
    private Point2D lastPoint;
    private Color colour = Color.BLACK;
    private Boolean isSelected = false, destinationSet;  //All boats are initialised as selected
    private boolean isPlayer = false;
    private Rotate rotation = new Rotate(0,0,1);

    private List<SelectedBoatListener> selectedBoatListenerListeners = new ArrayList<>();

    /**
     * Creates a BoatGroup with the default triangular boat polygon.
     */
    public BoatObject() {
        this(-BOAT_WIDTH / 2, BOAT_HEIGHT / 2,
            0.0, -BOAT_HEIGHT / 2,
            BOAT_WIDTH / 2, BOAT_HEIGHT / 2);
    }

    /**
     * Creates a BoatGroup with the boat being the default polygon. The head of the boat should be
     * at point (0,0).
     *
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat
     * polygon.
     */
    public BoatObject(double... points) {
        initChildren(points);
    }

    /**
     * Creates the javafx objects that will be the in the group by default.
     *
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat
     * polygon.
     */
    private void initChildren(double... points) {
        boatPoly = makeBoatPolygon();
        boatPoly.getTransforms().addAll(
            new Rotate(-40, new Point3D(1,0,0)),
            rotation,
            new Rotate(180, new Point3D(0, 0, 1))
        );
        boatPoly.getTransforms().add(new Scale(10, 10, 10));
//        boatPoly.setDrawMode(DrawMode.FILL);
//        boatPoly.setFill(colour);
//        boatPoly.setFill(this.colour);
//        boatPoly.setMaterial(new PhongMaterial(this.colour));
        boatPoly.setOnMouseEntered(event -> {
//            boatPoly.setFill(Color.FLORALWHITE);
//            boatPoly.setStroke(Color.RED);
//            boatPoly.setMaterial(new PhongMaterial(Color.FLORALWHITE));
        });
        boatPoly.setOnMouseExited(event -> {
//            boatPoly.setMaterial(new PhongMaterial(this.colour));
//            boatPoly.setFill(colour);
//            boatPoly.setFill(this.colour);
//            boatPoly.setStroke(Color.BLACK);
        });
        boatPoly.setOnMouseClicked(event -> setIsSelected(!isSelected));
        boatPoly.setCache(true);
//        boatPoly.setCacheHint(CacheHint.SPEED);

//        annotationBox = new AnnotationBox();
//        annotationBox.setFill(colour);

        leftLayLine = new Line();
        rightLayline = new Line();
        trail.getStrokeDashArray().setAll(5d, 10d);
        trail.setCache(true);
        wake = new Wake(0, -BOAT_HEIGHT);
        wake.setVisible(true);

        sail = new Polygon(0.0,BOAT_HEIGHT / 4,
            0.0, BOAT_HEIGHT);
        sailState = 0;
        sail.setStrokeWidth(2.0);
        sail.setStroke(Color.BLACK);
        sail.setFill(Color.TRANSPARENT);
        sail.setCache(true);
        super.getChildren().clear();
        PointLight pointLight = new PointLight(Color.WHITE);
//        pointLight.setLightOn(true);
        pointLight.getTransforms().add(new Translate(0, 0, -30));
        super.getChildren().add(pointLight);
//        pointLight = new PointLight(Color.WHITE);
////        pointLight.setLightOn(true);
//        pointLight.getTransforms().add(new Translate(50, 50, -20));
//        super.getChildren().add(pointLight);
//        pointLight = new PointLight(Color.WHITE);
////        pointLight.setLightOn(true);
//        pointLight.getTransforms().add(new Translate(50, -50, -20));
//        super.getChildren().add(pointLight);
//        pointLight = new PointLight(Color.WHITE);
////        pointLight.setLightOn(true);
//        pointLight.getTransforms().add(new Translate(-50, -50, -20));
//        super.getChildren().add(pointLight);
        AmbientLight light = new AmbientLight(new Color(0.5,0.5,0.5,1));
        super.getChildren().add(light);
        super.getChildren().addAll(boatPoly);//, sail);
    }

    public void setFill (Color value) {
        this.colour = value;
        PhongMaterial pm = new PhongMaterial(this.colour);
        pm.setSpecularPower(0.5);
        boatPoly.setMaterial(pm);
        trail.setStroke(colour);
    }

    public Shape3D makeBoatPolygon () {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(getClass().getResource("/meshes/hollow_simple_boat.stl").toString());
//        importer.read(getClass().getResource("/cube.stl").toString());
//        importer.read(getClass().getResource("/meshes/simple_yacht.stl").toString());
//        ObjModelImporter importer = new ObjModelImporter();
//        ColModelImporter importer = new ColModelImporter();
//        ColModelImporter importer = new ColModelImporter();
//        importer.read(getClass().getResource("/meshes/hollow_simple_boat.dae"));
//        MeshView mesh = importer.getImport()[0];
//        FileInputStream inputStream = null;
//        try{
//            inputStream = new FileInputStream(getClass().getResource("/meshes/simple_yacht.stl").toString());
//            Obj obj = ObjUtils.convertToRenderable(
//                ObjReader.read(inputStream));
//
//            IntBuffer indices = ObjData.getFaceVertexIndices(obj);
//            FloatBuffer vertices = ObjData.getVertices(obj);
//            FloatBuffer texCoords = ObjData.getTexCoords(obj);
//            MeshView
//            FloatBuffer normals = ObjData.getNormals(obj);
            return new MeshView(importer.getImport());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     *  @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     * @param rotation The rotation by which the boat moves
     * @param velocity The velocity the boat is moving
     * @param sailIn Boolean to toggle sail state.
     */
    public void moveTo(double x, double y, double rotation, double velocity, Boolean sailIn, double windDir) {
        Double dx = Math.abs(boatPoly.getLayoutX() - x);
        Double dy = Math.abs(boatPoly.getLayoutY() - y);
        Platform.runLater(() -> {
            rotateTo(rotation, sailIn, windDir);
            boatPoly.setLayoutX(x);
            boatPoly.setLayoutY(y);
            if (sailIn) {
//                sail.getPoints().clear();
//                sail.getPoints().addAll(0.0, 0.0, 4.0, 1.5, 8.0, 3.0, 12.0, 3.5, 16.0, 3.0, 20.0, 1.5, 24.0, 0.0);
//                sail.getPoints().addAll(0.0, 0.0, 24.0, 0.0);
                sail.setLayoutX(x);
                sail.setLayoutY(y);
            } else {
                animateSail();
                sail.setLayoutX(x);
                sail.setLayoutY(y);
            }
            wake.setLayoutX(x);
            wake.setLayoutY(y);
        });
        wake.setRotation(rotation, velocity);
//        rotateTo(rotation);
//        boatPoly.setLayoutX(x);
//        boatPoly.setLayoutY(y);
//        wake.setLayoutX(x);
//        wake.setLayoutY(y);
//        wake.rotate(rotation);

//        wake.setRotation(rotation, groundSpeed);
//        isStopped = false;
//        destinationSet = true;
        lastRotation = rotation;

        distanceTravelled += Math.sqrt((dx * dx) + (dy * dy));

        if (distanceTravelled > 15 && isPlayer) {
            distanceTravelled = 0d;
            Platform.runLater(() -> trail.getPoints().addAll(x, y));
        }
    }

    private Double normalizeHeading(double heading, double windDirection) {
        Double normalizedHeading = heading - windDirection;
        normalizedHeading = (double) Math.floorMod(normalizedHeading.longValue(), 360L);
        return normalizedHeading;
    }


    private void rotateTo(double heading, boolean sailsIn, double windDir) {
        rotation.setAngle(heading);
        if (sailsIn) {
            Double sailWindOffset = 30.0;
            Double upwindAngleLimit = 15.0;
            Double downwindAngleLimit = 10.0; //Upwind from normalised horizontal
            Double normalizedHeading = normalizeHeading(heading, windDir);
            if (normalizedHeading < 180) {
                sail.getTransforms().setAll(new Rotate(windDir + 90 + sailWindOffset));
                sail.getPoints().clear();
                sail.getPoints().addAll(0.0, 0.0, 4.0, -1.5, 8.0, -3.0, 12.0, -3.5, 16.0, -3.0, 20.0, -1.5, 24.0, 0.0);
                if (normalizedHeading > 90 + sailWindOffset){
                    sail.getTransforms().setAll(new Rotate(heading + downwindAngleLimit));
                }
                if (normalizedHeading < sailWindOffset + upwindAngleLimit){
                    sail.getTransforms().setAll(new Rotate(heading + 90 - upwindAngleLimit));
                }
            } else {
                sail.getTransforms().setAll(new Rotate(windDir + 90 - sailWindOffset));
                sail.getPoints().clear();
                sail.getPoints().addAll(0.0, 0.0, 4.0, 1.5, 8.0, 3.0, 12.0, 3.5, 16.0, 3.0, 20.0, 1.5, 24.0, 0.0);
                if (normalizedHeading < 270 - sailWindOffset){
                    sail.getTransforms().setAll(new Rotate(heading + 180 - downwindAngleLimit));
                }
                if (normalizedHeading > 360 - (sailWindOffset + upwindAngleLimit)){
                    sail.getTransforms().setAll(new Rotate(heading + 90 + upwindAngleLimit));
                }
            }
        } else {
            sail.getTransforms().setAll(new Rotate(windDir));
        }
    }


    private void animateSail(){
        Double[] points = new Double[200];
        double amplitude = 2.0;
        double period = 10;
        for (int i = 0; i < 50; i++) {
            points[i * 2] = amplitude * Math.sin(((Math.PI * i) / period + sailState));
            points[i * 2 + 1] = (double) (BOAT_HEIGHT * i) / BOAT_HEIGHT / 2;
            points[199 - (i * 2)] = (double) (BOAT_HEIGHT * i) / BOAT_HEIGHT / 2;
            points[199 - (i * 2 + 1)] = amplitude * Math.sin(((Math.PI * i) / period + sailState));
        }
        if (sailState == - 2 * Math.PI) {
            sailState = 0;
        } else {
            sailState = sailState - Math.PI / 5;
        }
        sail.getPoints().clear();
        sail.getPoints().addAll(points);

    }

    public void updateLocation() {
//        boatPoly.getTransforms().add(new Rotate(2, new Point3D(1,1,1)));
//        double dx = xVelocity / 60;
//        double dy = yVelocity / 60;
//
//        distanceTravelled += Math.abs(dx) + Math.abs(dy);
//        moveGroupBy(dx, dy);
//
//        if (distanceTravelled > 70) {
//            distanceTravelled = 0d;
//
//            if (lastPoint != null) {
//                Line l = new Line(
//                    lastPoint.getX(),
//                    lastPoint.getY(),
//                    boatPoly.getLayoutX(),
//                    boatPoly.getLayoutY()
//                );
//                l.getStrokeDashArray().setAll(3d, 7d);
//                l.setStroke(colour);
//                l.setCache(true);
//                l.setCacheHint(CacheHint.SPEED);
//                lineGroup.getChildren().add(l);
//            }
//            lastPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
//        }
//        wake.updatePosition();
    }

//    /**
//     * This function works out if a boat is going upwind or down wind. It looks at the boats current position, the next
//     * gates position and the current wind
//     * If bot the wind vector from the next gate and the boat from the next gate lay on the same side, then the boat is
//     * going up wind, if they are on different sides of the gate, then the boat is going downwind
//     * @param canvasController
//     */
//    public Boolean isUpwindLeg(GameViewController canvasController, Mark nextMark) {
//
//        Double windAngle = StreamParser.getWindDirection();
//        GateMark thisGateMark = (GateMark) nextMark;
//        SingleMark nextMark1 = thisGateMark.getSingleMark1();
//        SingleMark nextMark2 = thisGateMark.getSingleMark2();
//        Point2D nextMarkPoint1 = canvasController.findScaledXY(nextMark1.getLatitude(), nextMark1.getLongitude());
//        Point2D nextMarkPoint2 = canvasController.findScaledXY(nextMark2.getLatitude(), nextMark2.getLongitude());
//
//        Point2D boatCurrentPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
//        Point2D windTestPoint = GeoUtility.makeArbitraryVectorPoint(nextMarkPoint1, windAngle, 10d);
//
//
//        Integer boatLineFuncResult = GeoUtility.lineFunction(nextMarkPoint1, nextMarkPoint2, boatCurrentPoint);
//        Integer windLineFuncResult = GeoUtility.lineFunction(nextMarkPoint1, nextMarkPoint2, windTestPoint);
//
//
//        /*
//        If both the wind vector from the gate and the boat from the gate are on the same side of that gate, then the
//        boat is travelling into the wind. thus upwind. Otherwise if they are on different sides, then the boat is going
//        with the wind.
//         */
//        return boatLineFuncResult.equals(windLineFuncResult);
//        return true;
//    }

    public void setIsSelected(Boolean isSelected) {
        updateListener(isSelected);
        this.isSelected = isSelected;
        setLineGroupVisible(isSelected);
        setWakeVisible(isSelected);
        setLayLinesVisible(isSelected);
    }

    public void setVisibility (boolean teamName, boolean velocity, boolean estTime, boolean legTime,
        boolean trail, boolean wake) {
//        boatAnnotations.setVisible(teamName, velocity, estTime, legTime);
//        this.wake.setVisible(wake);
        this.trail.setVisible(trail);
    }

    public void setLineGroupVisible(Boolean visible) {
        trail.setVisible(visible);
    }

    public void setWakeVisible(Boolean visible) {
//        wake.setVisible(visible);
    }

    public void setLayLinesVisible(Boolean visible) {
        leftLayLine.setVisible(visible);
        rightLayline.setVisible(visible);
    }

    public void setLaylines(Line line1, Line line2) {
        this.leftLayLine = line1;
        this.rightLayline = line2;
    }

    public ArrayList<Line> getLaylines() {
        ArrayList<Line> laylines = new ArrayList<>();
        laylines.add(leftLayLine);
        laylines.add(rightLayline);
        return laylines;
    }

    public Group getWake () {
        return wake;
    }

    public Node getTrail() {
        return trail;
    }

    public Double getBoatLayoutX() {
        return boatPoly.getLayoutX();
    }


    public Double getBoatLayoutY() {
        return boatPoly.getLayoutY();
    }

    /**
     * Sets this boat to appear highlighted
     */
    public void setAsPlayer() {
//        boatPoly.getPoints().setAll(
//            -BOAT_WIDTH / 1.75, BOAT_HEIGHT / 1.75,
//            0.0, -BOAT_HEIGHT / 1.75,
//            BOAT_WIDTH / 1.75, BOAT_HEIGHT / 1.75
//        );
//        boatPoly.setStroke(Color.BLACK);
//        boatPoly.setStrokeWidth(2);
//        boatPoly.setStrokeLineCap(StrokeLineCap.ROUND);
        isPlayer = true;
        animateSail();
    }

    public void setTrajectory(double heading, double velocity, double windDir) {
        wake.setRotation(lastHeading - heading, velocity);
//        rotateTo(heading, false, windDir);
        xVelocity = Math.cos(Math.toRadians(heading)) * velocity;
        yVelocity = Math.sin(Math.toRadians(heading)) * velocity;
        lastHeading = heading;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setTrajectory(double heading, double velocity, double scaleFactorX, double scaleFactorY) {
//        wake.setRotation(lastHeading - heading, velocity);
//        rotateTo(heading);
//        xVelocity = Math.cos(Math.toRadians(heading)) * velocity * scaleFactorX;
//        yVelocity = Math.sin(Math.toRadians(heading)) * velocity * scaleFactorY;
        lastHeading = heading;
    }

    private void updateListener(Boolean isSelected) {
        for (SelectedBoatListener sbl : selectedBoatListenerListeners) {
            sbl.notifySelected(this, isSelected);
        }
    }

    public void addSelectedBoatListener(SelectedBoatListener sbl) {
        selectedBoatListenerListeners.add(sbl);
    }
}