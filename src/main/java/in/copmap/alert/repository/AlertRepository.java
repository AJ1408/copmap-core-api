package in.copmap.alert.repository;

import in.copmap.alert.domain.Alert;
import in.copmap.alert.domain.AlertStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    List<Alert> findByOperationTypeAndOperationIdOrderByCreatedAtDesc(String operationType, Long operationId);

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findByOfficerIdOrderByCreatedAtDesc(Long officerId);

    List<Alert> findByOperationIdAndStatusOrderByCreatedAtDesc(Long operationId, AlertStatus status);
}
