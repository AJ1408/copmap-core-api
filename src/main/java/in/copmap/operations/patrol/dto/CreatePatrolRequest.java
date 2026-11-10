package in.copmap.operations.patrol.dto;

import in.copmap.operations.patrol.domain.PatrolType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record CreatePatrolRequest(
    @NotBlank
    String title,
    @NotNull
    PatrolType type,
    @NotNull
    Instant shiftStart,
    @NotNull
    Instant shiftEnd,
    Integer geofenceRadiusM,
    List<@Valid WaypointDto> waypoints
) {}
