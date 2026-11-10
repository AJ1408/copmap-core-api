package in.copmap.tracking.events;

import in.copmap.tracking.dto.PingDto;

public record PingReceivedEvent(
        Long patrolId,
        Long officerId,
        PingDto ping
) {}
