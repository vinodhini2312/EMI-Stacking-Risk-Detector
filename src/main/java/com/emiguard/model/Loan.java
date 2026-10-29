package com.emiguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @NotBlank(message = "Lender name cannot be blank")
    @Size(max = 255, message = "Lender name cannot exceed 255 characters")
    @Column(name = "lender_name", nullable = false)
    private String lenderName;

    @NotNull(message = "Loan type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 50)
    private LoanType loanType;

    @Positive(message = "EMI amount must be strictly greater than 0")
    @Column(name = "emi_amount", nullable = false)
    private double emiAmount;

    @Min(value = 1, message = "Due day of month must be at least 1")
    @Max(value = 31, message = "Due day of month cannot exceed 31")
    @Column(name = "due_day_of_month", nullable = false)
    private int dueDayOfMonth; // e.g., 5th of every month

    @Column(name = "start_date")
    private LocalDate startDate;

    @Min(value = 1, message = "Duration must be at least 1 month")
    @Column(name = "duration_months")
    private Integer durationMonths; // e.g., 12 months, optional

    @Column(name = "end_date")
    private LocalDate endDate; // calculated or specified

    @Column(name = "frequency", length = 20)
    private String frequency; // "MONTHLY", "WEEKLY", "BIWEEKLY"

    @PositiveOrZero(message = "Total principal cannot be negative")
    @Column(name = "total_principal")
    private Double totalPrincipal;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Loan() {
        this.id = UUID.randomUUID().toString();
        this.frequency = "MONTHLY";
        this.active = true;
    }

    public Loan(String lenderName, LoanType loanType, double emiAmount, int dueDayOfMonth, LocalDate startDate, Integer durationMonths) {
        this.id = UUID.randomUUID().toString();
        this.lenderName = lenderName;
        this.loanType = loanType;
        this.emiAmount = emiAmount;
        this.dueDayOfMonth = dueDayOfMonth;
        this.startDate = startDate;
        this.durationMonths = durationMonths;
        this.frequency = "MONTHLY";
        this.active = true;
        if (startDate != null && durationMonths != null && durationMonths > 0) {
            this.endDate = startDate.plusMonths(durationMonths);
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
        updateEndDate();
    }

    @PreUpdate
    public void onPreUpdate() {
        updateEndDate();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLenderName() {
        return lenderName;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
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

    public int getDueDayOfMonth() {
        return dueDayOfMonth;
    }

    public void setDueDayOfMonth(int dueDayOfMonth) {
        this.dueDayOfMonth = dueDayOfMonth;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        updateEndDate();
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
        updateEndDate();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getFrequency() {
        return frequency != null ? frequency : "MONTHLY";
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Double getTotalPrincipal() {
        return totalPrincipal;
    }

    public void setTotalPrincipal(Double totalPrincipal) {
        this.totalPrincipal = totalPrincipal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private void updateEndDate() {
        if (this.startDate != null && this.durationMonths != null && this.durationMonths > 0) {
            this.endDate = this.startDate.plusMonths(this.durationMonths);
        }
    }

    public Integer calculateRemainingMonths(LocalDate referenceDate) {
        if (durationMonths == null || startDate == null) {
            return null;
        }
        LocalDate currentEnd = endDate != null ? endDate : startDate.plusMonths(durationMonths);
        if (referenceDate.isAfter(currentEnd)) {
            return 0;
        }
        long months = java.time.temporal.ChronoUnit.MONTHS.between(referenceDate.withDayOfMonth(1), currentEnd.withDayOfMonth(1));
        return (int) Math.max(0, months);
    }
}
