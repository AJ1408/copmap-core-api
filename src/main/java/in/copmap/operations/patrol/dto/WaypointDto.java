package in.copmap.operations.patrol.dto;

import in.copmap.operations.patrol.domain.PatrolWaypoint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record WaypointDto(
    @NotNull
    Integer seq,
    String name,
    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    Double lat,
    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    Double lng
) {
    public static WaypointDto from(PatrolWaypoint wp) {
        if (wp == null) {
            return null;
        }
        return new WaypointDto(wp.getSeq(), wp.getName(), wp.getLat(), wp.getLng());
    }
}
