package com.emiguard.model;

import java.time.LocalDate;

public class UpcomingPayment {
    private String loanId;
    private String lenderName;
    private LoanType loanType;
    private LocalDate dueDate;
    private double amount;
    private String periodLabel;
    private boolean simulated;

    public UpcomingPayment() {
    }

    public UpcomingPayment(String loanId, String lenderName, LoanType loanType, LocalDate dueDate, double amount, String periodLabel) {
        this.loanId = loanId;
        this.lenderName = lenderName;
        this.loanType = loanType;
        this.dueDate = dueDate;
        this.amount = amount;
        this.periodLabel = periodLabel;
        this.simulated = false;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public void setSimulated(boolean simulated) {
        this.simulated = simulated;
    }
}
