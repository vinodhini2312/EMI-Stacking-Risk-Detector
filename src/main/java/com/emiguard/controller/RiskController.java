package com.emiguard.controller;

import com.emiguard.model.IncomePattern;
import com.emiguard.model.Loan;
import com.emiguard.model.PeriodRisk;
import com.emiguard.model.UpcomingPayment;
import com.emiguard.service.IncomeService;
import com.emiguard.service.LoanService;
import com.emiguard.service.RiskCalculationEngine;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskController {

    private final LoanService loanService;
    private final IncomeService incomeService;
    private final RiskCalculationEngine riskEngine;

    public RiskController(LoanService loanService, IncomeService incomeService, RiskCalculationEngine riskEngine) {
        this.loanService = loanService;
        this.incomeService = incomeService;
        this.riskEngine = riskEngine;
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<PeriodRisk>> getRiskCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "8") int weeks) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        IncomePattern pattern = incomeService.getIncomePattern();
        LocalDate anchor = startDate != null ? startDate : LocalDate.now();
        List<PeriodRisk> calendar = riskEngine.calculateWeeklyRisks(activeLoans, pattern, anchor, weeks);
        return ResponseEntity.ok(calendar);
    }

    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getRiskAnalysis(
            @RequestParam(defaultValue = "8") int weeks) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        IncomePattern pattern = incomeService.getIncomePattern();
        LocalDate anchor = LocalDate.now();
        List<PeriodRisk> periods = riskEngine.calculateWeeklyRisks(activeLoans, pattern, anchor, weeks);

        double totalShortfall = 0;
        int riskyCount = 0;
        int safeCount = 0;
        int warningCount = 0;

        for (PeriodRisk pr : periods) {
            if (pr.getRiskLevel() == com.emiguard.model.RiskLevel.SHORTFALL) {
                riskyCount++;
                totalShortfall += pr.getShortfallAmount();
            } else if (pr.getRiskLevel() == com.emiguard.model.RiskLevel.WARNING) {
                warningCount++;
            } else {
                safeCount++;
            }
        }

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("periods", periods);
        analysis.put("riskyCount", riskyCount);
        analysis.put("safeCount", safeCount);
        analysis.put("warningCount", warningCount);
        analysis.put("totalShortfall", totalShortfall);
        analysis.put("activeLoansCount", activeLoans.size());
        analysis.put("monthlyObligation", loanService.calculateTotalMonthlyObligation());

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/upcoming-payments")
    public ResponseEntity<List<UpcomingPayment>> getUpcomingPayments(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<Loan> activeLoans = loanService.getActiveLoans();
        List<UpcomingPayment> payments = riskEngine.getAllUpcomingPayments(activeLoans, LocalDate.now(), daysAhead);
        return ResponseEntity.ok(payments);
    }
}
