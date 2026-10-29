package com.emiguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "income_schedules")
public class IncomeSchedule {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @NotBlank(message = "Income source name cannot be blank")
    @Size(max = 255, message = "Income source name cannot exceed 255 characters")
    @Column(name = "source_name", nullable = false)
    private String sourceName; // e.g. "Primary Salary", "Freelance Gig", "Rental Income"

    @Positive(message = "Income amount must be greater than zero")
    @Column(name = "amount", nullable = false)
    private double amount;

    @NotNull(message = "Expected income date is required")
    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @Column(name = "recurring", nullable = false)
    private boolean recurring = true;

    public IncomeSchedule() {
        this.id = UUID.randomUUID().toString();
        this.recurring = true;
    }

    public IncomeSchedule(String sourceName, double amount, LocalDate expectedDate, boolean recurring) {
        this.id = UUID.randomUUID().toString();
        this.sourceName = sourceName;
        this.amount = amount;
        this.expectedDate = expectedDate;
        this.recurring = recurring;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
}
