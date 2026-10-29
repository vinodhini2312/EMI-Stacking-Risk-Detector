package com.emiguard.model;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class SimulationRequest {

    @NotBlank(message = "Loan/purchase name is required")
    @Size(max = 255, message = "Loan name cannot exceed 255 characters")
    private String loanName;

    @NotNull(message = "Loan type is required")
    private LoanType loanType = LoanType.BNPL;

    @Positive(message = "EMI amount must be strictly greater than 0")
    private double emiAmount;

    @NotNull(message = "First due date is required")
    private LocalDate firstDueDate;

    private String frequency = "MONTHLY";

    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths = 6;

    @PositiveOrZero(message = "Total principal cannot be negative")
    private Double totalPrincipal;

    public SimulationRequest() {
    }

    public SimulationRequest(String loanName, LoanType loanType, double emiAmount, LocalDate firstDueDate, Integer durationMonths) {
        this.loanName = loanName;
        this.loanType = loanType != null ? loanType : LoanType.BNPL;
        this.emiAmount = emiAmount;
        this.firstDueDate = firstDueDate;
        this.frequency = "MONTHLY";
        this.durationMonths = durationMonths != null ? durationMonths : 6;
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
        return firstDueDate != null ? firstDueDate : LocalDate.now();
    }

    public void setFirstDueDate(LocalDate firstDueDate) {
        this.firstDueDate = firstDueDate;
    }

    public String getFrequency() {
        return frequency != null ? frequency : "MONTHLY";
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationMonths() {
        return durationMonths != null ? durationMonths : 6;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public Double getTotalPrincipal() {
        return totalPrincipal;
    }

    public void setTotalPrincipal(Double totalPrincipal) {
        this.totalPrincipal = totalPrincipal;
    }
}
