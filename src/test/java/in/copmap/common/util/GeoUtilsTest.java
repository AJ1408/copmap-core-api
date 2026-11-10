package in.copmap.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import in.copmap.operations.patrol.domain.PatrolWaypoint;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    void haversineMeters_samePointReturnsZero() {
        double dist = GeoUtils.haversineMeters(28.6139, 77.2090, 28.6139, 77.2090);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void haversineMeters_oneDegreeLatitudeIsApproximately111km() {
        double dist = GeoUtils.haversineMeters(28.0, 77.0, 29.0, 77.0);
        assertTrue(dist > 110_000 && dist < 112_000);
    }

    @Test
    void distanceToRouteMeters_emptyRouteReturnsZero() {
        double dist = GeoUtils.distanceToRouteMeters(28.6139, 77.2090, Collections.emptyList());
        assertEquals(0.0, dist);
    }

    @Test
    void distanceToRouteMeters_singleWaypointReturnsHaversine() {
        PatrolWaypoint wp = new PatrolWaypoint(1L, null, 1, "WP1", 28.6139, 77.2090);
        double dist = GeoUtils.distanceToRouteMeters(28.6139, 77.2090, List.of(wp));
        assertEquals(0.0, dist, 0.001);

        double distFar = GeoUtils.distanceToRouteMeters(28.6239, 77.2090, List.of(wp));
        assertTrue(distFar > 1000);
    }

    @Test
    void distanceToRouteMeters_pointOnRouteReturnsNearZero() {
        PatrolWaypoint wp1 = new PatrolWaypoint(1L, null, 1, "A", 28.6100, 77.2000);
        PatrolWaypoint wp2 = new PatrolWaypoint(2L, null, 2, "B", 28.6300, 77.2000);

        double dist = GeoUtils.distanceToRouteMeters(28.6200, 77.2000, List.of(wp1, wp2));
        assertTrue(dist < 5.0);
    }

    @Test
    void distanceToRouteMeters_pointOffRouteReturnsDistance() {
        PatrolWaypoint wp1 = new PatrolWaypoint(1L, null, 1, "A", 28.6100, 77.2000);
        PatrolWaypoint wp2 = new PatrolWaypoint(2L, null, 2, "B", 28.6300, 77.2000);

        double offRouteLng = 77.2000 + (200.0 / (111_320.0 * Math.cos(Math.toRadians(28.6200))));
        double dist = GeoUtils.distanceToRouteMeters(28.6200, offRouteLng, List.of(wp1, wp2));
        assertEquals(200.0, dist, 10.0);
    }

    @Test
    void distanceToRouteMeters_pointBeyondEndReturnsEndpointDistance() {
        PatrolWaypoint wp1 = new PatrolWaypoint(1L, null, 1, "A", 28.6100, 77.2000);
        PatrolWaypoint wp2 = new PatrolWaypoint(2L, null, 2, "B", 28.6200, 77.2000);

        double dist = GeoUtils.distanceToRouteMeters(28.6300, 77.2000, List.of(wp1, wp2));
        double expected = GeoUtils.haversineMeters(28.6300, 77.2000, 28.6200, 77.2000);
        assertEquals(expected, dist, 5.0);
    }
}

