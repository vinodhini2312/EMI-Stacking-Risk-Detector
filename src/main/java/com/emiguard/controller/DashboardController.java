package com.emiguard.controller;

import com.emiguard.model.*;
import com.emiguard.service.IncomeService;
import com.emiguard.service.LoanService;
import com.emiguard.service.RiskCalculationEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final LoanService loanService;
    private final IncomeService incomeService;
    private final RiskCalculationEngine riskEngine;

    public DashboardController(LoanService loanService, IncomeService incomeService, RiskCalculationEngine riskEngine) {
        this.loanService = loanService;
        this.incomeService = incomeService;
        this.riskEngine = riskEngine;
    }

    @GetMapping
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        List<Loan> activeLoans = loanService.getActiveLoans();
        IncomePattern incomePattern = incomeService.getIncomePattern();
        LocalDate today = LocalDate.now();

        List<PeriodRisk> weeklyPeriods = riskEngine.calculateWeeklyRisks(activeLoans, incomePattern, today, 8);
        List<UpcomingPayment> upcomingPayments = riskEngine.getAllUpcomingPayments(activeLoans, today, 30);

        int riskyWeeks = 0;
        int safeWeeks = 0;
        int warningWeeks = 0;
        double totalShortfall = 0.0;

        for (PeriodRisk pr : weeklyPeriods) {
            if (pr.getRiskLevel() == RiskLevel.SHORTFALL) {
                riskyWeeks++;
                totalShortfall += pr.getShortfallAmount();
            } else if (pr.getRiskLevel() == RiskLevel.WARNING) {
                warningWeeks++;
            } else {
                safeWeeks++;
            }
        }

        PeriodRisk currentWeek = !weeklyPeriods.isEmpty() ? weeklyPeriods.get(0) : new PeriodRisk();

        DashboardSummary summary = new DashboardSummary();
        summary.setTotalActiveLoans(activeLoans.size());
        summary.setTotalMonthlyEmiObligation(loanService.calculateTotalMonthlyObligation());
        summary.setExpectedMonthlyIncome(incomeService.getEstimatedMonthlyIncome());
        summary.setCurrentWeekEmi(currentWeek.getTotalEmiDue());
        summary.setCurrentWeekIncome(currentWeek.getExpectedIncome());
        summary.setCurrentWeekAvailable(currentWeek.getAvailableAmount());
        summary.setCurrentWeekRiskLevel(currentWeek.getRiskLevel());

        summary.setRiskyWeeksCount(riskyWeeks);
        summary.setSafeWeeksCount(safeWeeks);
        summary.setWarningWeeksCount(warningWeeks);
        summary.setTotalProjectedShortfall(totalShortfall);

        // Core prompt question answer: "Can I afford all my upcoming EMI payments based on my expected income?"
        if (riskyWeeks > 0) {
            summary.setCanAffordUpcoming(false);
            summary.setAffordabilityHeadline("🔴 SHORTFALL RISK DETECTED");
            summary.setAffordabilitySubtitle(String.format("You will be short by ₹%.0f across %d upcoming week(s). Action required to prevent default!",
                    totalShortfall, riskyWeeks));
        } else if (warningWeeks > 0) {
            summary.setCanAffordUpcoming(true);
            summary.setAffordabilityHeadline("🟠 TIGHT CASH BUFFER");
            summary.setAffordabilitySubtitle("You can cover all EMIs, but your cash margin will be slim (<15% buffer).");
        } else {
            summary.setCanAffordUpcoming(true);
            summary.setAffordabilityHeadline("🟢 ALL UPCOMING EMIS ARE AFFORDABLE");
            summary.setAffordabilitySubtitle("Your expected income comfortably covers all combined EMI obligations.");
        }

        summary.setUpcomingPayments(upcomingPayments);
        summary.setWeeklyPeriods(weeklyPeriods);

        return ResponseEntity.ok(summary);
    }
}
