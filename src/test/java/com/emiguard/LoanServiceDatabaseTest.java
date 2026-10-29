package com.emiguard;

import com.emiguard.model.Loan;
import com.emiguard.model.LoanType;
import com.emiguard.repository.LoanRepository;
import com.emiguard.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LoanServiceDatabaseTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    public void setUp() {
        loanRepository.deleteAll();
    }

    @Test
    public void testLoanDatabaseCrudOperations() {
        // Create & Insert
        Loan loan = new Loan("Axis Bank Loan", LoanType.PERSONAL_LOAN, 5000.0, 15, LocalDate.now().minusMonths(2), 24);
        loan.setTotalPrincipal(100000.0);
        Loan saved = loanService.saveLoan(loan);

        assertNotNull(saved.getId());
        assertEquals("Axis Bank Loan", saved.getLenderName());
        assertTrue(loanRepository.existsById(saved.getId()));

        // Read
        Optional<Loan> fetched = loanService.getLoanById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals(5000.0, fetched.get().getEmiAmount());

        // Update
        saved.setEmiAmount(5500.0);
        saved.setNotes("Updated interest rate");
        Loan updated = loanService.saveLoan(saved);
        assertEquals(5500.0, updated.getEmiAmount());

        // Delete
        boolean deleted = loanService.deleteLoan(saved.getId());
        assertTrue(deleted);
        assertFalse(loanRepository.existsById(saved.getId()));
    }

    @Test
    public void testCalculateMonthlyObligationWithFrequencies() {
        // Monthly Loan: 4000
        Loan l1 = new Loan("Monthly Loan", LoanType.PERSONAL_LOAN, 4000.0, 5, LocalDate.now(), 12);
        l1.setFrequency("MONTHLY");
        loanService.saveLoan(l1);

        // Weekly Loan: 1000 -> 4000/mo
        Loan l2 = new Loan("Weekly Loan", LoanType.BNPL, 1000.0, 10, LocalDate.now(), 6);
        l2.setFrequency("WEEKLY");
        loanService.saveLoan(l2);

        // Biweekly Loan: 2000 -> 4000/mo
        Loan l3 = new Loan("Biweekly Loan", LoanType.VEHICLE_LOAN, 2000.0, 12, LocalDate.now(), 12);
        l3.setFrequency("BIWEEKLY");
        loanService.saveLoan(l3);

        double totalObligation = loanService.calculateTotalMonthlyObligation();
        assertEquals(12000.0, totalObligation, 0.01);
    }

    @Test
    public void testResetDefaultSampleLoansSeedsFiveLoans() {
        loanService.resetToDefaultSampleLoans();
        List<Loan> allLoans = loanService.getAllLoans();
        assertEquals(5, allLoans.size());
        assertTrue(loanService.calculateTotalMonthlyObligation() > 0);
    }

    @Test
    public void testInvalidLoanCreationThrowsValidationException() {
        Loan invalidLoan = new Loan("", LoanType.PERSONAL_LOAN, -100.0, 45, LocalDate.now(), 0);
        assertThrows(IllegalArgumentException.class, () -> loanService.saveLoan(invalidLoan));
    }
}
