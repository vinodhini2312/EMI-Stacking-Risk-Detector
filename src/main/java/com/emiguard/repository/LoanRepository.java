package com.emiguard.repository;

import com.emiguard.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findByActiveTrueOrderByDueDayOfMonthAsc();
    List<Loan> findAllByOrderByDueDayOfMonthAsc();
}
