package in.copmap.tracking.redis;

import in.copmap.tracking.dto.LivePositionResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LivePositionStore {

    private final StringRedisTemplate redis;

    public void update(Long patrolId, Long officerId, double lat, double lng, long tsMillis) {
        String geoKey = "live:patrol:" + patrolId;
        String seenKey = "lastseen:patrol:" + patrolId;
        redis.opsForGeo().add(geoKey, new Point(lng, lat), officerId.toString());
        redis.opsForZSet().add(seenKey, officerId.toString(), tsMillis);
    }

    public List<LivePositionResponse> snapshot(Long patrolId) {
        String geoKey = "live:patrol:" + patrolId;
        String seenKey = "lastseen:patrol:" + patrolId;
        Set<String> members = redis.opsForZSet().reverseRange(seenKey, 0, -1);
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        String[] memberArray = members.toArray(new String[0]);
        List<Point> points = redis.opsForGeo().position(geoKey, memberArray);
        List<LivePositionResponse> result = new ArrayList<>(members.size());

        int index = 0;
        for (String idStr : members) {
            Long officerId = Long.valueOf(idStr);
            Double score = redis.opsForZSet().score(seenKey, idStr);
            Instant recordedAt = score != null ? Instant.ofEpochMilli(score.longValue()) : Instant.now();
            Point pt = (points != null && index < points.size()) ? points.get(index) : null;
            Double lat = pt != null ? pt.getY() : null;
            Double lng = pt != null ? pt.getX() : null;
            result.add(new LivePositionResponse(officerId, lat, lng, null, null, recordedAt));
            index++;
        }
        return result;
    }

    public List<Long> staleOfficers(Long patrolId, long olderThanMillis) {
        Set<String> ids = redis.opsForZSet().rangeByScore("lastseen:patrol:" + patrolId, 0, olderThanMillis);
        return ids == null ? List.of() : ids.stream().map(Long::valueOf).toList();
    }

    public void cleanup(Long patrolId) {
        redis.delete(List.of("live:patrol:" + patrolId, "lastseen:patrol:" + patrolId));
    }
}
