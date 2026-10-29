package com.emiguard.controller;

import com.emiguard.model.IncomePattern;
import com.emiguard.model.IncomeSchedule;
import com.emiguard.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/income")
@CrossOrigin(origins = "*")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public ResponseEntity<IncomePattern> getIncomePattern() {
        return ResponseEntity.ok(incomeService.getIncomePattern());
    }

    @PostMapping
    public ResponseEntity<IncomePattern> updateIncomePattern(@Valid @RequestBody IncomePattern pattern) {
        IncomePattern updated = incomeService.updateIncomePattern(pattern);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/custom-schedule")
    public ResponseEntity<IncomePattern> addCustomSchedule(@Valid @RequestBody IncomeSchedule schedule) {
        incomeService.addCustomIncomeSchedule(schedule);
        return ResponseEntity.ok(incomeService.getIncomePattern());
    }

    @PostMapping("/reset")
    public ResponseEntity<IncomePattern> resetIncomePattern() {
        incomeService.resetToDefaultPattern();
        return ResponseEntity.ok(incomeService.getIncomePattern());
    }
}
