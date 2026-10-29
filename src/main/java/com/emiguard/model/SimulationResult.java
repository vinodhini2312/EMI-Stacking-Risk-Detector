package com.emiguard.model;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {
    private SimulationRequest simulationRequest;
    
    // Counts Before vs After
    private int beforeSafeWeeks;
    private int beforeRiskyWeeks;
    private int beforeWarningWeeks;
    
    private int afterSafeWeeks;
    private int afterRiskyWeeks;
    private int afterWarningWeeks;

    // Financial summaries
    private double currentWeeklyIncome;
    private double currentWeeklyEmi;
    private double currentWeeklyBuffer;
    
    private double newLoanEmi;
    private double afterWeeklyEmi;
    private double afterWeeklyBufferOrShortfall;
    private double maxProjectedShortfall;

    // Risk verdict
    private String verdict; // "SAFE_TO_TAKE" or "SHORTFALL_RISK"
    private String verdictBadge; // "🟢 Safe to take" or "🔴 Shortfall Risk"
    private String verdictHeadline;
    private String detailedAnalysis;
    private String recommendation;

    // Timeline breakdowns
    private List<PeriodRisk> beforePeriods = new ArrayList<>();
    private List<PeriodRisk> afterPeriods = new ArrayList<>();
    private List<PeriodRisk> newlyRiskyWeeks = new ArrayList<>();

    public SimulationResult() {
    }

    public SimulationRequest getSimulationRequest() {
        return simulationRequest;
    }

    public void setSimulationRequest(SimulationRequest simulationRequest) {
        this.simulationRequest = simulationRequest;
    }

    public int getBeforeSafeWeeks() {
        return beforeSafeWeeks;
    }

    public void setBeforeSafeWeeks(int beforeSafeWeeks) {
        this.beforeSafeWeeks = beforeSafeWeeks;
    }

    public int getBeforeRiskyWeeks() {
        return beforeRiskyWeeks;
    }

    public void setBeforeRiskyWeeks(int beforeRiskyWeeks) {
        this.beforeRiskyWeeks = beforeRiskyWeeks;
    }

    public int getBeforeWarningWeeks() {
        return beforeWarningWeeks;
    }

    public void setBeforeWarningWeeks(int beforeWarningWeeks) {
        this.beforeWarningWeeks = beforeWarningWeeks;
    }

    public int getAfterSafeWeeks() {
        return afterSafeWeeks;
    }

    public void setAfterSafeWeeks(int afterSafeWeeks) {
        this.afterSafeWeeks = afterSafeWeeks;
    }

    public int getAfterRiskyWeeks() {
        return afterRiskyWeeks;
    }

    public void setAfterRiskyWeeks(int afterRiskyWeeks) {
        this.afterRiskyWeeks = afterRiskyWeeks;
    }

    public int getAfterWarningWeeks() {
        return afterWarningWeeks;
    }

    public void setAfterWarningWeeks(int afterWarningWeeks) {
        this.afterWarningWeeks = afterWarningWeeks;
    }

    public double getCurrentWeeklyIncome() {
        return currentWeeklyIncome;
    }

    public void setCurrentWeeklyIncome(double currentWeeklyIncome) {
        this.currentWeeklyIncome = currentWeeklyIncome;
    }

    public double getCurrentWeeklyEmi() {
        return currentWeeklyEmi;
    }

    public void setCurrentWeeklyEmi(double currentWeeklyEmi) {
        this.currentWeeklyEmi = currentWeeklyEmi;
    }

    public double getCurrentWeeklyBuffer() {
        return currentWeeklyBuffer;
    }

    public void setCurrentWeeklyBuffer(double currentWeeklyBuffer) {
        this.currentWeeklyBuffer = currentWeeklyBuffer;
    }

    public double getNewLoanEmi() {
        return newLoanEmi;
    }

    public void setNewLoanEmi(double newLoanEmi) {
        this.newLoanEmi = newLoanEmi;
    }

    public double getAfterWeeklyEmi() {
        return afterWeeklyEmi;
    }

    public void setAfterWeeklyEmi(double afterWeeklyEmi) {
        this.afterWeeklyEmi = afterWeeklyEmi;
    }

    public double getAfterWeeklyBufferOrShortfall() {
        return afterWeeklyBufferOrShortfall;
    }

    public void setAfterWeeklyBufferOrShortfall(double afterWeeklyBufferOrShortfall) {
        this.afterWeeklyBufferOrShortfall = afterWeeklyBufferOrShortfall;
    }

    public double getMaxProjectedShortfall() {
        return maxProjectedShortfall;
    }

    public void setMaxProjectedShortfall(double maxProjectedShortfall) {
        this.maxProjectedShortfall = maxProjectedShortfall;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getVerdictBadge() {
        return verdictBadge;
    }

    public void setVerdictBadge(String verdictBadge) {
        this.verdictBadge = verdictBadge;
    }

    public String getVerdictHeadline() {
        return verdictHeadline;
    }

    public void setVerdictHeadline(String verdictHeadline) {
        this.verdictHeadline = verdictHeadline;
    }

    public String getDetailedAnalysis() {
        return detailedAnalysis;
    }

    public void setDetailedAnalysis(String detailedAnalysis) {
        this.detailedAnalysis = detailedAnalysis;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public List<PeriodRisk> getBeforePeriods() {
        return beforePeriods;
    }

    public void setBeforePeriods(List<PeriodRisk> beforePeriods) {
        this.beforePeriods = beforePeriods;
    }

    public List<PeriodRisk> getAfterPeriods() {
        return afterPeriods;
    }

    public void setAfterPeriods(List<PeriodRisk> afterPeriods) {
        this.afterPeriods = afterPeriods;
    }

    public List<PeriodRisk> getNewlyRiskyWeeks() {
        return newlyRiskyWeeks;
    }

    public void setNewlyRiskyWeeks(List<PeriodRisk> newlyRiskyWeeks) {
        this.newlyRiskyWeeks = newlyRiskyWeeks;
    }

    public void setNewlyRisky(List<PeriodRisk> newlyRiskyWeeks) {
        this.newlyRiskyWeeks = newlyRiskyWeeks;
    }
}
