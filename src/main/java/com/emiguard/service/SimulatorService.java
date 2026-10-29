package com.emiguard.service;

import com.emiguard.model.*;
import com.emiguard.repository.SimulationAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulatorService {

    private final LoanService loanService;
    private final IncomeService incomeService;
    private final RiskCalculationEngine riskEngine;
    private final SimulationAuditRepository auditRepository;

    public SimulatorService(LoanService loanService,
                            IncomeService incomeService,
                            RiskCalculationEngine riskEngine,
                            SimulationAuditRepository auditRepository) {
        this.loanService = loanService;
        this.incomeService = incomeService;
        this.riskEngine = riskEngine;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public SimulationResult simulateNewLoan(SimulationRequest request) {
        if (request == null) {
            request = new SimulationRequest("New Gadget EMI", LoanType.BNPL, 3000.0, LocalDate.now().plusDays(7), 6);
        }

        if (request.getEmiAmount() <= 0) {
            throw new IllegalArgumentException("EMI amount for simulation must be strictly greater than zero");
        }

        List<Loan> existingLoans = loanService.getActiveLoans();
        IncomePattern incomePattern = incomeService.getIncomePattern();
        LocalDate anchorDate = LocalDate.now();
        int weeksToAnalyze = 8;

        // 1. Calculate BEFORE
        List<PeriodRisk> beforePeriods = riskEngine.calculateWeeklyRisks(existingLoans, incomePattern, anchorDate, weeksToAnalyze);

        int beforeSafe = 0;
        int beforeRisky = 0;
        int beforeWarning = 0;
        for (PeriodRisk pr : beforePeriods) {
            if (pr.getRiskLevel() == RiskLevel.SHORTFALL) {
                beforeRisky++;
            } else if (pr.getRiskLevel() == RiskLevel.WARNING) {
                beforeWarning++;
            } else {
                beforeSafe++;
            }
        }

        // 2. Build Hypothetical Loan
        Loan hypotheticalLoan = new Loan();
        hypotheticalLoan.setId("SIMULATED-" + System.currentTimeMillis());
        hypotheticalLoan.setLenderName(request.getLoanName() != null && !request.getLoanName().trim().isEmpty()
                ? request.getLoanName() : "Simulated Loan");
        hypotheticalLoan.setLoanType(request.getLoanType() != null ? request.getLoanType() : LoanType.BNPL);
        hypotheticalLoan.setEmiAmount(request.getEmiAmount());

        LocalDate firstDue = request.getFirstDueDate() != null ? request.getFirstDueDate() : LocalDate.now().plusDays(7);
        hypotheticalLoan.setStartDate(firstDue);
        hypotheticalLoan.setDueDayOfMonth(firstDue.getDayOfMonth());
        hypotheticalLoan.setFrequency(request.getFrequency() != null ? request.getFrequency() : "MONTHLY");
        hypotheticalLoan.setDurationMonths(request.getDurationMonths() != null ? request.getDurationMonths() : 6);
        hypotheticalLoan.setEndDate(firstDue.plusMonths(hypotheticalLoan.getDurationMonths()));
        hypotheticalLoan.setActive(true);

        List<Loan> simulatedLoans = new ArrayList<>(existingLoans);
        simulatedLoans.add(hypotheticalLoan);

        // 3. Calculate AFTER
        List<PeriodRisk> afterPeriods = riskEngine.calculateWeeklyRisks(simulatedLoans, incomePattern, anchorDate, weeksToAnalyze);

        int afterSafe = 0;
        int afterRisky = 0;
        int afterWarning = 0;
        List<PeriodRisk> newlyRiskyWeeks = new ArrayList<>();
        double maxShortfall = 0.0;

        for (int i = 0; i < afterPeriods.size(); i++) {
            PeriodRisk afterRisk = afterPeriods.get(i);
            PeriodRisk beforeRisk = (i < beforePeriods.size()) ? beforePeriods.get(i) : null;

            if (afterRisk.getRiskLevel() == RiskLevel.SHORTFALL) {
                afterRisky++;
                if (afterRisk.getShortfallAmount() > maxShortfall) {
                    maxShortfall = afterRisk.getShortfallAmount();
                }

                // Check if this week was NOT already a shortfall before
                if (beforeRisk == null || beforeRisk.getRiskLevel() != RiskLevel.SHORTFALL) {
                    afterRisk.setNewlyRisky(true);
                    newlyRiskyWeeks.add(afterRisk);
                }
            } else if (afterRisk.getRiskLevel() == RiskLevel.WARNING) {
                afterWarning++;
            } else {
                afterSafe++;
            }
        }

        // 4. Assemble SimulationResult
        SimulationResult result = new SimulationResult();
        result.setSimulationRequest(request);
        result.setBeforeSafeWeeks(beforeSafe);
        result.setBeforeRiskyWeeks(beforeRisky);
        result.setBeforeWarningWeeks(beforeWarning);
        result.setAfterSafeWeeks(afterSafe);
        result.setAfterRiskyWeeks(afterRisky);
        result.setAfterWarningWeeks(afterWarning);

        // Financial Situation
        double weeklyIncome = incomePattern.getDefaultAmount();
        double currentWeeklyEmi = beforePeriods.isEmpty() ? 0.0 : beforePeriods.get(0).getTotalEmiDue();
        double currentWeeklyBuffer = weeklyIncome - currentWeeklyEmi;

        result.setCurrentWeeklyIncome(weeklyIncome);
        result.setCurrentWeeklyEmi(currentWeeklyEmi);
        result.setCurrentWeeklyBuffer(currentWeeklyBuffer);

        result.setNewLoanEmi(request.getEmiAmount());
        result.setAfterWeeklyEmi(currentWeeklyEmi + request.getEmiAmount());
        result.setAfterWeeklyBufferOrShortfall(weeklyIncome - (currentWeeklyEmi + request.getEmiAmount()));
        result.setMaxProjectedShortfall(maxShortfall);

        result.setBeforePeriods(beforePeriods);
        result.setAfterPeriods(afterPeriods);
        result.setNewlyRiskyWeeks(newlyRiskyWeeks);

        // Verdict determination
        if (afterRisky > 0) {
            result.setVerdict("SHORTFALL_RISK");
            result.setVerdictBadge("🔴 Shortfall Risk");
            if (!newlyRiskyWeeks.isEmpty()) {
                result.setVerdictHeadline(String.format("Adding this EMI creates %d newly risky week(s) with up to ₹%.0f shortfall!",
                        newlyRiskyWeeks.size(), maxShortfall));
                result.setDetailedAnalysis(String.format(
                        "Taking this new EMI of ₹%.0f will push your total obligations beyond your expected income in week(s): %s.",
                        request.getEmiAmount(),
                        newlyRiskyWeeks.stream().map(PeriodRisk::getLabel).reduce((a, b) -> a + ", " + b).orElse("affected period")
                ));
            } else {
                result.setVerdictHeadline(String.format("Shortfall Risk: Total EMI exceeds expected income with up to ₹%.0f deficit!", maxShortfall));
                result.setDetailedAnalysis(String.format(
                        "Taking this new EMI of ₹%.0f increases your peak weekly EMI obligation to ₹%.0f against expected income of ₹%.0f.",
                        request.getEmiAmount(), currentWeeklyEmi + request.getEmiAmount(), weeklyIncome
                ));
            }
            result.setRecommendation("❌ NOT RECOMMENDED: Avoid taking this EMI or negotiate a due date outside your peak stacking period (e.g. shift due date after the 15th).");
        } else if (afterWarning > 0) {
            result.setVerdict("WARNING");
            result.setVerdictBadge("🟠 Tight Buffer");
            result.setVerdictHeadline("Affordable but leaves a very tight cash margin (<15% buffer).");
            result.setDetailedAnalysis("The new EMI can be paid, but leaves very little emergency buffer.");
            result.setRecommendation("⚠️ CAUTION: Proceed only if you have emergency savings.");
        } else {
            result.setVerdict("SAFE_TO_TAKE");
            result.setVerdictBadge("🟢 Safe to take");
            result.setVerdictHeadline("No shortfall detected. Your expected income comfortably covers this new EMI!");
            result.setDetailedAnalysis(String.format(
                    "With expected income of ₹%.0f, your new total obligation of ₹%.0f leaves an affordable surplus in all analyzed weeks.",
                    weeklyIncome, result.getAfterWeeklyEmi()
            ));
            result.setRecommendation("✅ Safe to take: You can comfortably afford this loan without creating stacking risks.");
        }

        // 5. Persist simulation audit into SQL database
        try {
            SimulationAudit audit = new SimulationAudit(request, result);
            auditRepository.save(audit);
        } catch (Exception ignored) {
            // Log but don't break simulation if audit fails
        }

        return result;
    }

    public List<SimulationAudit> getRecentSimulations() {
        return auditRepository.findAllByOrderByCreatedAtDesc();
    }
}
