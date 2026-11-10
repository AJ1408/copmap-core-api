package in.copmap.operations.bandobast.dto;

import in.copmap.operations.bandobast.domain.OperationKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record CreateBandobastRequest(
        @NotBlank String title,
        @NotNull OperationKind kind,
        Long stationId,
        Instant startsAt,
        Instant endsAt,
        Long commanderId,
        List<PointDto> points
) {
    public record PointDto(
            String name,
            Double lat,
            Double lng,
            Integer radiusM,
            Integer requiredStrength
    ) {}
}

