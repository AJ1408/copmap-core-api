package in.copmap.operations.bandobast.dto;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull Double lat,
        @NotNull Double lng
) {}

