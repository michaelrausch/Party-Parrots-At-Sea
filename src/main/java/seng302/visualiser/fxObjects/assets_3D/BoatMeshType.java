package seng302.visualiser.fxObjects.assets_3D;

/**
 * Enum for boat meshes. Enum values should be of the form :
 * ENUM_VALUE (hull file, mast file, X offset of mast CoR from origin, sail file, X offset of sail CoR from origin)
 * Files must be valid .stl files.
 */
public enum BoatMeshType {

    DINGHY ("dinghy_hull.stl", "dinghy_mast.stl", 0, "dinghy_sail.stl", -1.36653);

    final String hullFile, mastFile, sailFile;
    final double mastOffset, sailOffset;

    BoatMeshType(String hullFile, String mastFile, double mastOffset, String sailFile, double sailOffset) {
        this.hullFile = hullFile;
        this.mastFile = mastFile;
        this.mastOffset = mastOffset;
        this.sailFile = sailFile;
        this.sailOffset = sailOffset;
    }
}
