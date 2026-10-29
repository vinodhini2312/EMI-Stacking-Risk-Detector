package com.emiguard;

import com.emiguard.model.Loan;
import com.emiguard.model.LoanType;
import com.emiguard.model.SimulationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetDashboardReturnsOkAndExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActiveLoans").isNumber())
                .andExpect(jsonPath("$.totalMonthlyEmiObligation").isNumber())
                .andExpect(jsonPath("$.affordabilityHeadline").isString())
                .andExpect(jsonPath("$.weeklyPeriods").isArray());
    }

    @Test
    public void testGetLoansReturnsList() throws Exception {
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testCreateValidLoanReturnsSavedLoan() throws Exception {
        Loan loan = new Loan("Test Gadget EMI", LoanType.PHONE_DEVICE_LOAN, 2500.0, 15, LocalDate.now(), 6);
        loan.setTotalPrincipal(15000.0);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.lenderName").value("Test Gadget EMI"))
                .andExpect(jsonPath("$.emiAmount").value(2500.0));
    }

    @Test
    public void testCreateInvalidLoanReturns400BadRequest() throws Exception {
        // Loan with negative EMI and invalid day of month
        Loan invalidLoan = new Loan("", LoanType.PERSONAL_LOAN, -500.0, 35, LocalDate.now(), 0);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoan)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    public void testSimulationEndpointReturnsVerdictAndPersistsAudit() throws Exception {
        SimulationRequest req = new SimulationRequest("Test Smart Watch", LoanType.BNPL, 2000.0, LocalDate.now().plusDays(5), 4);

        mockMvc.perform(post("/api/simulator/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").isNotEmpty())
                .andExpect(jsonPath("$.verdictBadge").isNotEmpty())
                .andExpect(jsonPath("$.beforePeriods").isArray())
                .andExpect(jsonPath("$.afterPeriods").isArray());

        // Check history endpoint contains the simulation
        mockMvc.perform(get("/api/simulator/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].loanName").isNotEmpty());
    }

    @Test
    public void testSimulationWithInvalidDataReturns400() throws Exception {
        SimulationRequest invalidReq = new SimulationRequest("", LoanType.BNPL, -100.0, null, 0);

        mockMvc.perform(post("/api/simulator/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    public void testGetIncomePatternReturnsDefaultWeekly() throws Exception {
        mockMvc.perform(get("/api/income"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patternType").isNotEmpty())
                .andExpect(jsonPath("$.defaultAmount").isNumber());
    }

    @Test
    public void testGetRiskCalendarReturnsWeeks() throws Exception {
        mockMvc.perform(get("/api/risk/calendar?weeks=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    public void testGetReportsReturnsAuditReport() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWeeksAnalyzed").isNumber())
                .andExpect(jsonPath("$.periods").isArray());
    }
}
