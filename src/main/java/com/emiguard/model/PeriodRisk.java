package com.emiguard.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PeriodRisk {
    private int periodIndex;
    private LocalDate startDate;
    private LocalDate endDate;
    private String label; // e.g. "Sep 28 - Oct 04" or "Week 1 (Oct 05)"
    private double expectedIncome;
    private double totalEmiDue;
    private double availableAmount; // expectedIncome - totalEmiDue
    private double shortfallAmount; // max(0, totalEmiDue - expectedIncome)
    private RiskLevel riskLevel;
    private List<UpcomingPayment> contributingPayments = new ArrayList<>();
    private String riskHeadline;
    private String explanation;
    private boolean newlyRisky; // For simulation diff

    public PeriodRisk() {
        this.riskLevel = RiskLevel.SAFE;
    }

    public PeriodRisk(int periodIndex, LocalDate startDate, LocalDate endDate, String label, double expectedIncome, double totalEmiDue) {
        this.periodIndex = periodIndex;
        this.startDate = startDate;
        this.endDate = endDate;
        this.label = label;
        this.expectedIncome = expectedIncome;
        this.totalEmiDue = totalEmiDue;
        this.availableAmount = expectedIncome - totalEmiDue;
        this.shortfallAmount = Math.max(0, totalEmiDue - expectedIncome);
        this.evaluateRiskLevel();
    }

    public void evaluateRiskLevel() {
        this.availableAmount = expectedIncome - totalEmiDue;
        if (this.availableAmount < 0) {
            this.riskLevel = RiskLevel.SHORTFALL;
            this.shortfallAmount = Math.abs(this.availableAmount);
            this.riskHeadline = "Shortfall Risk";
            this.explanation = String.format("₹%.0f EMI due exceeds expected income of ₹%.0f by ₹%.0f shortfall.",
                    totalEmiDue, expectedIncome, shortfallAmount);
        } else if (expectedIncome > 0 && (availableAmount / expectedIncome < 0.15 || availableAmount < 1500)) {
            this.riskLevel = RiskLevel.WARNING;
            this.shortfallAmount = 0;
            this.riskHeadline = "Tight Margin";
            this.explanation = String.format("₹%.0f EMI leaves only ₹%.0f buffer after ₹%.0f income.",
                    totalEmiDue, availableAmount, expectedIncome);
        } else {
            this.riskLevel = RiskLevel.SAFE;
            this.shortfallAmount = 0;
            this.riskHeadline = "Safe";
            this.explanation = String.format("Comfortable buffer of ₹%.0f remaining after paying ₹%.0f EMI.",
                    availableAmount, totalEmiDue);
        }
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getExpectedIncome() {
        return expectedIncome;
    }

    public void setExpectedIncome(double expectedIncome) {
        this.expectedIncome = expectedIncome;
        evaluateRiskLevel();
    }

    public double getTotalEmiDue() {
        return totalEmiDue;
    }

    public void setTotalEmiDue(double totalEmiDue) {
        this.totalEmiDue = totalEmiDue;
        evaluateRiskLevel();
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public double getShortfallAmount() {
        return shortfallAmount;
    }

    public void setShortfallAmount(double shortfallAmount) {
        this.shortfallAmount = shortfallAmount;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<UpcomingPayment> getContributingPayments() {
        if (contributingPayments == null) {
            contributingPayments = new ArrayList<>();
        }
        return contributingPayments;
    }

    public void setContributingPayments(List<UpcomingPayment> contributingPayments) {
        this.contributingPayments = contributingPayments;
    }

    public String getRiskHeadline() {
        return riskHeadline;
    }

    public void setRiskHeadline(String riskHeadline) {
        this.riskHeadline = riskHeadline;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isNewlyRisky() {
        return newlyRisky;
    }

    public void setNewlyRisky(boolean newlyRisky) {
        this.newlyRisky = newlyRisky;
    }
}
