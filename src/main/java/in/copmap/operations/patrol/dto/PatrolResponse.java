package in.copmap.operations.patrol.dto;

import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolStatus;
import in.copmap.operations.patrol.domain.PatrolType;
import java.time.Instant;
import java.util.List;

public record PatrolResponse(
    Long id,
    String title,
    PatrolType type,
    Long stationId,
    PatrolStatus status,
    Instant shiftStart,
    Instant shiftEnd,
    Integer geofenceRadiusM,
    Long createdBy,
    Long closedBy,
    String closeRemarks,
    Instant createdAt,
    List<WaypointDto> waypoints
) {
    public static PatrolResponse from(Patrol p) {
        if (p == null) {
            return null;
        }
        List<WaypointDto> waypointDtos = p.getWaypoints() == null
            ? List.of()
            : p.getWaypoints().stream().map(WaypointDto::from).toList();

        return new PatrolResponse(
            p.getId(),
            p.getTitle(),
            p.getType(),
            p.getStationId(),
            p.getStatus(),
            p.getShiftStart(),
            p.getShiftEnd(),
            p.getGeofenceRadiusM(),
            p.getCreatedBy(),
            p.getClosedBy(),
            p.getCloseRemarks(),
            p.getCreatedAt(),
            waypointDtos
        );
    }
}
