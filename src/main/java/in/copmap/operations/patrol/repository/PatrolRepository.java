package in.copmap.operations.patrol.repository;

import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatrolRepository extends JpaRepository<Patrol, Long>, JpaSpecificationExecutor<Patrol> {

    Optional<Patrol> findByIdAndStationId(Long id, Long stationId);

    @EntityGraph(attributePaths = "waypoints")
    @Query("select p from Patrol p where p.id = :id")
    Patrol findWithWaypoints(@Param("id") Long id);

    @EntityGraph(attributePaths = "waypoints")
    @Query("select p from Patrol p where p.id = :id and p.stationId = :stationId")
    Optional<Patrol> findWithWaypointsByIdAndStationId(@Param("id") Long id, @Param("stationId") Long stationId);

    Page<Patrol> findByStationId(Long stationId, Pageable pageable);

    List<Patrol> findByStationId(Long stationId);

    List<Patrol> findByStatus(PatrolStatus status);
}
