package in.copmap.alert.dto;

import jakarta.validation.constraints.NotNull;

public record SosRequest(
        @NotNull Long patrolId,
        Double lat,
        Double lng,
        String message
) {}
