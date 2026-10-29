package com.emiguard.service;

import com.emiguard.model.Loan;
import com.emiguard.model.LoanType;
import com.emiguard.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class LoanService {

    private final LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAllByOrderByDueDayOfMonthAsc();
    }

    public List<Loan> getActiveLoans() {
        return loanRepository.findByActiveTrueOrderByDueDayOfMonthAsc();
    }

    public Optional<Loan> getLoanById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return loanRepository.findById(id);
    }

    @Transactional
    public Loan saveLoan(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Loan details cannot be null");
        }
        if (loan.getEmiAmount() <= 0) {
            throw new IllegalArgumentException("EMI amount must be strictly greater than zero");
        }
        if (loan.getDueDayOfMonth() < 1 || loan.getDueDayOfMonth() > 31) {
            throw new IllegalArgumentException("Due day of month must be between 1 and 31");
        }
        if (loan.getLenderName() == null || loan.getLenderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Lender or loan name cannot be blank");
        }

        if (loan.getId() == null || loan.getId().trim().isEmpty()) {
            loan.setId(UUID.randomUUID().toString());
        }
        if (loan.getStartDate() == null) {
            loan.setStartDate(LocalDate.now().minusMonths(1));
        }
        if (loan.getDurationMonths() != null && loan.getDurationMonths() > 0) {
            loan.setEndDate(loan.getStartDate().plusMonths(loan.getDurationMonths()));
        }
        if (loan.getFrequency() == null || loan.getFrequency().trim().isEmpty()) {
            loan.setFrequency("MONTHLY");
        }
        return loanRepository.save(loan);
    }

    @Transactional
    public boolean deleteLoan(String id) {
        if (id == null || !loanRepository.existsById(id)) {
            return false;
        }
        loanRepository.deleteById(id);
        return true;
    }

    public double calculateTotalMonthlyObligation() {
        return getActiveLoans().stream()
                .mapToDouble(l -> {
                    String freq = l.getFrequency() != null ? l.getFrequency().toUpperCase() : "MONTHLY";
                    if ("WEEKLY".equals(freq)) {
                        return l.getEmiAmount() * 4.0;
                    } else if ("BIWEEKLY".equals(freq)) {
                        return l.getEmiAmount() * 2.0;
                    }
                    return l.getEmiAmount();
                })
                .sum();
    }

    @Transactional
    public void clearAllLoans() {
        loanRepository.deleteAll();
    }

    /**
     * Seeds realistic sample loans illustrating the core user problem:
     * e.g., HDFC Personal Loan (₹6,500 due 5th), Apple iPhone 15 EMI (₹4,500 due 6th),
     * Honda Activa Loan (₹3,200 due 8th), Amazon Pay Later (₹2,000 due 20th),
     * Coursera MasterTrack (₹1,800 due 27th).
     * Notice how 5th, 6th, and 8th stack together to create ₹14,200 due in Week 2!
     */
    @Transactional
    public void resetToDefaultSampleLoans() {
        loanRepository.deleteAll();

        LocalDate baseDate = LocalDate.now();

        // 1. Personal Loan - due on 5th
        Loan l1 = new Loan("HDFC Personal Loan", LoanType.PERSONAL_LOAN, 6500.0, 5, baseDate.minusMonths(3), 24);
        l1.setTotalPrincipal(150000.0);
        l1.setNotes("Debt consolidation loan, auto-debit on 5th");
        loanRepository.save(l1);

        // 2. Phone Device EMI - due on 6th
        Loan l2 = new Loan("Apple iPhone 15 EMI", LoanType.PHONE_DEVICE_LOAN, 4500.0, 6, baseDate.minusMonths(2), 12);
        l2.setTotalPrincipal(54000.0);
        l2.setNotes("0% No-cost EMI via Bajaj Finserv");
        loanRepository.save(l2);

        // 3. Vehicle Loan - due on 8th
        Loan l3 = new Loan("Honda Activa Loan", LoanType.VEHICLE_LOAN, 3200.0, 8, baseDate.minusMonths(5), 36);
        l3.setTotalPrincipal(85000.0);
        l3.setNotes("Two-wheeler bank loan");
        loanRepository.save(l3);

        // 4. BNPL - due on 20th
        Loan l4 = new Loan("Amazon Pay Later", LoanType.BNPL, 2000.0, 20, baseDate.minusMonths(1), 6);
        l4.setTotalPrincipal(12000.0);
        l4.setNotes("Electronics purchase pay-later");
        loanRepository.save(l4);

        // 5. Education Loan - due on 27th
        Loan l5 = new Loan("Coursera MasterTrack", LoanType.EDUCATION_LOAN, 1800.0, 27, baseDate.minusMonths(2), 10);
        l5.setTotalPrincipal(18000.0);
        l5.setNotes("Skill certification course EMI");
        loanRepository.save(l5);
    }
}
