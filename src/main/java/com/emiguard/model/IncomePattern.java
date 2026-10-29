package com.emiguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "income_patterns")
public class IncomePattern {

    @Id
    @Column(name = "id", length = 64)
    private String id = "DEFAULT_PATTERN";

    @NotBlank(message = "Pattern type cannot be blank")
    @Column(name = "pattern_type", nullable = false, length = 50)
    private String patternType = "WEEKLY"; // "WEEKLY", "BIWEEKLY", "MONTHLY", "CUSTOM"

    @PositiveOrZero(message = "Default income amount cannot be negative")
    @Column(name = "default_amount", nullable = false)
    private double defaultAmount = 9000.0; // e.g. 9000 for weekly, or 40000 for monthly

    @Min(value = 1, message = "Pay day of month must be between 1 and 31")
    @Max(value = 31, message = "Pay day of month must be between 1 and 31")
    @Column(name = "pay_day_of_month")
    private int payDayOfMonth = 1; // e.g., 1st or 28th (for monthly)

    @Min(value = 1, message = "Pay day of week must be between 1 and 7")
    @Max(value = 7, message = "Pay day of week must be between 1 and 7")
    @Column(name = "pay_day_of_week")
    private int payDayOfWeek = 5; // 1 = Monday ... 7 = Sunday (for weekly/biweekly)

    @Column(name = "next_pay_date")
    private LocalDate nextPayDate; // Anchor date for recurring intervals

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pattern_id")
    private List<IncomeSchedule> customEntries = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public IncomePattern() {
        this.id = "DEFAULT_PATTERN";
        this.patternType = "WEEKLY";
        this.defaultAmount = 9000.0;
        this.payDayOfWeek = 5; // Friday
        this.payDayOfMonth = 1;
        this.nextPayDate = LocalDate.now();
    }

    public IncomePattern(String patternType, double defaultAmount, int payDayOfWeek, int payDayOfMonth, LocalDate nextPayDate) {
        this.id = "DEFAULT_PATTERN";
        this.patternType = patternType;
        this.defaultAmount = defaultAmount;
        this.payDayOfWeek = payDayOfWeek;
        this.payDayOfMonth = payDayOfMonth;
        this.nextPayDate = nextPayDate;
    }

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = "DEFAULT_PATTERN";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatternType() {
        return patternType != null ? patternType : "WEEKLY";
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public double getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(double defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    public int getPayDayOfMonth() {
        return payDayOfMonth;
    }

    public void setPayDayOfMonth(int payDayOfMonth) {
        this.payDayOfMonth = payDayOfMonth;
    }

    public int getPayDayOfWeek() {
        return payDayOfWeek;
    }

    public void setPayDayOfWeek(int payDayOfWeek) {
        this.payDayOfWeek = payDayOfWeek;
    }

    public LocalDate getNextPayDate() {
        return nextPayDate != null ? nextPayDate : LocalDate.now();
    }

    public void setNextPayDate(LocalDate nextPayDate) {
        this.nextPayDate = nextPayDate;
    }

    public List<IncomeSchedule> getCustomEntries() {
        if (customEntries == null) {
            customEntries = new ArrayList<>();
        }
        return customEntries;
    }

    public void setCustomEntries(List<IncomeSchedule> customEntries) {
        this.customEntries = customEntries;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
