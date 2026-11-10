package in.copmap.operations.events;

import in.copmap.operations.assignment.domain.OperationType;

public record OfficerAssignedEvent(
    Long assignmentId,
    Long officerId,
    OperationType operationType,
    Long operationId
) {}
