package in.copmap.operations.events;

public record DutyStartedEvent(
    Long patrolId,
    Long officerId
) {}
