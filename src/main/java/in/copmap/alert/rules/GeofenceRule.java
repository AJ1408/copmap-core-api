package in.copmap.alert.rules;

import in.copmap.alert.domain.AlertType;
import in.copmap.alert.service.AlertService;
import in.copmap.common.util.GeoUtils;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.repository.PatrolRepository;
import in.copmap.tracking.events.PingReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeofenceRule {

    private final AlertService alerts;
    private final PatrolRepository patrols;

    @EventListener
    public void onPing(PingReceivedEvent event) {
        Patrol patrol = patrols.findWithWaypoints(event.patrolId());
        if (patrol == null || patrol.getWaypoints() == null || patrol.getWaypoints().isEmpty()) {
            return;
        }

        double distance = GeoUtils.distanceToRouteMeters(
                event.ping().lat(), event.ping().lng(), patrol.getWaypoints());
        int radius = patrol.getGeofenceRadiusM() != null ? patrol.getGeofenceRadiusM() : 200;

        if (distance > radius) {
            alerts.raiseDeduped(
                    AlertType.GEOFENCE_BREACH,
                    patrol.getId(),
                    event.officerId(),
                    "Officer " + Math.round(distance) + "m off route",
                    event.ping().lat(),
                    event.ping().lng()
            );
        }
    }
}
