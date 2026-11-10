package in.copmap.auth.service;

import in.copmap.auth.domain.Role;
import in.copmap.auth.domain.User;
import in.copmap.auth.security.AuthUser;
import in.copmap.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlMinutes;

    public JwtService(AppProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.ttlMinutes = properties.jwt().accessTtlMinutes();
    }

    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim("badgeNo", user.getBadgeNo())
                .claim("role", user.getRole().name())
                .claim("stationId", user.getStationId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public AuthUser toAuthUser(Claims claims) {
        Number station = claims.get("stationId", Number.class);
        return new AuthUser(Long.valueOf(claims.getSubject()), claims.get("badgeNo", String.class),
                Role.valueOf(claims.get("role", String.class)), station == null ? null : station.longValue(),
                claims.getId());
    }

    public long ttlSeconds() {
        return ttlMinutes * 60;
    }
}
