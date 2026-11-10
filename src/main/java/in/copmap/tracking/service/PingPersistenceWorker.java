package in.copmap.tracking.service;

import in.copmap.tracking.dto.PingDto;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PingPersistenceWorker {

    private final JdbcTemplate jdbc;

    private static final String SQL = """
        INSERT INTO location_pings (officer_id, patrol_id, lat, lng, accuracy_m, speed_mps, recorded_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (officer_id, patrol_id, recorded_at) DO NOTHING
        """;

    @Async("pingExecutor")
    public void enqueue(Long patrolId, Long officerId, List<PingDto> pings) {
        if (pings == null || pings.isEmpty()) {
            return;
        }
        jdbc.batchUpdate(SQL, pings, 100, (ps, p) -> {
            ps.setLong(1, officerId);
            ps.setLong(2, patrolId);
            ps.setDouble(3, p.lat());
            ps.setDouble(4, p.lng());
            if (p.accuracy() == null) {
                ps.setNull(5, Types.REAL);
            } else {
                ps.setFloat(5, p.accuracy());
            }
            if (p.speed() == null) {
                ps.setNull(6, Types.REAL);
            } else {
                ps.setFloat(6, p.speed());
            }
            ps.setTimestamp(7, Timestamp.from(p.recordedAt()));
        });
    }
}
