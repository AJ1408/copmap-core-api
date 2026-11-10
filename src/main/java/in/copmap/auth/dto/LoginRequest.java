package in.copmap.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String badgeNo, @NotBlank String password) {}
