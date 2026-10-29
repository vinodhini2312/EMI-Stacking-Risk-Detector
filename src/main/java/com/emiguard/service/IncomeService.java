package com.emiguard.service;

import com.emiguard.model.IncomePattern;
import com.emiguard.model.IncomeSchedule;
import com.emiguard.repository.IncomePatternRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncomeService {

    private static final String DEFAULT_PATTERN_ID = "DEFAULT_PATTERN";
    private final IncomePatternRepository incomePatternRepository;

    public IncomeService(IncomePatternRepository incomePatternRepository) {
        this.incomePatternRepository = incomePatternRepository;
    }

    public IncomePattern getIncomePattern() {
        return incomePatternRepository.findById(DEFAULT_PATTERN_ID)
                .orElseGet(this::resetToDefaultPattern);
    }

    @Transactional
    public IncomePattern updateIncomePattern(IncomePattern newPattern) {
        if (newPattern == null) {
            throw new IllegalArgumentException("Income pattern details cannot be null");
        }
        if (newPattern.getDefaultAmount() < 0) {
            throw new IllegalArgumentException("Income amount cannot be negative");
        }
        newPattern.setId(DEFAULT_PATTERN_ID);
        return incomePatternRepository.save(newPattern);
    }

    @Transactional
    public void addCustomIncomeSchedule(IncomeSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Custom income schedule cannot be null");
        }
        if (schedule.getAmount() <= 0) {
            throw new IllegalArgumentException("Custom income amount must be greater than zero");
        }
        IncomePattern pattern = getIncomePattern();
        if (pattern.getCustomEntries() == null) {
            pattern.setCustomEntries(new ArrayList<>());
        }
        pattern.getCustomEntries().add(schedule);
        incomePatternRepository.save(pattern);
    }

    public double getEstimatedMonthlyIncome() {
        IncomePattern currentPattern = getIncomePattern();
        if (currentPattern == null) {
            return 36000.0;
        }
        String type = currentPattern.getPatternType() != null ? currentPattern.getPatternType().toUpperCase() : "WEEKLY";
        switch (type) {
            case "WEEKLY":
                return currentPattern.getDefaultAmount() * 4.0;
            case "BIWEEKLY":
                return currentPattern.getDefaultAmount() * 2.0;
            case "MONTHLY":
                return currentPattern.getDefaultAmount();
            case "CUSTOM":
                if (currentPattern.getCustomEntries() != null && !currentPattern.getCustomEntries().isEmpty()) {
                    return currentPattern.getCustomEntries().stream().mapToDouble(IncomeSchedule::getAmount).sum();
                }
                return currentPattern.getDefaultAmount();
            default:
                return currentPattern.getDefaultAmount() * 4.0;
        }
    }

    @Transactional
    public IncomePattern resetToDefaultPattern() {
        IncomePattern defaultPattern = new IncomePattern();
        defaultPattern.setId(DEFAULT_PATTERN_ID);
        defaultPattern.setPatternType("WEEKLY");
        defaultPattern.setDefaultAmount(9000.0); // Exactly matching prompt example: ₹9,000 weekly income
        defaultPattern.setPayDayOfWeek(5); // Friday
        defaultPattern.setPayDayOfMonth(1);
        defaultPattern.setNextPayDate(LocalDate.now());

        List<IncomeSchedule> custom = new ArrayList<>();
        LocalDate today = LocalDate.now();
        custom.add(new IncomeSchedule("Weekly Stipend / Salary", 9000.0, today.plusDays(2), true));
        custom.add(new IncomeSchedule("Weekend Freelance", 3000.0, today.plusDays(12), false));
        defaultPattern.setCustomEntries(custom);

        return incomePatternRepository.save(defaultPattern);
    }
}
