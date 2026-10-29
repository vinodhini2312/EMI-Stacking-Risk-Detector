package com.emiguard.service;

import com.emiguard.model.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RiskCalculationEngine {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    /**
     * Slices the next N weeks (default 8) from anchorDate and computes the exact EMI vs Income risk for each week.
     */
    public List<PeriodRisk> calculateWeeklyRisks(List<Loan> loans, IncomePattern incomePattern, LocalDate anchorDate, int numberOfWeeks) {
        if (anchorDate == null) {
            anchorDate = LocalDate.now();
        }
        if (numberOfWeeks <= 0) {
            numberOfWeeks = 8;
        }

        // Align anchorDate to the nearest Monday or start of current week
        LocalDate weekStart = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<PeriodRisk> periodRisks = new ArrayList<>();

        for (int i = 0; i < numberOfWeeks; i++) {
            LocalDate currentStart = weekStart.plusWeeks(i);
            LocalDate currentEnd = currentStart.plusDays(6);

            String label = String.format("%s - %s", currentStart.format(DATE_FORMATTER), currentEnd.format(DATE_FORMATTER));

            // Calculate expected income for this week
            double expectedIncome = calculateIncomeForPeriod(incomePattern, currentStart, currentEnd);

            // Calculate total EMI due and collect contributing payments
            List<UpcomingPayment> paymentsInPeriod = getLoanPaymentsForPeriod(loans, currentStart, currentEnd, label);
            double totalEmiDue = paymentsInPeriod.stream().mapToDouble(UpcomingPayment::getAmount).sum();

            PeriodRisk periodRisk = new PeriodRisk(i + 1, currentStart, currentEnd, label, expectedIncome, totalEmiDue);
            periodRisk.setContributingPayments(paymentsInPeriod);

            periodRisks.add(periodRisk);
        }

        return periodRisks;
    }

    /**
     * Calculates expected income in a given date range based on the user's configured income pattern.
     */
    public double calculateIncomeForPeriod(IncomePattern pattern, LocalDate start, LocalDate end) {
        if (pattern == null) {
            return 0.0;
        }

        String type = pattern.getPatternType() != null ? pattern.getPatternType().toUpperCase() : "WEEKLY";

        if ("CUSTOM".equals(type)) {
            double customSum = 0.0;
            if (pattern.getCustomEntries() != null) {
                for (IncomeSchedule schedule : pattern.getCustomEntries()) {
                    if (schedule.getExpectedDate() != null &&
                            !schedule.getExpectedDate().isBefore(start) &&
                            !schedule.getExpectedDate().isAfter(end)) {
                        customSum += schedule.getAmount();
                    }
                }
            }
            return customSum;
        }

        if ("WEEKLY".equals(type)) {
            // Weekly income arrives every week (typically defaultAmount)
            return pattern.getDefaultAmount();
        }

        if ("BIWEEKLY".equals(type)) {
            // Biweekly arrives every 14 days from nextPayDate anchor
            LocalDate payAnchor = pattern.getNextPayDate() != null ? pattern.getNextPayDate() : start;
            boolean fallsInThisWeek = false;
            LocalDate cursor = payAnchor;
            while (cursor.isBefore(start)) {
                cursor = cursor.plusWeeks(2);
            }
            while (!cursor.isAfter(end)) {
                if (!cursor.isBefore(start)) {
                    fallsInThisWeek = true;
                    break;
                }
                cursor = cursor.plusWeeks(2);
            }
            return fallsInThisWeek ? pattern.getDefaultAmount() : 0.0;
        }

        if ("MONTHLY".equals(type)) {
            // Monthly income arrives once a month on the specified payDayOfMonth
            int payDay = pattern.getPayDayOfMonth() > 0 ? pattern.getPayDayOfMonth() : 1;
            boolean arrivesThisWeek = false;
            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                int maxDayInMonth = cursor.lengthOfMonth();
                int actualDay = Math.min(payDay, maxDayInMonth);
                if (cursor.getDayOfMonth() == actualDay) {
                    arrivesThisWeek = true;
                    break;
                }
                cursor = cursor.plusDays(1);
            }
            return arrivesThisWeek ? pattern.getDefaultAmount() : 0.0;
        }

        return pattern.getDefaultAmount();
    }

    /**
     * Finds all active loans with an EMI due date falling within [start, end].
     */
    public List<UpcomingPayment> getLoanPaymentsForPeriod(List<Loan> loans, LocalDate start, LocalDate end, String periodLabel) {
        List<UpcomingPayment> payments = new ArrayList<>();
        if (loans == null) {
            return payments;
        }

        for (Loan loan : loans) {
            if (!loan.isActive() || loan.getEmiAmount() <= 0) {
                continue;
            }

            LocalDate loanStart = loan.getStartDate() != null ? loan.getStartDate() : start.minusMonths(1);
            LocalDate loanEnd = loan.getEndDate();

            if (loanEnd != null && start.isAfter(loanEnd)) {
                // Loan is already completed before this period
                continue;
            }
            if (end.isBefore(loanStart)) {
                // Loan hasn't started yet
                continue;
            }

            String freq = loan.getFrequency() != null ? loan.getFrequency().toUpperCase() : "MONTHLY";

            if ("MONTHLY".equals(freq)) {
                int dueDay = loan.getDueDayOfMonth() > 0 ? loan.getDueDayOfMonth() : 5;
                LocalDate cursor = start;
                while (!cursor.isAfter(end)) {
                    int maxDayInMonth = cursor.lengthOfMonth();
                    int actualDay = Math.min(dueDay, maxDayInMonth);
                    if (cursor.getDayOfMonth() == actualDay) {
                        // Due date falls in this period
                        if ((loanEnd == null || !cursor.isAfter(loanEnd)) && !cursor.isBefore(loanStart)) {
                            payments.add(new UpcomingPayment(
                                    loan.getId(),
                                    loan.getLenderName(),
                                    loan.getLoanType(),
                                    cursor,
                                    loan.getEmiAmount(),
                                    periodLabel
                            ));
                        }
                    }
                    cursor = cursor.plusDays(1);
                }
            } else if ("WEEKLY".equals(freq)) {
                // Due once every week
                LocalDate paymentDate = start.plusDays(2); // Mid-week
                payments.add(new UpcomingPayment(
                        loan.getId(),
                        loan.getLenderName(),
                        loan.getLoanType(),
                        paymentDate,
                        loan.getEmiAmount(),
                        periodLabel
                ));
            } else {
                // Default fallback: match on day of month
                int dueDay = loan.getDueDayOfMonth() > 0 ? loan.getDueDayOfMonth() : 1;
                LocalDate cursor = start;
                while (!cursor.isAfter(end)) {
                    int maxDayInMonth = cursor.lengthOfMonth();
                    int actualDay = Math.min(dueDay, maxDayInMonth);
                    if (cursor.getDayOfMonth() == actualDay) {
                        payments.add(new UpcomingPayment(
                                loan.getId(),
                                loan.getLenderName(),
                                loan.getLoanType(),
                                cursor,
                                loan.getEmiAmount(),
                                periodLabel
                        ));
                    }
                    cursor = cursor.plusDays(1);
                }
            }
        }

        payments.sort(Comparator.comparing(UpcomingPayment::getDueDate));
        return payments;
    }

    /**
     * Extracts upcoming payments sorted chronologically across all loans for the next N days.
     */
    public List<UpcomingPayment> getAllUpcomingPayments(List<Loan> loans, LocalDate fromDate, int daysAhead) {
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        LocalDate toDate = fromDate.plusDays(daysAhead);
        List<UpcomingPayment> allPayments = new ArrayList<>();

        LocalDate weekStart = fromDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        while (!weekStart.isAfter(toDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            String label = String.format("%s - %s", weekStart.format(DATE_FORMATTER), weekEnd.format(DATE_FORMATTER));
            List<UpcomingPayment> weekly = getLoanPaymentsForPeriod(loans, weekStart, weekEnd, label);
            for (UpcomingPayment p : weekly) {
                if (!p.getDueDate().isBefore(fromDate) && !p.getDueDate().isAfter(toDate)) {
                    allPayments.add(p);
                }
            }
            weekStart = weekStart.plusWeeks(1);
        }

        allPayments.sort(Comparator.comparing(UpcomingPayment::getDueDate));
        return allPayments;
    }
}
