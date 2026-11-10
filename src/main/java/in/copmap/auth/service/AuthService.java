package in.copmap.auth.service;

import in.copmap.auth.domain.User;
import in.copmap.auth.dto.LoginRequest;
import in.copmap.auth.dto.LoginResponse;
import in.copmap.auth.dto.UserResponse;
import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final in.copmap.auth.repository.UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TokenBlacklistService blacklist;

    public LoginResponse login(LoginRequest request) {
        User user = users.findByBadgeNo(request.badgeNo())
                .filter(User::isActive)
                .filter(candidate -> encoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                        "Invalid badge number or password"));
        return new LoginResponse(jwt.generate(user), "Bearer", jwt.ttlSeconds(), UserResponse.from(user));
    }

    public void logout(String bearerToken) {
        Claims claims = jwt.parse(bearerToken);
        blacklist.blacklist(claims.getId(), claims.getExpiration().toInstant());
    }

    public User getUserById(Long id) {
        return users.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
    }
}
