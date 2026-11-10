package in.copmap.operations.assignment.service;

import in.copmap.auth.domain.Role;
import in.copmap.auth.domain.User;
import in.copmap.auth.security.AuthUser;
import in.copmap.auth.service.AuthService;
import in.copmap.common.audit.AuditService;
import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.assignment.domain.AssignmentStatus;
import in.copmap.operations.assignment.domain.OperationType;
import in.copmap.operations.assignment.dto.AssignOfficersRequest;
import in.copmap.operations.assignment.dto.AssignmentResponse;
import in.copmap.operations.assignment.repository.AssignmentRepository;
import in.copmap.operations.events.DutyStartedEvent;
import in.copmap.operations.events.OfficerAssignedEvent;
import in.copmap.operations.events.PatrolClosedEvent;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.service.PatrolService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final PatrolService patrolService;
    private final ConflictChecker conflictChecker;
    private final AuthService authService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public List<AssignmentResponse> assignOfficers(Long patrolId, AssignOfficersRequest req, AuthUser me) {
        Patrol patrol = patrolService.findScopedPatrol(patrolId, me);
        List<Assignment> created = new ArrayList<>();

        for (Long officerId : req.officerIds()) {
            validateOfficer(officerId, patrol.getStationId());
            conflictChecker.checkConflict(officerId, patrol.getShiftStart(), patrol.getShiftEnd());

            Assignment assignment = buildAssignment(patrol, officerId);
            Assignment saved = assignmentRepository.save(assignment);
            created.add(saved);

            auditService.log(
                me.userId(),
                "OFFICER_ASSIGNED",
                "ASSIGNMENT",
                saved.getId(),
                Map.of("officerId", officerId, "patrolId", patrolId)
            );

            eventPublisher.publishEvent(new OfficerAssignedEvent(
                saved.getId(),
                officerId,
                OperationType.PATROL,
                patrolId
            ));
        }

        return created.stream().map(AssignmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignments(Long patrolId, AuthUser me) {
        patrolService.findScopedPatrol(patrolId, me);
        return assignmentRepository.findByOperationTypeAndOperationId(OperationType.PATROL, patrolId)
            .stream()
            .map(AssignmentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMyAssignments(AuthUser me) {
        return assignmentRepository.findByOfficerId(me.userId())
            .stream()
            .map(AssignmentResponse::from)
            .toList();
    }

    @Transactional
    public AssignmentResponse startDuty(Long patrolId, AuthUser me) {
        Assignment assignment = assignmentRepository.findByOperationTypeAndOperationIdAndOfficerId(
            OperationType.PATROL,
            patrolId,
            me.userId()
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Assignment not found for patrol"));

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new BusinessException(
                ErrorCode.INVALID_STATE,
                "Assignment cannot be started in status: " + assignment.getStatus()
            );
        }

        assignment.setStatus(AssignmentStatus.ON_DUTY);
        assignment.setStartedAt(Instant.now());
        assignmentRepository.save(assignment);

        patrolService.activateIfPlanned(patrolId);
        recordLastSeen(patrolId, me.userId());

        auditService.log(me.userId(), "DUTY_STARTED", "ASSIGNMENT", assignment.getId(), Map.of("patrolId", patrolId));
        eventPublisher.publishEvent(new DutyStartedEvent(patrolId, me.userId()));

        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public AssignmentResponse endDuty(Long patrolId, AuthUser me) {
        Assignment assignment = assignmentRepository.findByOperationTypeAndOperationIdAndOfficerId(
            OperationType.PATROL,
            patrolId,
            me.userId()
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Assignment not found for patrol"));

        if (assignment.getStatus() != AssignmentStatus.ON_DUTY) {
            throw new BusinessException(
                ErrorCode.INVALID_STATE,
                "Assignment is not ON_DUTY"
            );
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setEndedAt(Instant.now());
        assignmentRepository.save(assignment);

        auditService.log(me.userId(), "DUTY_ENDED", "ASSIGNMENT", assignment.getId(), Map.of("patrolId", patrolId));

        return AssignmentResponse.from(assignment);
    }

    @Transactional
    @EventListener
    public void onPatrolClosed(PatrolClosedEvent event) {
        List<Assignment> assignments = assignmentRepository.findByOperationTypeAndOperationId(
            OperationType.PATROL,
            event.patrolId()
        );
        Instant now = Instant.now();
        for (Assignment assignment : assignments) {
            completeIfActive(assignment, now);
        }
        cleanRedisKeys(event.patrolId());
    }

    private void completeIfActive(Assignment assignment, Instant now) {
        if (assignment.getStatus() == AssignmentStatus.ASSIGNED || assignment.getStatus() == AssignmentStatus.ON_DUTY) {
            assignment.setStatus(AssignmentStatus.COMPLETED);
            if (assignment.getEndedAt() == null) {
                assignment.setEndedAt(now);
            }
            assignmentRepository.save(assignment);
        }
    }

    private void recordLastSeen(Long patrolId, Long officerId) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForZSet().add(
                    "lastseen:patrol:" + patrolId,
                    officerId.toString(),
                    System.currentTimeMillis()
                );
            } catch (Exception ignored) {
            }
        }
    }

    private void cleanRedisKeys(Long patrolId) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(List.of("live:patrol:" + patrolId, "lastseen:patrol:" + patrolId));
            } catch (Exception ignored) {
            }
        }
    }

    private Assignment buildAssignment(Patrol patrol, Long officerId) {
        Assignment assignment = new Assignment();
        assignment.setOfficerId(officerId);
        assignment.setOperationType(OperationType.PATROL);
        assignment.setOperationId(patrol.getId());
        assignment.setStartsAt(patrol.getShiftStart());
        assignment.setEndsAt(patrol.getShiftEnd());
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        return assignment;
    }

    private void validateOfficer(Long officerId, Long stationId) {
        User officer = authService.getUserById(officerId);
        if (!officer.isActive()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Officer is inactive");
        }
        if (officer.getRole() != Role.OFFICER) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "User is not an officer");
        }
        if (stationId != null && !stationId.equals(officer.getStationId())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Officer does not belong to station");
        }
    }
}
