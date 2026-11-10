package in.copmap.tracking.repository;

import in.copmap.tracking.domain.LocationPing;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationPingRepository extends JpaRepository<LocationPing, Long> {

    List<LocationPing> findByPatrolIdOrderByRecordedAtAsc(Long patrolId);

    List<LocationPing> findByPatrolIdAndOfficerIdOrderByRecordedAtAsc(Long patrolId, Long officerId);

    List<LocationPing> findByPatrolIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long patrolId, Instant start, Instant end);
}
