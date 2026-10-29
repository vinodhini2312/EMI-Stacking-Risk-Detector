package com.emiguard.service;

import com.emiguard.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final LoanService loanService;
    private final IncomeService incomeService;
    private final RiskCalculationEngine riskEngine;

    public ReportService(LoanService loanService, IncomeService incomeService, RiskCalculationEngine riskEngine) {
        this.loanService = loanService;
        this.incomeService = incomeService;
        this.riskEngine = riskEngine;
    }

    public ReportData generateReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(2);
        }

        List<Loan> activeLoans = loanService.getActiveLoans();
        IncomePattern incomePattern = incomeService.getIncomePattern();

        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate);
        int numWeeks = (int) Math.max(4, Math.min(24, weeksBetween <= 0 ? 8 : weeksBetween));

        List<PeriodRisk> periods = riskEngine.calculateWeeklyRisks(activeLoans, incomePattern, startDate, numWeeks);
        List<UpcomingPayment> payments = riskEngine.getAllUpcomingPayments(activeLoans, startDate, (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate));

        double totalEmi = periods.stream().mapToDouble(PeriodRisk::getTotalEmiDue).sum();
        double totalIncome = periods.stream().mapToDouble(PeriodRisk::getExpectedIncome).sum();
        double totalShortfall = periods.stream().mapToDouble(PeriodRisk::getShortfallAmount).sum();

        int safe = 0;
        int risky = 0;
        int warning = 0;

        for (PeriodRisk pr : periods) {
            if (pr.getRiskLevel() == RiskLevel.SHORTFALL) {
                risky++;
            } else if (pr.getRiskLevel() == RiskLevel.WARNING) {
                warning++;
            } else {
                safe++;
            }
        }

        ReportData report = new ReportData();
        report.setReportStartDate(startDate);
        report.setReportEndDate(endDate);
        report.setTotalActiveLoans(activeLoans.size());
        report.setTotalEmiObligationInPeriod(totalEmi);
        report.setTotalExpectedIncomeInPeriod(totalIncome);
        report.setNetBalanceInPeriod(totalIncome - totalEmi);
        report.setTotalProjectedShortfall(totalShortfall);
        report.setTotalWeeksAnalyzed(periods.size());
        report.setSafeWeeksCount(safe);
        report.setRiskyWeeksCount(risky);
        report.setWarningWeeksCount(warning);
        report.setPeriods(periods);
        report.setPayments(payments);
        report.setActiveLoans(activeLoans);

        return report;
    }
}
