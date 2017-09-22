package seng302.visualiser.fxObjects.assets_3D;

/**
 * Enum for boat meshes. Enum values should be of the form :
 * ENUM_VALUE (hull file, mast file, Y offset of mast CoR from origin, sail file, Y offset of sail CoR from origin, jib file, fixed sail)
 * Files must be valid .stl files.
 */
public enum BoatMeshType {

    DINGHY("dinghy_hull.stl", "dinghy_mast.stl", 1.36653, "dinghy_sail.stl", 1.36653, null, false, 1.7, 1.0, 5.0),
    CAT_ATE_A_MERINGUE("catamaran_hull.stl", "catamaran_mast.stl", 0.997, "catamaran_sail.stl",
        0.997, null, false, 1.0, 1.4, 10.0),
    PIRATE_SHIP("pirateship_hull.stl", "pirateship_mast.stl", -0.5415, "pirateship_mainsail.stl",
        -0.5415, "pirateship_frontsail.stl", true, 1.2, 1.6, 6.0);

    final String hullFile, mastFile, sailFile, jibFile;
    final double mastOffset, sailOffset;
    public final double maxSpeedMultiplier;
    public final double accelerationMultiplier;
    public final double turnStep;
    final boolean fixedSail;
    final static BoatMeshType[] boatTypes = new BoatMeshType[]{DINGHY, CAT_ATE_A_MERINGUE, PIRATE_SHIP};

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

    public static BoatMeshType getBoatMeshType(String boatType) {
        switch (boatType){
            case "DINGHY":
                return DINGHY;
            case "CAT_ATE_A_MERINGUE":
                return CAT_ATE_A_MERINGUE;
            case "PIRATE_SHIP":
                return PIRATE_SHIP;
            default:
                return DINGHY;
        }
    }


    //TODO kre39 make something not terrible to cycle through boat types
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
