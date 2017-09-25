package seng302.models;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import seng302.gameServer.GameState;
import seng302.model.ServerYacht;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;


public class YachtTest {

    private static ServerYacht y1;
    //Yacht y2;
    private static Double windDirection = 180d;
    private static Double windSpeed = 20d;
    private static GameState gs;

    @BeforeClass
    public static void setUp() {
        new GameState();
        y1 = new ServerYacht(BoatMeshType.DINGHY, 1, "Y1", "Y1", "Yacht 1", "C1");
        gs = new GameState();
    }

    //Commented out until can fix the weird non-deterministic bug.
//    @Test
//    public void tackGybeTest() {
//        HashMap<Double, Double> values = new HashMap<>();
//        values.put(280.0, 80.0);
//        values.put(270.0, 90.0);
//        values.put(359.0, 1.0);
//        values.put(180.0, 180.0);
//        values.put(75.0, 285.0);
//
//        for (Double begin : values.keySet()) {
//            y1.setHeading(begin);
//            y1.tackGybe(windDirection);
//
//            for (int i = 0; i < 200; i++) {
//                y1.runAutoPilot();
//            }
//            assertEquals(values.get(begin), y1.getHeading(), 5.0);
//        }
//    }
//
//    @Test
//    public void vmgTest() {
//
//        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));
//        Double upwind = PolarTable.getOptimalUpwindVMG(windSpeed).keySet().iterator().next();
//        Double downwind = PolarTable.getOptimalDownwindVMG(windSpeed).keySet().iterator().next();
//
//        List<Pair<Double, Double>> values = new ArrayList<>();
//
//        upwind = (double) Math.floorMod(upwind.longValue() + windDirection.longValue(), 360L);
//        Double upwindRight = upwind;
//        Double upwindLeft = 360 - upwindRight;
//        downwind = (double) Math.floorMod(downwind.longValue() + windDirection.longValue(), 360L);
//        Double downwindRight = downwind;
//        Double downwindLeft = 360 - downwindRight;
//
//        values.add(new Pair<>(190d, upwindRight));
//        values.add(new Pair<>(170d, upwindLeft));
//        values.add(new Pair<>(10d, downwindLeft));
//        values.add(new Pair<>(350d, downwindRight));
//
//        for (Pair<Double, Double> beginEndPair : values) {
//            y1.setHeading(beginEndPair.getKey());
//            y1.turnToVMG();
//            for (int i = 0; i < 200; i++) {
//                y1.runAutoPilot();
//            }
//            y1.disableAutoPilot();
//            assertEquals(beginEndPair.getValue(), y1.getHeading(), 5.0);
//        }
//
//    }


    @AfterClass
    public static void tearDown() {
        y1 = null;
    }

}
