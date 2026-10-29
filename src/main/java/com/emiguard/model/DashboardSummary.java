package com.emiguard.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardSummary {
    private int totalActiveLoans;
    private double totalMonthlyEmiObligation;
    private double expectedMonthlyIncome;
    private double currentWeekEmi;
    private double currentWeekIncome;
    private double currentWeekAvailable;
    private RiskLevel currentWeekRiskLevel;
    
    private int riskyWeeksCount;
    private int safeWeeksCount;
    private int warningWeeksCount;
    private double totalProjectedShortfall;
    
    private boolean canAffordUpcoming;
    private String affordabilityHeadline;
    private String affordabilitySubtitle;
    
    private List<UpcomingPayment> upcomingPayments = new ArrayList<>();
    private List<PeriodRisk> weeklyPeriods = new ArrayList<>();

    public DashboardSummary() {
    }

    public int getTotalActiveLoans() {
        return totalActiveLoans;
    }

    public void setTotalActiveLoans(int totalActiveLoans) {
        this.totalActiveLoans = totalActiveLoans;
    }

    public double getTotalMonthlyEmiObligation() {
        return totalMonthlyEmiObligation;
    }

    public void setTotalMonthlyEmiObligation(double totalMonthlyEmiObligation) {
        this.totalMonthlyEmiObligation = totalMonthlyEmiObligation;
    }

    public double getExpectedMonthlyIncome() {
        return expectedMonthlyIncome;
    }

    public void setExpectedMonthlyIncome(double expectedMonthlyIncome) {
        this.expectedMonthlyIncome = expectedMonthlyIncome;
    }

    public double getCurrentWeekEmi() {
        return currentWeekEmi;
    }

    public void setCurrentWeekEmi(double currentWeekEmi) {
        this.currentWeekEmi = currentWeekEmi;
    }

    public double getCurrentWeekIncome() {
        return currentWeekIncome;
    }

    public void setCurrentWeekIncome(double currentWeekIncome) {
        this.currentWeekIncome = currentWeekIncome;
    }

    public double getCurrentWeekAvailable() {
        return currentWeekAvailable;
    }

    public void setCurrentWeekAvailable(double currentWeekAvailable) {
        this.currentWeekAvailable = currentWeekAvailable;
    }

    public RiskLevel getCurrentWeekRiskLevel() {
        return currentWeekRiskLevel;
    }

    public void setCurrentWeekRiskLevel(RiskLevel currentWeekRiskLevel) {
        this.currentWeekRiskLevel = currentWeekRiskLevel;
    }

    public int getRiskyWeeksCount() {
        return riskyWeeksCount;
    }

    public void setRiskyWeeksCount(int riskyWeeksCount) {
        this.riskyWeeksCount = riskyWeeksCount;
    }

    public int getSafeWeeksCount() {
        return safeWeeksCount;
    }

    public void setSafeWeeksCount(int safeWeeksCount) {
        this.safeWeeksCount = safeWeeksCount;
    }

    public int getWarningWeeksCount() {
        return warningWeeksCount;
    }

    public void setWarningWeeksCount(int warningWeeksCount) {
        this.warningWeeksCount = warningWeeksCount;
    }

    public double getTotalProjectedShortfall() {
        return totalProjectedShortfall;
    }

    public void setTotalProjectedShortfall(double totalProjectedShortfall) {
        this.totalProjectedShortfall = totalProjectedShortfall;
    }

    public boolean isCanAffordUpcoming() {
        return canAffordUpcoming;
    }

    public void setCanAffordUpcoming(boolean canAffordUpcoming) {
        this.canAffordUpcoming = canAffordUpcoming;
    }

    public String getAffordabilityHeadline() {
        return affordabilityHeadline;
    }

    public void setAffordabilityHeadline(String affordabilityHeadline) {
        this.affordabilityHeadline = affordabilityHeadline;
    }

    public String getAffordabilitySubtitle() {
        return affordabilitySubtitle;
    }

    public void setAffordabilitySubtitle(String affordabilitySubtitle) {
        this.affordabilitySubtitle = affordabilitySubtitle;
    }

    public List<UpcomingPayment> getUpcomingPayments() {
        return upcomingPayments;
    }

    public void setUpcomingPayments(List<UpcomingPayment> upcomingPayments) {
        this.upcomingPayments = upcomingPayments;
    }

    public List<PeriodRisk> getWeeklyPeriods() {
        return weeklyPeriods;
    }

    public void setWeeklyPeriods(List<PeriodRisk> weeklyPeriods) {
        this.weeklyPeriods = weeklyPeriods;
    }
}
