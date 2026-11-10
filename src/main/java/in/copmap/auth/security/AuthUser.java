package in.copmap.auth.security;

import in.copmap.auth.domain.Role;

public record AuthUser(Long userId, String badgeNo, Role role, Long stationId, String jti) {}
