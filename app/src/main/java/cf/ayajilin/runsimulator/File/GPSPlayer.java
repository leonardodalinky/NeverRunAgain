package cf.ayajilin.runsimulator.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class GPSPlayer {

    public static class LocationWrapper {
        public final double latitude;
        public final double longitude;
        public final double altitude;
        public final long timestamp;

        private LocationWrapper(double longitude, double latitude, double altitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.timestamp = timestamp;
        }
    }

    private class Vector2d {
        double x, y;
        public Vector2d(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public Vector2d mul(Vector2d other) {
            return new Vector2d(x * other.x, y * other.y);
        }
        public double length() {
            return Math.sqrt(x * x + y * y);
        }
        public Vector2d div(double other) {
            return new Vector2d(x / other, y / other);
        }
    }

    private LocationWrapper[] locations;
    private int currentIndex = 0;
    private long initTime = 0;
    private long beginTime = 0;
    private long leftOverTime = 0;
    private final double speedNoiseLevel;
    private final double routeNoiseLevel;
    private double noiseParam0, noiseParam1, noiseParam2;

    public GPSPlayer(JSONArray jsonArray, double speedNoiseLevel, double routeNoiseLevel) throws JSONException {
        int len = jsonArray.length();
        locations = new LocationWrapper[len];
        for (int i = 0; i < len; ++ i) {
            JSONObject object = jsonArray.getJSONObject(i);
            locations[i] = new LocationWrapper(
                    object.getDouble("lon"),
                    object.getDouble("lat"),
                    20, object.getLong("time"));
        }

        Random random = new Random();
        noiseParam0 = random.nextInt(1000) / 10000d + -0.15;
        noiseParam1 = random.nextInt(1000) / 10000d + -0.05;
        noiseParam2 = random.nextInt(1000) / 10000d + 0.05;

        for (int i = 0; i < len * 3; ++ i) {
            LocationWrapper loc0 = locations[(i + len - 1) % len];
            LocationWrapper loc1 = locations[i % len];
            LocationWrapper loc2 = locations[(i + 1) % len];
            Vector2d vec = new Vector2d(loc2.longitude - loc0.longitude, loc2.latitude - loc0.latitude);
            Vector2d v_a = new Vector2d(loc1.longitude - loc0.longitude, loc1.latitude - loc0.latitude);
            if (vec.length() <= 0d) {
                continue;
            }
            double v_a_ratio = v_a.mul(vec).div(vec.length()).length();
            double ratio = Math.max(0.5, Math.min(v_a_ratio, 1 - v_a_ratio));
            // use p = 0.7 * r (0.7 approx 1/sqrt(2)) to control (p1, p2) in circle of radius r
            double noise1 = vec.length() * ratio * generateNoise(i, routeNoiseLevel * 0.7);
            double noise2 = vec.length() * ratio * generateNoise(i + 30, routeNoiseLevel * 0.7);
            locations[i % len] = new LocationWrapper(
                    loc1.longitude + noise1, loc1.latitude + noise2,
                    loc1.altitude * (1 + generateNoise(i, 0.2))
                    , loc1.timestamp);
        }


        this.speedNoiseLevel = speedNoiseLevel;
        this.routeNoiseLevel = routeNoiseLevel;
    }

    private boolean good() {
        return locations.length > 1;
    }

    private LocationWrapper at(int index) {
        if (good()) {
            return locations[index % locations.length];
        } else {
            return null;
        }
    }

    private long timeDistance(int index) {
        if (good()) {
            if (index % locations.length == locations.length - 1) {
                // last position
                return 1000;
            }
            return at(index + 1).timestamp - at(index).timestamp;
        } else {
            return 1000;
        }
    }

    private double linearInsert(double a1, double a2, double p) {
        return a1 + (a2 - a1) * p;
    }

    private double generateNoise(double x, double noiseLevel) {
        return (Math.sin(x * noiseParam0) + Math.sin(x * noiseParam1) + Math.sin(x * noiseParam2)) * noiseLevel / 3;
    }

    public void begin() {
        beginTime = System.currentTimeMillis();
        initTime = System.currentTimeMillis();
        leftOverTime = 0;
        currentIndex = 0;
    }

    public LocationWrapper getLocation() {
        long currentTime = System.currentTimeMillis();
        double delta = (currentTime - beginTime) * (1 + generateNoise((currentTime - initTime) / 1000d, speedNoiseLevel));
        leftOverTime += delta;
        // XposedBridge.log(currentTime - beginTime + " to " + delta);
        beginTime = currentTime;
        while (leftOverTime >= timeDistance(currentIndex)) {
            leftOverTime -= timeDistance(currentIndex);
            ++currentIndex;
        }
        double p = (double)leftOverTime / timeDistance(currentIndex);

        if (good()) {
            LocationWrapper loc1 = at(currentIndex);
            LocationWrapper loc2 = at(currentIndex + 1);
            LocationWrapper location = new LocationWrapper(
                    linearInsert(loc1.longitude, loc2.longitude, p),
                    linearInsert(loc1.latitude, loc2.latitude, p),
                    linearInsert(loc1.altitude, loc2.altitude, p),
                    currentTime
            );
            return location;
        } else {
            return null;
        }
    }


}
