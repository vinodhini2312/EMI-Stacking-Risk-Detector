package com.emiguard.controller;

import com.emiguard.model.Loan;
import com.emiguard.model.LoanType;
import com.emiguard.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable String id) {
        return loanService.getLoanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody Loan loan) {
        Loan saved = loanService.saveLoan(loan);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable String id, @Valid @RequestBody Loan loan) {
        loan.setId(id);
        Loan updated = loanService.saveLoan(loan);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLoan(@PathVariable String id) {
        boolean deleted = loanService.deleteLoan(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", deleted ? "Loan removed successfully" : "Loan not found");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<List<Loan>> resetLoans() {
        loanService.resetToDefaultSampleLoans();
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getLoanTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        for (LoanType t : LoanType.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("code", t.name());
            item.put("displayName", t.getDisplayName());
            item.put("icon", t.getIcon());
            types.add(item);
        }
        return ResponseEntity.ok(types);
    }
}
