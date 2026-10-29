package com.emiguard.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportData {
    private LocalDate reportStartDate;
    private LocalDate reportEndDate;
    private int totalActiveLoans;
    private double totalEmiObligationInPeriod;
    private double totalExpectedIncomeInPeriod;
    private double netBalanceInPeriod;
    private double totalProjectedShortfall;
    private int totalWeeksAnalyzed;
    private int safeWeeksCount;
    private int riskyWeeksCount;
    private int warningWeeksCount;
    private List<PeriodRisk> periods = new ArrayList<>();
    private List<UpcomingPayment> payments = new ArrayList<>();
    private List<Loan> activeLoans = new ArrayList<>();

    public ReportData() {
    }

    public LocalDate getReportStartDate() {
        return reportStartDate;
    }

    public void setReportStartDate(LocalDate reportStartDate) {
        this.reportStartDate = reportStartDate;
    }

    public LocalDate getReportEndDate() {
        return reportEndDate;
    }

    public void setReportEndDate(LocalDate reportEndDate) {
        this.reportEndDate = reportEndDate;
    }

    public int getTotalActiveLoans() {
        return totalActiveLoans;
    }

    public void setTotalActiveLoans(int totalActiveLoans) {
        this.totalActiveLoans = totalActiveLoans;
    }

    public double getTotalEmiObligationInPeriod() {
        return totalEmiObligationInPeriod;
    }

    public void setTotalEmiObligationInPeriod(double totalEmiObligationInPeriod) {
        this.totalEmiObligationInPeriod = totalEmiObligationInPeriod;
    }

    public double getTotalExpectedIncomeInPeriod() {
        return totalExpectedIncomeInPeriod;
    }

    public void setTotalExpectedIncomeInPeriod(double totalExpectedIncomeInPeriod) {
        this.totalExpectedIncomeInPeriod = totalExpectedIncomeInPeriod;
    }

    public double getNetBalanceInPeriod() {
        return netBalanceInPeriod;
    }

    public void setNetBalanceInPeriod(double netBalanceInPeriod) {
        this.netBalanceInPeriod = netBalanceInPeriod;
    }

    public double getTotalProjectedShortfall() {
        return totalProjectedShortfall;
    }

    public void setTotalProjectedShortfall(double totalProjectedShortfall) {
        this.totalProjectedShortfall = totalProjectedShortfall;
    }

    public int getTotalWeeksAnalyzed() {
        return totalWeeksAnalyzed;
    }

    public void setTotalWeeksAnalyzed(int totalWeeksAnalyzed) {
        this.totalWeeksAnalyzed = totalWeeksAnalyzed;
    }

    public int getSafeWeeksCount() {
        return safeWeeksCount;
    }

    public void setSafeWeeksCount(int safeWeeksCount) {
        this.safeWeeksCount = safeWeeksCount;
    }

    public int getRiskyWeeksCount() {
        return riskyWeeksCount;
    }

    public void setRiskyWeeksCount(int riskyWeeksCount) {
        this.riskyWeeksCount = riskyWeeksCount;
    }

    public int getWarningWeeksCount() {
        return warningWeeksCount;
    }

    public void setWarningWeeksCount(int warningWeeksCount) {
        this.warningWeeksCount = warningWeeksCount;
    }

    public List<PeriodRisk> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PeriodRisk> periods) {
        this.periods = periods;
    }

    public List<UpcomingPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<UpcomingPayment> payments) {
        this.payments = payments;
    }

    public List<Loan> getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(List<Loan> activeLoans) {
        this.activeLoans = activeLoans;
    }
}
