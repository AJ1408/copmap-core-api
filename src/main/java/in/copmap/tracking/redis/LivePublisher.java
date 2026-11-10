package in.copmap.tracking.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LivePublisher {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public void publish(Long patrolId, String type, Object payload) {
        try {
            String json = mapper.writeValueAsString(Map.of("type", type, "data", payload));
            redis.convertAndSend("chan:patrol:" + patrolId, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise live event", e);
        }
    }
}
