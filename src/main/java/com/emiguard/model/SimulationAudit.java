package com.emiguard.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "simulation_audits")
public class SimulationAudit {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "loan_name", length = 255)
    private String loanName;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", length = 50)
    private LoanType loanType;

    @Column(name = "emi_amount", nullable = false)
    private double emiAmount;

    @Column(name = "first_due_date")
    private LocalDate firstDueDate;

    @Column(name = "frequency", length = 20)
    private String frequency;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "verdict", length = 50, nullable = false)
    private String verdict; // "SHORTFALL_RISK", "WARNING", "SAFE_TO_TAKE"

    @Column(name = "verdict_badge", length = 100)
    private String verdictBadge;

    @Column(name = "max_projected_shortfall")
    private double maxProjectedShortfall;

    @Column(name = "before_risky_weeks")
    private int beforeRiskyWeeks;

    @Column(name = "after_risky_weeks")
    private int afterRiskyWeeks;

    @Column(name = "current_weekly_income")
    private double currentWeeklyIncome;

    @Column(name = "current_weekly_emi")
    private double currentWeeklyEmi;

    @Column(name = "after_weekly_emi")
    private double afterWeeklyEmi;

    @Column(name = "after_weekly_buffer_or_shortfall")
    private double afterWeeklyBufferOrShortfall;

    @Column(name = "newly_risky_weeks_count")
    private int newlyRiskyWeeksCount;

    public SimulationAudit() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public SimulationAudit(SimulationRequest request, SimulationResult result) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        if (request != null) {
            this.loanName = request.getLoanName();
            this.loanType = request.getLoanType();
            this.emiAmount = request.getEmiAmount();
            this.firstDueDate = request.getFirstDueDate();
            this.frequency = request.getFrequency();
            this.durationMonths = request.getDurationMonths();
        }
        if (result != null) {
            this.verdict = result.getVerdict();
            this.verdictBadge = result.getVerdictBadge();
            this.maxProjectedShortfall = result.getMaxProjectedShortfall();
            this.beforeRiskyWeeks = result.getBeforeRiskyWeeks();
            this.afterRiskyWeeks = result.getAfterRiskyWeeks();
            this.currentWeeklyIncome = result.getCurrentWeeklyIncome();
            this.currentWeeklyEmi = result.getCurrentWeeklyEmi();
            this.afterWeeklyEmi = result.getAfterWeeklyEmi();
            this.afterWeeklyBufferOrShortfall = result.getAfterWeeklyBufferOrShortfall();
            this.newlyRiskyWeeksCount = result.getNewlyRiskyWeeks() != null ? result.getNewlyRiskyWeeks().size() : 0;
        }
    }

    @PrePersist
    public void onPrePersist() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public LocalDate getFirstDueDate() {
        return firstDueDate;
    }

    public void setFirstDueDate(LocalDate firstDueDate) {
        this.firstDueDate = firstDueDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
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

    public double getMaxProjectedShortfall() {
        return maxProjectedShortfall;
    }

    public void setMaxProjectedShortfall(double maxProjectedShortfall) {
        this.maxProjectedShortfall = maxProjectedShortfall;
    }

    public int getBeforeRiskyWeeks() {
        return beforeRiskyWeeks;
    }

    public void setBeforeRiskyWeeks(int beforeRiskyWeeks) {
        this.beforeRiskyWeeks = beforeRiskyWeeks;
    }

    public int getAfterRiskyWeeks() {
        return afterRiskyWeeks;
    }

    public void setAfterRiskyWeeks(int afterRiskyWeeks) {
        this.afterRiskyWeeks = afterRiskyWeeks;
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

    public int getNewlyRiskyWeeksCount() {
        return newlyRiskyWeeksCount;
    }

    public void setNewlyRiskyWeeksCount(int newlyRiskyWeeksCount) {
        this.newlyRiskyWeeksCount = newlyRiskyWeeksCount;
    }
}
