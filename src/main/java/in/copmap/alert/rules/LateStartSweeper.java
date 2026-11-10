package in.copmap.alert.rules;

import in.copmap.alert.domain.AlertType;
import in.copmap.alert.service.AlertService;
import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.assignment.domain.AssignmentStatus;
import in.copmap.operations.assignment.domain.OperationType;
import in.copmap.operations.assignment.repository.AssignmentRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LateStartSweeper {

    private final AssignmentRepository assignments;
    private final AlertService alerts;

    @Scheduled(fixedDelay = 60000)
    public void sweep() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(15));
        List<Assignment> late = assignments.findByStatusAndStartsAtBefore(AssignmentStatus.ASSIGNED, cutoff);
        for (Assignment assignment : late) {
            handleLateAssignment(assignment);
        }
    }

    private void handleLateAssignment(Assignment assignment) {
        if (assignment.getOperationType() == OperationType.PATROL) {
            alerts.raiseDeduped(
                    AlertType.LATE_START,
                    assignment.getOperationId(),
                    assignment.getOfficerId(),
                    "Patrol assignment not started within 15 minutes of scheduled time"
            );
        }
    }
}
