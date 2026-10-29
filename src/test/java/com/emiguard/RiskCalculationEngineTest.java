package com.emiguard;

import com.emiguard.model.*;
import com.emiguard.service.RiskCalculationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RiskCalculationEngineTest {

    private RiskCalculationEngine riskEngine;
    private IncomePattern weeklyIncomePattern;

    @BeforeEach
    public void setUp() {
        riskEngine = new RiskCalculationEngine();
        weeklyIncomePattern = new IncomePattern("WEEKLY", 9000.0, 5, 1, LocalDate.now());
    }

    @Test
    public void testShortfallRiskDetectionWhenEmisStackAboveIncome() {
        LocalDate baseMonday = LocalDate.of(2026, 10, 5); // 2026-10-05 is a Monday
        List<Loan> loans = new ArrayList<>();

        // 3 loans due on Oct 5th, 6th, 8th (all within Monday Oct 5 to Sunday Oct 11)
        loans.add(new Loan("Personal Loan", LoanType.PERSONAL_LOAN, 6500.0, 5, baseMonday.minusMonths(1), 12));
        loans.add(new Loan("Phone EMI", LoanType.PHONE_DEVICE_LOAN, 4500.0, 6, baseMonday.minusMonths(1), 6));
        loans.add(new Loan("Vehicle Loan", LoanType.VEHICLE_LOAN, 3200.0, 8, baseMonday.minusMonths(1), 24));

        List<PeriodRisk> risks = riskEngine.calculateWeeklyRisks(loans, weeklyIncomePattern, baseMonday, 4);

        assertNotNull(risks);
        assertFalse(risks.isEmpty());

        PeriodRisk stackedWeek = risks.get(0);
        assertEquals(9000.0, stackedWeek.getExpectedIncome(), 0.01);
        assertEquals(14200.0, stackedWeek.getTotalEmiDue(), 0.01);
        assertEquals(-5200.0, stackedWeek.getAvailableAmount(), 0.01);
        assertEquals(5200.0, stackedWeek.getShortfallAmount(), 0.01);
        assertEquals(RiskLevel.SHORTFALL, stackedWeek.getRiskLevel());
    }

    @Test
    public void testApplicantWithNoExistingLoans() {
        LocalDate baseMonday = LocalDate.of(2026, 10, 5);
        List<Loan> emptyLoans = new ArrayList<>();

        List<PeriodRisk> risks = riskEngine.calculateWeeklyRisks(emptyLoans, weeklyIncomePattern, baseMonday, 4);

        assertNotNull(risks);
        assertEquals(4, risks.size());
        for (PeriodRisk pr : risks) {
            assertEquals(0.0, pr.getTotalEmiDue(), 0.01);
            assertEquals(9000.0, pr.getExpectedIncome(), 0.01);
            assertEquals(9000.0, pr.getAvailableAmount(), 0.01);
            assertEquals(0.0, pr.getShortfallAmount(), 0.01);
            assertEquals(RiskLevel.SAFE, pr.getRiskLevel());
        }
    }

    @Test
    public void testSafeWeekWhenIncomeExceedsEmi() {
        LocalDate now = LocalDate.now();
        List<Loan> loans = new ArrayList<>();
        LocalDate base = LocalDate.of(now.getYear(), now.getMonth(), 1);
        loans.add(new Loan("Small Gadget EMI", LoanType.BNPL, 2000.0, 20, base.minusMonths(1), 6));

        List<PeriodRisk> risks = riskEngine.calculateWeeklyRisks(loans, weeklyIncomePattern, base, 4);

        PeriodRisk firstWeek = risks.get(0);
        assertTrue(firstWeek.getAvailableAmount() >= 0);
        assertNotEquals(RiskLevel.SHORTFALL, firstWeek.getRiskLevel());
    }

    @Test
    public void testZeroIncomeWithExistingEmiResultsInShortfall() {
        LocalDate baseMonday = LocalDate.of(2026, 10, 5);
        IncomePattern zeroIncome = new IncomePattern("WEEKLY", 0.0, 5, 1, baseMonday);
        List<Loan> loans = new ArrayList<>();
        loans.add(new Loan("Personal Loan", LoanType.PERSONAL_LOAN, 5000.0, 5, baseMonday.minusMonths(1), 12));

        List<PeriodRisk> risks = riskEngine.calculateWeeklyRisks(loans, zeroIncome, baseMonday, 1);

        assertEquals(1, risks.size());
        PeriodRisk pr = risks.get(0);
        assertEquals(0.0, pr.getExpectedIncome(), 0.01);
        assertEquals(5000.0, pr.getTotalEmiDue(), 0.01);
        assertEquals(-5000.0, pr.getAvailableAmount(), 0.01);
        assertEquals(5000.0, pr.getShortfallAmount(), 0.01);
        assertEquals(RiskLevel.SHORTFALL, pr.getRiskLevel());
    }

    @Test
    public void testTightBufferTriggersWarning() {
        // Income is 9000, EMI is 8000 -> Available 1000 (< 1500 minimum buffer threshold)
        PeriodRisk pr = new PeriodRisk(1, LocalDate.now(), LocalDate.now().plusDays(6), "Week 1", 9000.0, 8000.0);
        assertEquals(RiskLevel.WARNING, pr.getRiskLevel());
        assertEquals("Tight Margin", pr.getRiskHeadline());
        assertEquals(0.0, pr.getShortfallAmount(), 0.01);
    }
}
