package in.copmap.tracking.service;

import in.copmap.auth.security.AuthUser;
import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.assignment.domain.AssignmentStatus;
import in.copmap.operations.assignment.domain.OperationType;
import in.copmap.operations.assignment.repository.AssignmentRepository;
import in.copmap.tracking.dto.LivePositionResponse;
import in.copmap.tracking.dto.PingBatchRequest;
import in.copmap.tracking.dto.PingDto;
import in.copmap.tracking.events.PingReceivedEvent;
import in.copmap.tracking.redis.LivePositionStore;
import in.copmap.tracking.redis.LivePublisher;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final PingRateLimiter limiter;
    private final LivePositionStore store;
    private final LivePublisher publisher;
    private final PingPersistenceWorker persister;
    private final ApplicationEventPublisher events;
    private final AssignmentRepository assignments;

    public void ingest(PingBatchRequest req, AuthUser me) {
        limiter.check(me.userId());
        verifyOnDuty(req.patrolId(), me.userId());
        PingDto latest = req.pings().stream()
                .max(Comparator.comparing(PingDto::recordedAt))
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "No pings provided"));

        store.update(req.patrolId(), me.userId(), latest.lat(), latest.lng(), latest.recordedAt().toEpochMilli());
        publisher.publish(req.patrolId(), "POSITION", LivePositionResponse.of(me.userId(), latest));
        persister.enqueue(req.patrolId(), me.userId(), req.pings());
        events.publishEvent(new PingReceivedEvent(req.patrolId(), me.userId(), latest));
    }

    public List<LivePositionResponse> getLivePositions(Long patrolId, AuthUser me) {
        return store.snapshot(patrolId);
    }

    private void verifyOnDuty(Long patrolId, Long officerId) {
        Assignment assignment = assignments.findByOperationTypeAndOperationIdAndOfficerId(
                OperationType.PATROL, patrolId, officerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE, "Officer has no assignment for patrol"));
        if (assignment.getStatus() != AssignmentStatus.ON_DUTY) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "Officer is not ON_DUTY for this patrol");
        }
    }
}
