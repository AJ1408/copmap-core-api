package in.copmap.config;

import in.copmap.auth.domain.Role;
import in.copmap.auth.security.AuthUser;
import in.copmap.auth.service.JwtService;
import in.copmap.auth.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwt;
    private final TokenBlacklistService blacklist;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = extractBearerToken(accessor);
        AuthUser user = validateAndResolveUser(token);
        if (user.role() == Role.OFFICER) {
            throw new MessagingException("Officers cannot subscribe to live feeds");
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
        accessor.setUser(auth);
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessagingException("Missing bearer token");
        }
        return header.substring(7);
    }

    private AuthUser validateAndResolveUser(String token) {
        try {
            Claims claims = jwt.parse(token);
            if (blacklist.isBlacklisted(claims.getId())) {
                throw new MessagingException("Token revoked");
            }
            return jwt.toAuthUser(claims);
        } catch (JwtException | IllegalArgumentException e) {
            throw new MessagingException("Invalid token");
        }
    }
}
