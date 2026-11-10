package in.copmap.tracking.service;

import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.config.AppProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PingRateLimiter {

    private final StringRedisTemplate redis;
    private final AppProperties props;

    public void check(Long officerId) {
        String key = "rl:loc:" + officerId + ":" + (System.currentTimeMillis() / 60_000);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofSeconds(90));
        }
        if (count != null && count > props.tracking().maxPingsPerMinute()) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "Too many location updates");
        }
    }
}
