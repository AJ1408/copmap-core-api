package in.copmap.alert.dto;

import in.copmap.alert.domain.Alert;
import in.copmap.alert.domain.AlertSeverity;
import in.copmap.alert.domain.AlertStatus;
import in.copmap.alert.domain.AlertType;
import java.time.Instant;

public record AlertResponse(
        Long id,
        AlertType type,
        AlertSeverity severity,
        String operationType,
        Long operationId,
        Long officerId,
        String message,
        Double lat,
        Double lng,
        AlertStatus status,
        Long ackedBy,
        Instant ackedAt,
        Instant createdAt
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getOperationType(),
                alert.getOperationId(),
                alert.getOfficerId(),
                alert.getMessage(),
                alert.getLat(),
                alert.getLng(),
                alert.getStatus(),
                alert.getAckedBy(),
                alert.getAckedAt(),
                alert.getCreatedAt()
        );
    }
}
