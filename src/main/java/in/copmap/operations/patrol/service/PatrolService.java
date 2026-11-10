package in.copmap.operations.patrol.service;

import in.copmap.auth.domain.Role;
import in.copmap.auth.security.AuthUser;
import in.copmap.common.api.PageResponse;
import in.copmap.common.audit.AuditLog;
import in.copmap.common.audit.AuditLogRepository;
import in.copmap.common.audit.AuditService;
import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.events.PatrolClosedEvent;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolStatus;
import in.copmap.operations.patrol.domain.PatrolWaypoint;
import in.copmap.operations.patrol.dto.ClosePatrolRequest;
import in.copmap.operations.patrol.dto.CreatePatrolRequest;
import in.copmap.operations.patrol.dto.PatrolResponse;
import in.copmap.operations.patrol.dto.WaypointDto;
import in.copmap.operations.patrol.repository.PatrolRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatrolService {

    private final PatrolRepository patrolRepository;
    private final PatrolStateMachine stateMachine;
    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PatrolResponse create(CreatePatrolRequest req, AuthUser me) {
        Long stationId = resolveStationId(me);
        Patrol patrol = buildPatrol(req, me, stationId);
        addWaypoints(patrol, req.waypoints());

        Patrol saved = patrolRepository.save(patrol);
        auditService.log(me.userId(), "PATROL_CREATED", "PATROL", saved.getId(), Map.of("title", saved.getTitle()));
        return PatrolResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PatrolResponse getById(Long id, AuthUser me) {
        Patrol patrol = findScopedPatrol(id, me);
        return PatrolResponse.from(patrol);
    }

    @Transactional(readOnly = true)
    public PageResponse<PatrolResponse> list(PatrolStatus status, LocalDate date, int page, int size, AuthUser me) {
        Specification<Patrol> spec = buildSearchSpec(status, date, me);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Patrol> paged = patrolRepository.findAll(spec, pageable);

        List<PatrolResponse> content = paged.getContent().stream()
            .map(PatrolResponse::from)
            .toList();

        return new PageResponse<>(content, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages());
    }

    @Transactional
    public PatrolResponse publish(Long id, AuthUser me) {
        Patrol patrol = findScopedPatrol(id, me);
        stateMachine.assertTransition(patrol.getStatus(), PatrolStatus.PLANNED);
        patrol.setStatus(PatrolStatus.PLANNED);

        auditService.log(me.userId(), "PATROL_PUBLISHED", "PATROL", id, Map.of("status", PatrolStatus.PLANNED.name()));
        return PatrolResponse.from(patrol);
    }

    @Transactional
    public PatrolResponse close(Long id, ClosePatrolRequest req, AuthUser me) {
        Patrol patrol = findScopedPatrol(id, me);
        stateMachine.assertTransition(patrol.getStatus(), PatrolStatus.COMPLETED);
        patrol.setStatus(PatrolStatus.COMPLETED);
        patrol.setClosedBy(me.userId());
        patrol.setCloseRemarks(req.remarks());

        auditService.log(me.userId(), "PATROL_CLOSED", "PATROL", id, Map.of("remarks", req.remarks()));
        eventPublisher.publishEvent(new PatrolClosedEvent(id));
        return PatrolResponse.from(patrol);
    }

    @Transactional
    public void activateIfPlanned(Long id) {
        Patrol patrol = patrolRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Patrol not found"));

        if (patrol.getStatus() == PatrolStatus.PLANNED) {
            stateMachine.assertTransition(patrol.getStatus(), PatrolStatus.ACTIVE);
            patrol.setStatus(PatrolStatus.ACTIVE);
            patrolRepository.save(patrol);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditTrail(Long id, AuthUser me) {
        findScopedPatrol(id, me);
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("PATROL", id);
    }

    public Patrol findScopedPatrol(Long id, AuthUser me) {
        Patrol patrol = patrolRepository.findWithWaypoints(id);
        if (patrol == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Patrol not found");
        }
        if (me.role() != Role.SUPERVISOR && !patrol.getStationId().equals(me.stationId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Patrol not found");
        }
        return patrol;
    }

    private Long resolveStationId(AuthUser me) {
        Long stationId = me.stationId();
        if (stationId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Station ID is required");
        }
        return stationId;
    }

    private Patrol buildPatrol(CreatePatrolRequest req, AuthUser me, Long stationId) {
        Patrol patrol = new Patrol();
        patrol.setTitle(req.title());
        patrol.setType(req.type());
        patrol.setStationId(stationId);
        patrol.setStatus(PatrolStatus.DRAFT);
        patrol.setShiftStart(req.shiftStart());
        patrol.setShiftEnd(req.shiftEnd());
        patrol.setGeofenceRadiusM(req.geofenceRadiusM() != null ? req.geofenceRadiusM() : 200);
        patrol.setCreatedBy(me.userId());
        return patrol;
    }

    private void addWaypoints(Patrol patrol, List<WaypointDto> waypointDtos) {
        if (waypointDtos == null) {
            return;
        }
        for (WaypointDto wpDto : waypointDtos) {
            PatrolWaypoint waypoint = new PatrolWaypoint();
            waypoint.setSeq(wpDto.seq());
            waypoint.setName(wpDto.name());
            waypoint.setLat(wpDto.lat());
            waypoint.setLng(wpDto.lng());
            patrol.addWaypoint(waypoint);
        }
    }

    private Specification<Patrol> buildSearchSpec(PatrolStatus status, LocalDate date, AuthUser me) {
        Specification<Patrol> spec = Specification.where(null);
        if (me.role() != Role.SUPERVISOR) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("stationId"), me.stationId()));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (date != null) {
            spec = spec.and((root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("shiftStart"), date.atStartOfDay(ZoneOffset.UTC).toInstant()),
                cb.lessThan(root.get("shiftStart"), date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())
            ));
        }
        return spec;
    }
}
