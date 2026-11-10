package in.copmap.common.util;

import in.copmap.operations.patrol.domain.PatrolWaypoint;
import java.util.List;

public final class GeoUtils {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double METERS_PER_DEG_LAT = 111_320.0;

    private GeoUtils() {}

    public static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }

    public static double distanceToSegmentMeters(
            double lat, double lng, double latA, double lngA, double latB, double lngB) {
        double mPerDegLng = METERS_PER_DEG_LAT * Math.cos(Math.toRadians(lat));
        double ax = (lngA - lng) * mPerDegLng;
        double ay = (latA - lat) * METERS_PER_DEG_LAT;
        double bx = (lngB - lng) * mPerDegLng;
        double by = (latB - lat) * METERS_PER_DEG_LAT;
        double dx = bx - ax;
        double dy = by - ay;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, (-ax * dx - ay * dy) / len2));
        double cx = ax + t * dx;
        double cy = ay + t * dy;
        return Math.hypot(cx, cy);
    }

    public static double distanceToRouteMeters(double lat, double lng, List<PatrolWaypoint> wps) {
        if (wps == null || wps.isEmpty()) {
            return 0;
        }
        if (wps.size() == 1) {
            return haversineMeters(lat, lng, wps.get(0).getLat(), wps.get(0).getLng());
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < wps.size() - 1; i++) {
            PatrolWaypoint a = wps.get(i);
            PatrolWaypoint b = wps.get(i + 1);
            min = Math.min(min, distanceToSegmentMeters(lat, lng, a.getLat(), a.getLng(), b.getLat(), b.getLng()));
        }
        return min;
    }
}
