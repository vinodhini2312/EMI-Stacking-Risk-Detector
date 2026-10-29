package com.emiguard;

import com.emiguard.model.*;
import com.emiguard.service.IncomeService;
import com.emiguard.service.LoanService;
import com.emiguard.service.SimulatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimulatorServiceTest {

    @Autowired
    private SimulatorService simulatorService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private IncomeService incomeService;

    @BeforeEach
    public void setUp() {
        loanService.clearAllLoans();
        incomeService.resetToDefaultPattern();
    }

    @Test
    public void testSimulationIdentifiesNewlyCreatedShortfalls() {
        LocalDate base = LocalDate.now();
        // Income is 9000 weekly
        // Loan 1: 7000 due on day 10
        Loan loan = new Loan("Existing Loan", LoanType.PERSONAL_LOAN, 7000.0, 10, base.minusMonths(1), 12);
        loanService.saveLoan(loan);

        // Simulate new EMI of ₹3000 due on day 10 (Total = 10000 > 9000)
        LocalDate due = base.withDayOfMonth(10);
        SimulationRequest req = new SimulationRequest("New Shopping EMI", LoanType.BNPL, 3000.0, due, 6);

        SimulationResult result = simulatorService.simulateNewLoan(req);

        assertNotNull(result);
        assertEquals("SHORTFALL_RISK", result.getVerdict());
        assertFalse(result.getNewlyRiskyWeeks().isEmpty(), "Should detect newly risky week created by adding 3000 to 7000");
        assertTrue(result.getAfterRiskyWeeks() > result.getBeforeRiskyWeeks());
        assertTrue(result.getMaxProjectedShortfall() > 0);
    }

    @Test
    public void testSimulationSafeToTakeWhenIncomeIsSufficient() {
        LocalDate base = LocalDate.now();
        Loan loan = new Loan("Small Existing EMI", LoanType.BNPL, 2000.0, 10, base.minusMonths(1), 6);
        loanService.saveLoan(loan);

        // Simulate small new loan of ₹1500 (Total = 3500 < 9000)
        SimulationRequest req = new SimulationRequest("New Headphones", LoanType.PHONE_DEVICE_LOAN, 1500.0, base.withDayOfMonth(10), 3);

        SimulationResult result = simulatorService.simulateNewLoan(req);

        assertNotNull(result);
        assertEquals("SAFE_TO_TAKE", result.getVerdict());
        assertTrue(result.getNewlyRiskyWeeks().isEmpty());
    }

    @Test
    public void testSimulationThrowsExceptionForInvalidEmi() {
        SimulationRequest req = new SimulationRequest("Zero EMI Loan", LoanType.BNPL, 0.0, LocalDate.now(), 6);
        assertThrows(IllegalArgumentException.class, () -> simulatorService.simulateNewLoan(req));
    }
}
