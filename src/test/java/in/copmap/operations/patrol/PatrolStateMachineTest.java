package in.copmap.operations.patrol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.patrol.domain.PatrolStatus;
import in.copmap.operations.patrol.service.PatrolStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatrolStateMachineTest {

    private PatrolStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new PatrolStateMachine();
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, PLANNED",
            "DRAFT, CANCELLED",
            "PLANNED, ACTIVE",
            "PLANNED, CANCELLED",
            "ACTIVE, COMPLETED"
    })
    void validTransitionsSucceed(PatrolStatus from, PatrolStatus to) {
        assertDoesNotThrow(() -> stateMachine.assertTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, ACTIVE",
            "DRAFT, COMPLETED",
            "PLANNED, DRAFT",
            "PLANNED, COMPLETED",
            "ACTIVE, DRAFT",
            "ACTIVE, PLANNED",
            "ACTIVE, CANCELLED",
            "COMPLETED, ACTIVE",
            "COMPLETED, DRAFT",
            "CANCELLED, ACTIVE",
            "CANCELLED, DRAFT"
    })
    void invalidTransitionsFail(PatrolStatus from, PatrolStatus to) {
        BusinessException ex = assertThrows(BusinessException.class, () -> stateMachine.assertTransition(from, to));
        assertEquals(ErrorCode.INVALID_STATE, ex.getCode());
    }
}

