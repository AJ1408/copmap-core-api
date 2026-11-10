package in.copmap.operations.assignment.dto;

import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.assignment.domain.AssignmentStatus;
import in.copmap.operations.assignment.domain.OperationType;
import java.time.Instant;

public record AssignmentResponse(
    Long id,
    Long officerId,
    OperationType operationType,
    Long operationId,
    Long pointId,
    Instant startsAt,
    Instant endsAt,
    AssignmentStatus status,
    Instant startedAt,
    Instant endedAt
) {
    public static AssignmentResponse from(Assignment a) {
        if (a == null) {
            return null;
        }
        return new AssignmentResponse(
            a.getId(),
            a.getOfficerId(),
            a.getOperationType(),
            a.getOperationId(),
            a.getPointId(),
            a.getStartsAt(),
            a.getEndsAt(),
            a.getStatus(),
            a.getStartedAt(),
            a.getEndedAt()
        );
    }
}
