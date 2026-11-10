package in.copmap.operations.patrol.dto;

import jakarta.validation.constraints.NotBlank;

public record ClosePatrolRequest(
    @NotBlank
    String remarks
) {}
