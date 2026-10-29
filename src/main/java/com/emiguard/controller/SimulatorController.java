package com.emiguard.controller;

import com.emiguard.model.SimulationAudit;
import com.emiguard.model.SimulationRequest;
import com.emiguard.model.SimulationResult;
import com.emiguard.service.SimulatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    private final SimulatorService simulatorService;

    public SimulatorController(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<SimulationResult> simulateLoan(@Valid @RequestBody SimulationRequest request) {
        SimulationResult result = simulatorService.simulateNewLoan(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SimulationAudit>> getSimulationHistory() {
        return ResponseEntity.ok(simulatorService.getRecentSimulations());
    }
}
