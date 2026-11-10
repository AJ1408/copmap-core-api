package in.copmap.alert.rules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.copmap.alert.domain.AlertType;
import in.copmap.alert.service.AlertService;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolWaypoint;
import in.copmap.operations.patrol.repository.PatrolRepository;
import in.copmap.tracking.dto.PingDto;
import in.copmap.tracking.events.PingReceivedEvent;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeofenceRuleTest {

    @Mock
    private AlertService alertService;

    @Mock
    private PatrolRepository patrolRepository;

    private GeofenceRule geofenceRule;

    private Patrol patrol;
    private final Long patrolId = 10L;
    private final Long officerId = 50L;

    @BeforeEach
    void setUp() {
        geofenceRule = new GeofenceRule(alertService, patrolRepository);

        patrol = new Patrol();
        patrol.setId(patrolId);
        patrol.setGeofenceRadiusM(200);

        PatrolWaypoint wp1 = new PatrolWaypoint(1L, patrol, 1, "Start", 28.6100, 77.2000);
        PatrolWaypoint wp2 = new PatrolWaypoint(2L, patrol, 2, "End", 28.6200, 77.2000);
        patrol.setWaypoints(List.of(wp1, wp2));
    }

    @Test
    void whenPingIsWithinRadius_noAlertRaised() {
        when(patrolRepository.findWithWaypoints(patrolId)).thenReturn(patrol);

        PingDto ping = new PingDto(28.6150, 77.2000, 5.0f, 1.2f, Instant.now());
        PingReceivedEvent event = new PingReceivedEvent(patrolId, officerId, ping);

        geofenceRule.onPing(event);

        verify(alertService, never()).raiseDeduped(any(), anyLong(), anyLong(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    void whenPingExceedsRadius_alertRaised() {
        when(patrolRepository.findWithWaypoints(patrolId)).thenReturn(patrol);

        PingDto ping = new PingDto(28.6150, 77.2100, 5.0f, 1.2f, Instant.now());
        PingReceivedEvent event = new PingReceivedEvent(patrolId, officerId, ping);

        geofenceRule.onPing(event);

        verify(alertService).raiseDeduped(
                eq(AlertType.GEOFENCE_BREACH),
                eq(patrolId),
                eq(officerId),
                contains("off route"),
                eq(28.6150),
                eq(77.2100)
        );
    }

    @Test
    void whenPatrolNotFound_noAlertRaised() {
        when(patrolRepository.findWithWaypoints(patrolId)).thenReturn(null);

        PingDto ping = new PingDto(28.6150, 77.2100, 5.0f, 1.2f, Instant.now());
        PingReceivedEvent event = new PingReceivedEvent(patrolId, officerId, ping);

        geofenceRule.onPing(event);

        verify(alertService, never()).raiseDeduped(any(), anyLong(), anyLong(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    void whenPatrolHasNoWaypoints_noAlertRaised() {
        patrol.setWaypoints(Collections.emptyList());
        when(patrolRepository.findWithWaypoints(patrolId)).thenReturn(patrol);

        PingDto ping = new PingDto(28.6150, 77.2100, 5.0f, 1.2f, Instant.now());
        PingReceivedEvent event = new PingReceivedEvent(patrolId, officerId, ping);

        geofenceRule.onPing(event);

        verify(alertService, never()).raiseDeduped(any(), anyLong(), anyLong(), anyString(), anyDouble(), anyDouble());
    }
}

