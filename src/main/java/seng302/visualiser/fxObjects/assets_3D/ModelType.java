package seng302.visualiser.fxObjects.assets_3D;

/**
 * Enum for models. Values should be the name of the file and files should be .dae files with texture
 * information included.
 */
public enum ModelType {

    VELOCITY_COIN ("velocity_pickup.dae"),
    FINISH_MARKER ("finish_marker.dae"),
    START_MARKER ("start_marker.dae"),
    PLAIN_MARKER ("plain_marker.dae"),
    MARK_AREA ("mark_area.dae"),
    OCEAN ("ocean.dae");

    final String filename;

    ModelType(String filename) {
        this.filename = filename;
    }
}

