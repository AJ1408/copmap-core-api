package in.copmap.alert.rules;

import in.copmap.alert.domain.AlertType;
import in.copmap.alert.service.AlertService;
import in.copmap.config.AppProperties;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolStatus;
import in.copmap.operations.patrol.repository.PatrolRepository;
import in.copmap.tracking.redis.LivePositionStore;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoSignalSweeper {

    private final PatrolRepository patrols;
    private final LivePositionStore store;
    private final AlertService alerts;
    private final AppProperties props;

    @Scheduled(fixedDelay = 60000)
    public void sweep() {
        int minutes = props.tracking().noSignalMinutes();
        long cutoff = System.currentTimeMillis() - Duration.ofMinutes(minutes).toMillis();
        List<Patrol> activePatrols = patrols.findByStatus(PatrolStatus.ACTIVE);
        for (Patrol patrol : activePatrols) {
            checkPatrol(patrol.getId(), cutoff, minutes);
        }
    }

    private void checkPatrol(Long patrolId, long cutoff, int minutes) {
        List<Long> stale = store.staleOfficers(patrolId, cutoff);
        for (Long officerId : stale) {
            alerts.raiseDeduped(
                    AlertType.NO_SIGNAL,
                    patrolId,
                    officerId,
                    "No location update for " + minutes + " minutes"
            );
        }
    }
}
