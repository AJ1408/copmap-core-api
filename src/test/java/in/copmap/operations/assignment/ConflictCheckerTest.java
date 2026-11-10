package in.copmap.operations.assignment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.assignment.repository.AssignmentRepository;
import in.copmap.operations.assignment.service.ConflictChecker;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConflictCheckerTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    private ConflictChecker conflictChecker;

    private final Instant now = Instant.parse("2026-11-10T08:00:00Z");
    private final Instant later = Instant.parse("2026-11-10T16:00:00Z");
    private final Long officerId = 101L;

    @BeforeEach
    void setUp() {
        conflictChecker = new ConflictChecker(assignmentRepository);
    }

    @Test
    void whenNoOverlap_thenNoConflict() {
        when(assignmentRepository.hasOverlap(officerId, now, later)).thenReturn(false);

        boolean hasConflict = conflictChecker.hasConflict(officerId, now, later);

        assertFalse(hasConflict);
        assertDoesNotThrow(() -> conflictChecker.checkConflict(officerId, now, later));
        assertDoesNotThrow(() -> conflictChecker.assertNoConflict(officerId, now, later));
        verify(assignmentRepository).hasOverlap(officerId, now, later);
    }

    @Test
    void whenOverlapExists_thenThrowsConflictException() {
        when(assignmentRepository.hasOverlap(officerId, now, later)).thenReturn(true);

        boolean hasConflict = conflictChecker.hasConflict(officerId, now, later);

        assertTrue(hasConflict);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                conflictChecker.checkConflict(officerId, now, later));
        assertEquals(ErrorCode.ASSIGNMENT_CONFLICT, ex.getCode());
    }
}

