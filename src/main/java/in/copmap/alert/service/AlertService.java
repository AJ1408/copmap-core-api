package in.copmap.alert.service;

import in.copmap.alert.domain.Alert;
import in.copmap.alert.domain.AlertSeverity;
import in.copmap.alert.domain.AlertStatus;
import in.copmap.alert.domain.AlertType;
import in.copmap.alert.dto.AlertResponse;
import in.copmap.alert.dto.SosRequest;
import in.copmap.alert.notify.NotificationMessage;
import in.copmap.alert.notify.Notifier;
import in.copmap.alert.repository.AlertRepository;
import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.config.AppProperties;
import in.copmap.tracking.redis.LivePublisher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repo;
    private final StringRedisTemplate redis;
    private final LivePublisher publisher;
    private final Notifier notifier;
    private final AppProperties props;

    @Transactional
    public void raiseDeduped(AlertType type, Long patrolId, Long officerId, String message) {
        raiseDeduped(type, patrolId, officerId, message, null, null);
    }

    @Transactional
    public void raiseDeduped(AlertType type, Long patrolId, Long officerId, String message, Double lat, Double lng) {
        String key = "alert:dedupe:" + patrolId + ":" + type + ":" + officerId;
        Boolean first = redis.opsForValue().setIfAbsent(key, "1",
                Duration.ofMinutes(props.alerts().dedupeTtlMinutes()));
        if (!Boolean.TRUE.equals(first)) {
            return;
        }

        Alert alert = new Alert();
        alert.setType(type);
        alert.setSeverity(resolveSeverity(type));
        alert.setOperationType("PATROL");
        alert.setOperationId(patrolId);
        alert.setOfficerId(officerId);
        alert.setMessage(message);
        alert.setLat(lat);
        alert.setLng(lng);
        alert.setStatus(AlertStatus.OPEN);
        repo.save(alert);

        publisher.publish(patrolId, "ALERT", AlertResponse.from(alert));
        notifier.send(new NotificationMessage(officerId, "Alert: " + type, message));
    }

    @Transactional
    public AlertResponse raiseSos(SosRequest req, Long officerId) {
        Alert alert = new Alert();
        alert.setType(AlertType.SOS);
        alert.setSeverity(AlertSeverity.HIGH);
        alert.setOperationType("PATROL");
        alert.setOperationId(req.patrolId());
        alert.setOfficerId(officerId);
        alert.setMessage(req.message() != null ? req.message() : "SOS triggered by officer");
        alert.setLat(req.lat());
        alert.setLng(req.lng());
        alert.setStatus(AlertStatus.OPEN);
        repo.save(alert);

        publisher.publish(req.patrolId(), "ALERT", AlertResponse.from(alert));
        notifier.send(new NotificationMessage(officerId, "SOS Alert", alert.getMessage()));
        return AlertResponse.from(alert);
    }

    @Transactional
    public AlertResponse acknowledge(Long alertId, Long ackedByUserId) {
        Alert alert = repo.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Alert not found: " + alertId));
        if (alert.getStatus() != AlertStatus.ACKNOWLEDGED) {
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAckedBy(ackedByUserId);
            alert.setAckedAt(Instant.now());
            repo.save(alert);
            if (alert.getOperationId() != null) {
                publisher.publish(alert.getOperationId(), "ALERT_ACK", AlertResponse.from(alert));
            }
        }
        return AlertResponse.from(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> list(AlertStatus status, Long patrolId) {
        List<Alert> alerts = queryAlerts(status, patrolId);
        return alerts.stream().map(AlertResponse::from).toList();
    }

    private List<Alert> queryAlerts(AlertStatus status, Long patrolId) {
        if (status != null && patrolId != null) {
            return repo.findByOperationIdAndStatusOrderByCreatedAtDesc(patrolId, status);
        }
        if (status != null) {
            return repo.findByStatusOrderByCreatedAtDesc(status);
        }
        if (patrolId != null) {
            return repo.findByOperationTypeAndOperationIdOrderByCreatedAtDesc("PATROL", patrolId);
        }
        return repo.findAllByOrderByCreatedAtDesc();
    }

    private AlertSeverity resolveSeverity(AlertType type) {
        if (type == AlertType.SOS) {
            return AlertSeverity.HIGH;
        }
        if (type == AlertType.GEOFENCE_BREACH) {
            return AlertSeverity.MEDIUM;
        }
        return AlertSeverity.LOW;
    }
}
