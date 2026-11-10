package in.copmap.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {}
