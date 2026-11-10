package in.copmap.common.audit;

import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;

    @Transactional
    public AuditLog log(Long actorId, String action, String entityType, Long entityId,
                        Map<String, Object> details) {
        AuditLog audit = new AuditLog();
        audit.setActorId(actorId);
        audit.setAction(action);
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setDetails(details);
        audit.setCreatedAt(Instant.now());
        return repository.save(audit);
    }
}
