package seng302.utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import seng302.model.GeoPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.token.Token;

/**
 * A class for generating and spawning tokens in random locations
 * Created by wmu16 on 27/09/17.
 */
public class RandomSpawn {

    private static final Integer DEGREES_IN_CIRCLE = 360;

    private HashMap<GeoPoint, Double> spawnRadii;
    private Random random;

    /**
     * @param markOrder this must be the ORDERED list of marks. Better yet UNIQUE to avoid over
     * computation
     */
    public RandomSpawn(List<CompoundMark> markOrder) {
        this.spawnRadii = new HashMap<>();
        random = new Random();

        spawnRadii = generateSpawnRadii(markOrder);
    }

    private HashMap<GeoPoint, Double> generateSpawnRadii(List<CompoundMark> markOrder) {
        System.out.println(markOrder);
        HashMap<GeoPoint, Double> spawnRadii = new HashMap<>();
        for (int i = 0; i < markOrder.size() - 1; i++) {
            GeoPoint spawnCentre = GeoUtility.getDirtyMidPoint(
                markOrder.get(i).getMidPoint(),
                markOrder.get(i + 1).getMidPoint());

            Double distance = GeoUtility.getDistance(spawnCentre, markOrder.get(i).getMidPoint());
            spawnRadii.put(spawnCentre, distance);
        }

        return spawnRadii;
    }


    /**
     * @return A random token type at a random location in a random radii of the set of possible
     * radii
     */
    public Token getRandomTokenLocation() {
        Object[] keys = spawnRadii.keySet().toArray();
        GeoPoint randomSpawnCentre = (GeoPoint) keys[random.nextInt(keys.length)];
        Double spawnRadius = spawnRadii.get(randomSpawnCentre);
        Double randomDistance = spawnRadius * random.nextDouble();
        Double randomAngle = random.nextDouble() * DEGREES_IN_CIRCLE;
        GeoPoint randomLocation = GeoUtility
            .getGeoCoordinate(randomSpawnCentre, randomAngle, randomDistance);
        return new Token(randomLocation);

    }

}
