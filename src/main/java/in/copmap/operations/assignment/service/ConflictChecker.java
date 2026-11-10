package in.copmap.operations.assignment.service;

import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.assignment.repository.AssignmentRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConflictChecker {

    private final AssignmentRepository assignmentRepository;

    public void checkConflict(Long officerId, Instant startsAt, Instant endsAt) {
        if (hasConflict(officerId, startsAt, endsAt)) {
            throw new BusinessException(
                ErrorCode.ASSIGNMENT_CONFLICT,
                "Officer " + officerId + " has an overlapping assignment"
            );
        }
    }

    public void assertNoConflict(Long officerId, Instant startsAt, Instant endsAt) {
        checkConflict(officerId, startsAt, endsAt);
    }

    public boolean hasConflict(Long officerId, Instant startsAt, Instant endsAt) {
        return assignmentRepository.hasOverlap(officerId, startsAt, endsAt);
    }
}
