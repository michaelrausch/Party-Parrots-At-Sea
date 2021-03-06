package seng302.visualiser.fxObjects.assets_3D;

/**
 * Enum for boat meshes. Enum values should be of the form :
 * ENUM_VALUE (hull file, mast file, Y offset of mast CoR from origin, sail file, Y offset of sail CoR from origin, jib file, fixed sail)
 * Files must be valid .stl files.
 */
public enum BoatMeshType {

    DINGHY("dinghy_hull.stl", "dinghy_mast.stl", 1.36653, "dinghy_sail.stl", 1.36653, null, false, 1.8, 1.0, 1.0),
    CATAMARAN("catamaran_hull.stl", "catamaran_mast.stl", 0.997, "catamaran_sail.stl",
        0.997, null, false, 1.0, 1.4, 2.0),
    PIRATE_SHIP("pirateship_hull.stl", "pirateship_mast.stl", -0.5415, "pirateship_mainsail.stl",
        -0.5415, "pirateship_frontsail.stl", true, 1.2, 1.6, 1.2),
    DUCKY("ducky_hull.stl", "ducky_mast.stl", -2.18539, "ducky_sail.stl", -2.18539, "ducky_eyes.stl", false, 1.2, 1.1, 1.4),
    PARROT("parrot_hull.stl", null, 0, "parrot_features.stl", 0, "parrot_sail.stl", true, 1, 1, 1),
    WAKA("waka_hull.stl", "waka_mast.stl", 0, "waka_sail.stl", 0, null, true, 1.7, 0.5, 1.5);

    final String hullFile, mastFile, sailFile, jibFile;
    final double mastOffset, sailOffset;
    public final double maxSpeedMultiplier;
    public final double accelerationMultiplier;
    public final double turnStep;
    final boolean fixedSail;
    final static BoatMeshType[] boatTypes = new BoatMeshType[]{DINGHY, CATAMARAN, PIRATE_SHIP, DUCKY, PARROT, WAKA};

    BoatMeshType(String hullFile, String mastFile, double mastOffset, String sailFile,
        double sailOffset, String jibFile, boolean fixedSail, double maxSpeedMultiplier, double accelerationMultiplier, double turnStep) {
        this.hullFile = hullFile;
        this.mastFile = mastFile;
        this.mastOffset = mastOffset;
        this.sailFile = sailFile;
        this.sailOffset = sailOffset;
        this.jibFile = jibFile;
        this.fixedSail = fixedSail;
        this.maxSpeedMultiplier = maxSpeedMultiplier;
        this.accelerationMultiplier = accelerationMultiplier;
        this.turnStep = turnStep;
    }


    public static BoatMeshType getNextBoatType(BoatMeshType boatType) {
        for (int i = 0; i < boatTypes.length; i++) {
            if (i == boatTypes.length -1) {
                return boatTypes[0];
            } else if (boatType == boatTypes[i]) {
                return boatTypes[i+1];
            }
        }
        return boatType;
    }

    public static BoatMeshType getPrevBoatType(BoatMeshType boatType) {
        for (int i = 0; i < boatTypes.length; i++) {
            if (i == 0 && boatType == boatTypes[i]) {
                return boatTypes[boatTypes.length -1];
            } else if (boatType == boatTypes[i]) {
                return boatTypes[i-1];
            }
        }
        return boatType;
    }
}
