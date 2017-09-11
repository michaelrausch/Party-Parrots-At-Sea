package seng302.visualiser.fxObjects.assets_3D;

/**
 * Enum for models. Values should be the name of the file and files should be .dae files with texture
 * information included. Can be null in which case assets are assumed to be empty.
 */
public enum ModelType {

    VELOCITY_PICKUP("velocity_pickup.dae"),
    FINISH_MARKER ("finish_marker.dae"),
    START_MARKER ("start_marker.dae"),
    PLAIN_MARKER ("plain_marker.dae"),
    MARK_AREA ("mark_area.dae"),
    OCEAN (null),
    BORDER_PYLON ("barrier_pole.dae"),
    BORDER_BARRIER ("barrier_segment.dae"),
    FINISH_LINE ("finish_line.dae"),
    START_LINE ("start_line.dae"),
    GATE_LINE ("gate_line.dae"),
    WAKE ("wake.dae");

    final String filename;

    ModelType(String filename) {
        this.filename = filename;
    }
}

