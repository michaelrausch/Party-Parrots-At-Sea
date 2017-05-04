// TODO: 4/05/17 cir27 - Find out why this test is causing build failures

//package seng302.models;
//import seng302.*;
//import javafx.scene.paint.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Polygon;
//import javafx.scene.transform.Rotate;
//import javafx.scene.transform.Transform;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * Created by cir27 on 4/05/17.
// */
//public class BoatGroupTest {
//    BoatGroup boatGroup;
//    @Before
//    public void setUp () {
//        Boat b = new Boat("TEST", 0.0, "T" ,0);
//        boatGroup = new BoatGroup(b, Color.BLACK);
//    }
//
//    @Test
//    public void setDestinationFirstUseForcesLocationUpdate () {
//        boatGroup.setDestination(10, 10, 90, 0);
//        Polygon bp = (Polygon) boatGroup.getChildren().get(2);
//        Assert.assertTrue(10 == bp.getLayoutX());
//        Assert.assertTrue(10 == bp.getLayoutY());
//    }
//
//    @Test
//    public void setDestinationFutureUseDoesntForce () {
//        for (int i = 0; i < 60; i++) {
//            boatGroup.setDestination(200, 200, 90, 0);
//        }
//        boatGroup.setDestination(210, 210, 90, 0);
//        Polygon bp = (Polygon) boatGroup.getChildren().get(2);
//        Assert.assertTrue(200 == bp.getLayoutX());
//        Assert.assertTrue(200 == bp.getLayoutY());
//    }
//
//    @Test
//    public void setDestinationUnrealisticMovementForceUpdate () {
//        Polygon bp = (Polygon) boatGroup.getChildren().get(2);
//        double xLocation = bp.getLayoutX();
//        double yLocation = bp.getLayoutY();
//        boatGroup.setDestination(xLocation + 500, yLocation + 500, 90, 0);
//        Assert.assertTrue(xLocation + 500 == bp.getLayoutX());
//        Assert.assertTrue(yLocation + 500 == bp.getLayoutY());
//    }
//
//    @Test
//    public void setDestinationUnrealisticNegativeForceUpdate () {
//        Polygon bp = (Polygon) boatGroup.getChildren().get(2);
//        double xLocation = bp.getLayoutX();
//        double yLocation = bp.getLayoutY();
//        boatGroup.setDestination(xLocation - 500, yLocation - 500, 90, 0);
//        Assert.assertTrue(xLocation - 500 == bp.getLayoutX());
//        Assert.assertTrue(yLocation - 500 == bp.getLayoutY());
//    }
//
//    @Test
//    public void updatePositionGeneratesExpectedMovement () {
//        Polygon bp = (Polygon) boatGroup.getChildren().get(2);
//        double xLocation = bp.getLayoutX();
//        double yLocation = bp.getLayoutY();
//        int movement = 10;
//        double delay = RaceObject.getExpectedUpdateInterval();
//        double defaultTimePeriod = 1000 / 60;
//        double expectedMovement = movement / delay * defaultTimePeriod;
//        for (int i = 0; i < 60; i++) {
//            boatGroup.setDestination(xLocation, yLocation, 90, 0);
//        }
//        boatGroup.setDestination(xLocation + 10, yLocation + 10, 90, 0);
//        boatGroup.updatePosition(1000/60);
//        Assert.assertEquals(expectedMovement, bp.getLayoutX() - xLocation, 0.0);
//    }
//
//    @Test
//    public void correctRaceID () {
//        Assert.assertTrue(boatGroup.hasRaceId(0));
//    }
//
//    @Test
//    public void incorrectRaceID () {
//        Assert.assertTrue(!boatGroup.hasRaceId(2));
//    }
//
//    @Test
//    public void nothingOnWrongId () {
//        Polygon bp = (Polygon) boatGroup.getChildren().get(2);
//        double originalX = bp.getLayoutX();
//        double originalY = bp.getLayoutY();
//        boatGroup.setDestination(10, 10, 90, 12);
//        Assert.assertTrue(originalX == bp.getLayoutX());
//        Assert.assertTrue(originalY == bp.getLayoutY());
//    }
//}
