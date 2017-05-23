package seng302.models;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Yacht class for the racing boat.
 *
 * Class created to store more variables (eg. boat statuses) compared to the XMLParser boat class,
 *  also done outside Boat class because some old variables are not used anymore.
 */
public class Yacht {
    // Used in boat group
    private Color colour;

    private DoubleProperty velocityProperty = new DoubleProperty() {

        private ObservableValue<? extends  Number> boundValue;
        private List<ChangeListener> changeListeners = new ArrayList<>();
        private List<InvalidationListener> invalidationListeners = new ArrayList<>();
        private double velocity;

        @Override
        public void bind(ObservableValue<? extends Number> observable) {
            boundValue = observable;
        }

        @Override
        public void unbind() {
            boundValue = null;
        }

        @Override
        public boolean isBound() {
            if (boundValue == null) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public Object getBean() {
            return Yacht.this;
        }

        @Override
        public String getName() {
            return "velocity property of " + boatName;
        }

        @Override
        public double get() {
            return velocity;
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            changeListeners.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            changeListeners.remove(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove(listener);
        }

        @Override
        public void set (double newVelocity) {
            double oldVelocity = velocity;
            velocity = newVelocity;
            if (newVelocity >= 0)
                for (ChangeListener cl : changeListeners) {
                    cl.changed(this, oldVelocity, newVelocity);
                }
            else
                for (InvalidationListener il : invalidationListeners) {
                    il.invalidated(this);
                }
            if (isBound())
                boundValue.notify();
        }
    };
    private LongProperty timeAtNextProperty = new LongProperty() {

        private ObservableValue<? extends  Number> boundValue;
        private List<ChangeListener> changeListeners = new ArrayList<>();
        private List<InvalidationListener> invalidationListeners = new ArrayList<>();
        private long estimate;

        @Override
        public void bind(ObservableValue<? extends Number> observable) {
            boundValue = observable;
        }

        @Override
        public void unbind() {
            boundValue = null;
        }

        @Override
        public boolean isBound() {
            if (boundValue == null) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public Object getBean() {
            return Yacht.this;
        }

        @Override
        public String getName() {
            return "estimated time to next mark property of " + boatName;
        }

        @Override
        public long get() {
            return estimate;
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            changeListeners.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            changeListeners.remove(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove(listener);
        }

        @Override
        public void set (long newEstimate) {
            long oldEstimate = estimate;
            estimate = newEstimate;
            if (newEstimate >= 0)
                for (ChangeListener cl : changeListeners) {
                    cl.changed(this, oldEstimate, newEstimate);
                }
            else
                for (InvalidationListener il : invalidationListeners) {
                    il.invalidated(this);
                }
            if (isBound())
                boundValue.notify();
        }
    };
    private LongProperty markRoundingTimeProperty = new LongProperty() {
        private ObservableValue<? extends  Number> boundValue;
        private List<ChangeListener> changeListeners = new ArrayList<>();
        private List<InvalidationListener> invalidationListeners = new ArrayList<>();
        private long roundingTime;

        @Override
        public void bind(ObservableValue<? extends Number> observable) {
            boundValue = observable;
        }

        @Override
        public void unbind() {
            boundValue = null;
        }

        @Override
        public boolean isBound() {
            if (boundValue == null) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public Object getBean() {
            return Yacht.this;
        }

        @Override
        public String getName() {
            return "time from last mark property of " + boatName;
        }

        @Override
        public long get() {
            return roundingTime;
        }

        @Override
        public void addListener(ChangeListener<? super Number> listener) {
            changeListeners.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Number> listener) {
            changeListeners.remove(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove(listener);
        }

        @Override
        public void set (long newTime) {
            long oldTime = newTime;
            roundingTime = newTime;
            if (newTime >= 0)
                for (ChangeListener cl : changeListeners) {
                    cl.changed(this, oldTime, newTime);
                }
            else
                for (InvalidationListener il : invalidationListeners) {
                    il.invalidated(this);
                }
            if (isBound())
                boundValue.notify();
        }
    };

    private String boatType;
    private Integer sourceID;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;
    // Boat status
    private Integer boatStatus;
    private Integer legNumber;
    private Integer penaltiesAwarded;
    private Integer penaltiesServed;
    private Long estimateTimeAtFinish;
    private String position;

    /**
     * Used in EventTest and RaceTest.
     *
     * @param boatName Create a yacht object with name.
     */
    public Yacht (String boatName) {
        this.boatName = boatName;
    }

    /**
     * Used in BoatGroupTest.
     *
     * @param boatName     The name of the team sailing the boat
     * @param boatVelocity The speed of the boat in meters/second
     * @param shortName    A shorter version of the teams name
     */
    public Yacht(String boatName, double boatVelocity, String shortName, int id) {
        this.boatName = boatName;
        this.velocityProperty.set(boatVelocity);
        this.shortName = shortName;
        this.sourceID = id;
    }

    public Yacht(String boatType, Integer sourceID, String hullID, String shortName, String boatName, String country) {
        this.boatType = boatType;
        this.sourceID = sourceID;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
    }

    public String getBoatType() {
        return boatType;
    }
    public Integer getSourceID() {
        return sourceID;
    }
    public String getHullID() {
        return hullID;
    }
    public String getShortName() {
        return shortName;
    }
    public String getBoatName() {
        return boatName;
    }
    public String getCountry() {
        return country;
    }

    public Integer getBoatStatus() {
        return boatStatus;
    }

    public void setBoatStatus(Integer boatStatus) {
        this.boatStatus = boatStatus;
    }

    public Integer getLegNumber() {
        return legNumber;
    }

    public void setLegNumber(Integer legNumber) {
        this.legNumber = legNumber;
    }

    public Integer getPenaltiesAwarded() {
        return penaltiesAwarded;
    }

    public void setPenaltiesAwarded(Integer penaltiesAwarded) {
        this.penaltiesAwarded = penaltiesAwarded;
    }

    public Integer getPenaltiesServed() {
        return penaltiesServed;
    }

    public void setPenaltiesServed(Integer penaltiesServed) {
        this.penaltiesServed = penaltiesServed;
    }

    public void setEstimateTimeAtNextMark(Long estimateTimeAtNextMark) {
        timeAtNextProperty.set(estimateTimeAtNextMark);
    }

    public String getEstimateTimeAtFinish() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(estimateTimeAtFinish);
    }

    public void setEstimateTimeAtFinish(Long estimateTimeAtFinish) {
        this.estimateTimeAtFinish = estimateTimeAtFinish;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public void setVelocity(double velocity) {
        velocityProperty.set(velocity);
    }


    public void setMarkRoundingTime(Long markRoundingTime) {
        markRoundingTimeProperty.set(markRoundingTime);
    }

    @Override
    public String toString() {
        return boatName;
    }

    public ReadOnlyDoubleProperty getReadOnlyVelocityProperty () {
        return velocityProperty;
    }

    public ReadOnlyLongProperty getReadOnlyNextMarkProperty() {
        return timeAtNextProperty;
    }

    public ReadOnlyLongProperty getReadOnlyMarkRoundingProperty() {
        return markRoundingTimeProperty;
    }
}
