package in.copmap.tracking.dto;

import java.time.Instant;

public record LivePositionResponse(
        Long officerId,
        Double lat,
        Double lng,
        Float accuracy,
        Float speed,
        Instant recordedAt
) {
    public static LivePositionResponse of(Long officerId, PingDto ping) {
        return new LivePositionResponse(
                officerId,
                ping.lat(),
                ping.lng(),
                ping.accuracy(),
                ping.speed(),
                ping.recordedAt()
        );
    }
}
