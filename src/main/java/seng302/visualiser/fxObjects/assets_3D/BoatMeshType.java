package seng302.visualiser.fxObjects.assets_3D;

/**
 * Enum for boat meshes. Enum values should be of the form :
 * ENUM_VALUE (hull file, mast file, Y offset of mast CoR from origin, sail file, Y offset of sail CoR from origin, jib file, fixed sail)
 * Files must be valid .stl files.
 */
public enum BoatMeshType {

    DINGHY("dinghy_hull.stl", "dinghy_mast.stl", 1.36653, "dinghy_sail.stl", 1.36653, null, false),
    CAT_ATE_A_MERINGUE("catamaran_hull.stl", "catamaran_mast.stl", 0.997, "catamaran_sail.stl",
        0.997, null, false),
    PIRATE_SHIP("pirateship_hull.stl", "pirateship_mast.stl", -0.5415, "pirateship_mainsail.stl",
        -0.5415, "pirateship_frontsail.stl", true);

    final String hullFile, mastFile, sailFile, jibFile;
    final double mastOffset, sailOffset;
    final boolean fixedSail;

    BoatMeshType(String hullFile, String mastFile, double mastOffset, String sailFile,
        double sailOffset, String jibFile, boolean fixedSail) {
        this.hullFile = hullFile;
        this.mastFile = mastFile;
        this.mastOffset = mastOffset;
        this.sailFile = sailFile;
        this.sailOffset = sailOffset;
        this.jibFile = jibFile;
        this.fixedSail = fixedSail;
    }
}
