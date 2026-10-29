package com.emiguard.controller;

import com.emiguard.service.IncomeService;
import com.emiguard.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    private final LoanService loanService;
    private final IncomeService incomeService;

    public SystemController(LoanService loanService, IncomeService incomeService) {
        this.loanService = loanService;
        this.incomeService = incomeService;
    }

    @PostMapping("/reset-sample-data")
    public ResponseEntity<Map<String, Object>> resetSampleData() {
        loanService.resetToDefaultSampleLoans();
        incomeService.resetToDefaultPattern();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Sample loan stack and income schedule successfully reset!");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/clear-all-data")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        loanService.clearAllLoans();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "All loans cleared.");
        return ResponseEntity.ok(resp);
    }
}
