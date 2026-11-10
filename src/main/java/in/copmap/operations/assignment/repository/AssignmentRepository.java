package in.copmap.operations.assignment.repository;

import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.assignment.domain.AssignmentStatus;
import in.copmap.operations.assignment.domain.OperationType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("""
        select count(a) > 0 from Assignment a
        where a.officerId = :officerId
          and a.status in ('ASSIGNED','ON_DUTY')
          and a.startsAt < :end and a.endsAt > :start
    """)
    boolean hasOverlap(@Param("officerId") Long officerId, @Param("start") Instant start, @Param("end") Instant end);

    List<Assignment> findByOperationTypeAndOperationId(OperationType operationType, Long operationId);

    List<Assignment> findByOfficerId(Long officerId);

    Optional<Assignment> findByOperationTypeAndOperationIdAndOfficerId(
        OperationType operationType,
        Long operationId,
        Long officerId
    );

    List<Assignment> findByStatusAndStartsAtBefore(AssignmentStatus status, Instant startsAt);
}
