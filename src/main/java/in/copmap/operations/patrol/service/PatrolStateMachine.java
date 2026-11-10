package in.copmap.operations.patrol.service;

import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.patrol.domain.PatrolStatus;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PatrolStateMachine {

    private static final Map<PatrolStatus, Set<PatrolStatus>> ALLOWED = Map.of(
        PatrolStatus.DRAFT, Set.of(PatrolStatus.PLANNED, PatrolStatus.CANCELLED),
        PatrolStatus.PLANNED, Set.of(PatrolStatus.ACTIVE, PatrolStatus.CANCELLED),
        PatrolStatus.ACTIVE, Set.of(PatrolStatus.COMPLETED),
        PatrolStatus.COMPLETED, Set.of(),
        PatrolStatus.CANCELLED, Set.of()
    );

    public void assertTransition(PatrolStatus from, PatrolStatus to) {
        Set<PatrolStatus> next = ALLOWED.get(from);
        if (next == null || !next.contains(to)) {
            throw new BusinessException(
                ErrorCode.INVALID_STATE,
                "Cannot move patrol from " + from + " to " + to
            );
        }
    }
}
