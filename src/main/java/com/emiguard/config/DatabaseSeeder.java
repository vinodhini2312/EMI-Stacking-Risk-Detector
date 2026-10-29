package com.emiguard.config;

import com.emiguard.repository.IncomePatternRepository;
import com.emiguard.repository.LoanRepository;
import com.emiguard.service.IncomeService;
import com.emiguard.service.LoanService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final LoanRepository loanRepository;
    private final IncomePatternRepository incomePatternRepository;
    private final LoanService loanService;
    private final IncomeService incomeService;

    public DatabaseSeeder(LoanRepository loanRepository,
                          IncomePatternRepository incomePatternRepository,
                          LoanService loanService,
                          IncomeService incomeService) {
        this.loanRepository = loanRepository;
        this.incomePatternRepository = incomePatternRepository;
        this.loanService = loanService;
        this.incomeService = incomeService;
    }

    @Override
    public void run(String... args) {
        // Automatically seed sample data if tables are empty
        if (loanRepository.count() == 0) {
            loanService.resetToDefaultSampleLoans();
        }
        if (incomePatternRepository.count() == 0) {
            incomeService.resetToDefaultPattern();
        }
    }
}
