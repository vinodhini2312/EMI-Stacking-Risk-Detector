/**
 * EMI Stacking Risk Detector - Hybrid REST API & Client-Side Engine
 * 
 * Supports both:
 * 1. Connected Mode: Connects to Spring Boot REST API & PostgreSQL / H2
 * 2. Static / GitHub Pages Mode: Autonomous client-side calculation engine
 */

const API_BASE = '/api';

// Realistic Seed Data
const DEFAULT_SAMPLE_LOANS = [
  {
    id: "LOAN-1",
    lenderName: "HDFC Personal Loan",
    loanType: "PERSONAL_LOAN",
    emiAmount: 6500.0,
    dueDayOfMonth: 5,
    startDate: "2026-07-05",
    durationMonths: 24,
    frequency: "MONTHLY",
    totalPrincipal: 150000.0,
    notes: "Debt consolidation loan, auto-debit on 5th",
    active: true
  },
  {
    id: "LOAN-2",
    lenderName: "Apple iPhone 15 EMI",
    loanType: "PHONE_DEVICE_LOAN",
    emiAmount: 4500.0,
    dueDayOfMonth: 6,
    startDate: "2026-08-06",
    durationMonths: 12,
    frequency: "MONTHLY",
    totalPrincipal: 54000.0,
    notes: "0% No-cost EMI via Bajaj Finserv",
    active: true
  },
  {
    id: "LOAN-3",
    lenderName: "Honda Activa Loan",
    loanType: "VEHICLE_LOAN",
    emiAmount: 3200.0,
    dueDayOfMonth: 8,
    startDate: "2026-05-08",
    durationMonths: 36,
    frequency: "MONTHLY",
    totalPrincipal: 85000.0,
    notes: "Two-wheeler bank loan",
    active: true
  },
  {
    id: "LOAN-4",
    lenderName: "Amazon Pay Later",
    loanType: "BNPL",
    emiAmount: 2000.0,
    dueDayOfMonth: 20,
    startDate: "2026-09-20",
    durationMonths: 6,
    frequency: "MONTHLY",
    totalPrincipal: 12000.0,
    notes: "Electronics purchase pay-later",
    active: true
  },
  {
    id: "LOAN-5",
    lenderName: "Coursera MasterTrack",
    loanType: "EDUCATION_LOAN",
    emiAmount: 1800.0,
    dueDayOfMonth: 27,
    startDate: "2026-08-27",
    durationMonths: 10,
    frequency: "MONTHLY",
    totalPrincipal: 18000.0,
    notes: "Skill certification course EMI",
    active: true
  }
];

const DEFAULT_INCOME_PATTERN = {
  patternType: "WEEKLY",
  defaultAmount: 9000.0,
  payDayOfWeek: 5,
  payDayOfMonth: 1,
  nextPayDate: new Date().toISOString().split('T')[0],
  customEntries: [
    {
      id: "INC-1",
      sourceName: "Weekly Stipend / Salary",
      amount: 9000.0,
      expectedDate: new Date(Date.now() + 2 * 86400000).toISOString().split('T')[0],
      recurring: true
    },
    {
      id: "INC-2",
      sourceName: "Weekend Freelance",
      amount: 3000.0,
      expectedDate: new Date(Date.now() + 12 * 86400000).toISOString().split('T')[0],
      recurring: false
    }
  ]
};

// Client-side local engine state
const LocalEngine = {
  getLoans() {
    const raw = localStorage.getItem('emiguard_loans');
    if (!raw) {
      localStorage.setItem('emiguard_loans', JSON.stringify(DEFAULT_SAMPLE_LOANS));
      return JSON.parse(JSON.stringify(DEFAULT_SAMPLE_LOANS));
    }
    return JSON.parse(raw);
  },

  saveLoans(loans) {
    localStorage.setItem('emiguard_loans', JSON.stringify(loans));
  },

  getIncome() {
    const raw = localStorage.getItem('emiguard_income');
    if (!raw) {
      localStorage.setItem('emiguard_income', JSON.stringify(DEFAULT_INCOME_PATTERN));
      return JSON.parse(JSON.stringify(DEFAULT_INCOME_PATTERN));
    }
    return JSON.parse(raw);
  },

  saveIncome(income) {
    localStorage.setItem('emiguard_income', JSON.stringify(income));
  },

  resetAll() {
    localStorage.setItem('emiguard_loans', JSON.stringify(DEFAULT_SAMPLE_LOANS));
    localStorage.setItem('emiguard_income', JSON.stringify(DEFAULT_INCOME_PATTERN));
    localStorage.removeItem('emiguard_audits');
  },

  getAudits() {
    const raw = localStorage.getItem('emiguard_audits');
    return raw ? JSON.parse(raw) : [];
  },

  saveAudit(audit) {
    const audits = this.getAudits();
    audits.unshift(audit);
    localStorage.setItem('emiguard_audits', JSON.stringify(audits));
  },

  computeWeeklyPeriods(loans, incomePattern, numWeeks = 8) {
    const periods = [];
    const now = new Date();
    // Monday of current week
    const currentDay = now.getDay();
    const distanceToMonday = (currentDay + 6) % 7;
    const startMonday = new Date(now);
    startMonday.setDate(now.getDate() - distanceToMonday);
    startMonday.setHours(0, 0, 0, 0);

    const activeLoans = (loans || []).filter(l => l.active && l.emiAmount > 0);
    const weeklyIncome = incomePattern ? (incomePattern.defaultAmount || 9000.0) : 9000.0;

    for (let i = 0; i < numWeeks; i++) {
      const pStart = new Date(startMonday);
      pStart.setDate(startMonday.getDate() + (i * 7));
      const pEnd = new Date(pStart);
      pEnd.setDate(pStart.getDate() + 6);

      const label = `${pStart.toLocaleString('default', { month: 'short' })} ${String(pStart.getDate()).padStart(2, '0')} - ${pEnd.toLocaleString('default', { month: 'short' })} ${String(pEnd.getDate()).padStart(2, '0')}`;

      // Calculate payments in this range
      const contributingPayments = [];
      let totalEmiDue = 0;

      for (const loan of activeLoans) {
        const dueDay = loan.dueDayOfMonth || 5;
        // Check days in this week
        for (let d = new Date(pStart); d <= pEnd; d.setDate(d.getDate() + 1)) {
          if (d.getDate() === dueDay) {
            contributingPayments.push({
              loanId: loan.id,
              lenderName: loan.lenderName,
              loanType: loan.loanType,
              dueDate: d.toISOString().split('T')[0],
              amount: loan.emiAmount,
              periodLabel: label
            });
            totalEmiDue += loan.emiAmount;
          }
        }
      }

      const expectedIncome = weeklyIncome;
      const availableAmount = expectedIncome - totalEmiDue;
      const shortfallAmount = Math.max(0, totalEmiDue - expectedIncome);

      let riskLevel = "SAFE";
      let riskHeadline = "Safe";
      let explanation = `Comfortable buffer of ₹${availableAmount.toFixed(0)} remaining.`;

      if (availableAmount < 0) {
        riskLevel = "SHORTFALL";
        riskHeadline = "Shortfall Risk";
        explanation = `₹${totalEmiDue.toFixed(0)} EMI due exceeds expected income of ₹${expectedIncome.toFixed(0)} by ₹${shortfallAmount.toFixed(0)} shortfall.`;
      } else if (expectedIncome > 0 && (availableAmount / expectedIncome < 0.15 || availableAmount < 1500)) {
        riskLevel = "WARNING";
        riskHeadline = "Tight Margin";
        explanation = `₹${totalEmiDue.toFixed(0)} EMI leaves only ₹${availableAmount.toFixed(0)} buffer after ₹${expectedIncome.toFixed(0)} income.`;
      }

      periods.push({
        periodIndex: i + 1,
        startDate: pStart.toISOString().split('T')[0],
        endDate: pEnd.toISOString().split('T')[0],
        label,
        expectedIncome,
        totalEmiDue,
        availableAmount,
        shortfallAmount,
        riskLevel,
        riskHeadline,
        explanation,
        contributingPayments,
        newlyRisky: false
      });
    }

    return periods;
  }
};

const api = {
  useLocalFallback: false,

  async request(endpoint, options = {}) {
    // If running statically on GitHub Pages or file protocol, use local engine
    if (window.location.protocol === 'file:' || window.location.hostname.endsWith('github.io') || this.useLocalFallback) {
      return this.handleLocalRequest(endpoint, options);
    }

    try {
      const res = await fetch(`${API_BASE}${endpoint}`, options);
      if (!res.ok) {
        if (res.status === 404) {
          // Fallback to local engine
          this.useLocalFallback = true;
          return this.handleLocalRequest(endpoint, options);
        }
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `Request failed with status ${res.status}`);
      }
      return res.json();
    } catch (err) {
      console.warn("Backend not reachable, activating autonomous client-side engine:", err.message);
      this.useLocalFallback = true;
      return this.handleLocalRequest(endpoint, options);
    }
  },

  handleLocalRequest(endpoint, options = {}) {
    const method = options.method || 'GET';
    const body = options.body ? JSON.parse(options.body) : null;

    if (endpoint === '/dashboard') {
      const loans = LocalEngine.getLoans().filter(l => l.active);
      const income = LocalEngine.getIncome();
      const periods = LocalEngine.computeWeeklyPeriods(loans, income, 8);
      
      let riskyWeeks = 0, safeWeeks = 0, warningWeeks = 0, totalShortfall = 0;
      periods.forEach(p => {
        if (p.riskLevel === 'SHORTFALL') {
          riskyWeeks++;
          totalShortfall += p.shortfallAmount;
        } else if (p.riskLevel === 'WARNING') {
          warningWeeks++;
        } else {
          safeWeeks++;
        }
      });

      const currentWeek = periods[0] || {};
      const totalMonthlyEmi = loans.reduce((acc, l) => acc + (l.frequency === 'WEEKLY' ? l.emiAmount * 4 : l.frequency === 'BIWEEKLY' ? l.emiAmount * 2 : l.emiAmount), 0);
      const expectedMonthlyIncome = income.defaultAmount * 4.0;

      let canAfford = riskyWeeks === 0;
      let headline = canAfford ? (warningWeeks > 0 ? "🟠 TIGHT CASH BUFFER" : "🟢 ALL UPCOMING EMIS ARE AFFORDABLE") : "🔴 SHORTFALL RISK DETECTED";
      let subtitle = canAfford ? (warningWeeks > 0 ? "You can cover all EMIs, but your cash margin will be slim (<15% buffer)." : "Your expected income comfortably covers all combined EMI obligations.") : `You will be short by ₹${totalShortfall.toFixed(0)} across ${riskyWeeks} upcoming week(s). Action required to prevent default!`;

      const upcomingPayments = [];
      periods.forEach(p => p.contributingPayments.forEach(cp => upcomingPayments.push(cp)));

      return {
        totalActiveLoans: loans.length,
        totalMonthlyEmiObligation: totalMonthlyEmi,
        expectedMonthlyIncome,
        currentWeekEmi: currentWeek.totalEmiDue || 0,
        currentWeekIncome: currentWeek.expectedIncome || 0,
        currentWeekAvailable: currentWeek.availableAmount || 0,
        currentWeekRiskLevel: currentWeek.riskLevel || 'SAFE',
        riskyWeeksCount: riskyWeeks,
        safeWeeksCount: safeWeeks,
        warningWeeksCount: warningWeeks,
        totalProjectedShortfall: totalShortfall,
        canAffordUpcoming: canAfford,
        affordabilityHeadline: headline,
        affordabilitySubtitle: subtitle,
        upcomingPayments,
        weeklyPeriods: periods
      };
    }

    if (endpoint === '/loans' && method === 'GET') {
      return LocalEngine.getLoans();
    }

    if (endpoint === '/loans/types') {
      return [
        { code: "PERSONAL_LOAN", displayName: "Personal Loan", icon: "🏦" },
        { code: "PHONE_DEVICE_LOAN", displayName: "Phone / Device Financing", icon: "📱" },
        { code: "VEHICLE_LOAN", displayName: "Vehicle Loan", icon: "🛵" },
        { code: "BNPL", displayName: "Buy Now Pay Later (BNPL)", icon: "🛍️" },
        { code: "EDUCATION_LOAN", displayName: "Education / Course Loan", icon: "🎓" },
        { code: "OTHER", displayName: "Other Consumer Debt", icon: "💳" }
      ];
    }

    if (endpoint === '/loans' && method === 'POST') {
      const loans = LocalEngine.getLoans();
      body.id = "LOAN-" + Date.now();
      body.active = true;
      loans.push(body);
      LocalEngine.saveLoans(loans);
      return body;
    }

    if (endpoint.startsWith('/loans/') && method === 'PUT') {
      const id = endpoint.split('/')[2];
      const loans = LocalEngine.getLoans();
      const idx = loans.findIndex(l => l.id === id);
      if (idx !== -1) {
        body.id = id;
        loans[idx] = body;
        LocalEngine.saveLoans(loans);
        return body;
      }
      return body;
    }

    if (endpoint.startsWith('/loans/') && method === 'DELETE') {
      const id = endpoint.split('/')[2];
      const loans = LocalEngine.getLoans().filter(l => l.id !== id);
      LocalEngine.saveLoans(loans);
      return { success: true, message: "Loan removed successfully" };
    }

    if (endpoint === '/loans/reset') {
      LocalEngine.saveLoans(DEFAULT_SAMPLE_LOANS);
      return DEFAULT_SAMPLE_LOANS;
    }

    if (endpoint === '/income' && method === 'GET') {
      return LocalEngine.getIncome();
    }

    if (endpoint === '/income' && method === 'POST') {
      LocalEngine.saveIncome(body);
      return body;
    }

    if (endpoint === '/income/reset') {
      LocalEngine.saveIncome(DEFAULT_INCOME_PATTERN);
      return DEFAULT_INCOME_PATTERN;
    }

    if (endpoint.startsWith('/risk/calendar')) {
      const loans = LocalEngine.getLoans();
      const income = LocalEngine.getIncome();
      return LocalEngine.computeWeeklyPeriods(loans, income, 8);
    }

    if (endpoint.startsWith('/risk/analysis')) {
      const loans = LocalEngine.getLoans();
      const income = LocalEngine.getIncome();
      const periods = LocalEngine.computeWeeklyPeriods(loans, income, 8);
      let riskyCount = 0, safeCount = 0, warningCount = 0, totalShortfall = 0;
      periods.forEach(p => {
        if (p.riskLevel === 'SHORTFALL') {
          riskyCount++;
          totalShortfall += p.shortfallAmount;
        } else if (p.riskLevel === 'WARNING') {
          warningCount++;
        } else {
          safeCount++;
        }
      });
      return {
        periods,
        riskyCount,
        safeCount,
        warningCount,
        totalShortfall,
        activeLoansCount: loans.filter(l => l.active).length,
        monthlyObligation: loans.reduce((acc, l) => acc + l.emiAmount, 0)
      };
    }

    if (endpoint.startsWith('/risk/upcoming-payments')) {
      const loans = LocalEngine.getLoans();
      const income = LocalEngine.getIncome();
      const periods = LocalEngine.computeWeeklyPeriods(loans, income, 8);
      const payments = [];
      periods.forEach(p => p.contributingPayments.forEach(cp => payments.push(cp)));
      return payments;
    }

    if (endpoint === '/simulator/simulate' && method === 'POST') {
      const existingLoans = LocalEngine.getLoans().filter(l => l.active);
      const income = LocalEngine.getIncome();

      const beforePeriods = LocalEngine.computeWeeklyPeriods(existingLoans, income, 8);
      let beforeSafe = 0, beforeRisky = 0, beforeWarning = 0;
      beforePeriods.forEach(p => {
        if (p.riskLevel === 'SHORTFALL') beforeRisky++;
        else if (p.riskLevel === 'WARNING') beforeWarning++;
        else beforeSafe++;
      });

      const firstDue = new Date(body.firstDueDate || (Date.now() + 7 * 86400000));
      const simulatedLoan = {
        id: "SIM-" + Date.now(),
        lenderName: body.loanName || "Simulated Loan",
        loanType: body.loanType || "BNPL",
        emiAmount: body.emiAmount || 3000,
        dueDayOfMonth: firstDue.getDate(),
        frequency: body.frequency || "MONTHLY",
        durationMonths: body.durationMonths || 6,
        active: true
      };

      const simulatedLoans = [...existingLoans, simulatedLoan];
      const afterPeriods = LocalEngine.computeWeeklyPeriods(simulatedLoans, income, 8);

      let afterSafe = 0, afterRisky = 0, afterWarning = 0, maxShortfall = 0;
      const newlyRiskyWeeks = [];

      afterPeriods.forEach((afterRisk, idx) => {
        const beforeRisk = beforePeriods[idx];
        if (afterRisk.riskLevel === 'SHORTFALL') {
          afterRisky++;
          if (afterRisk.shortfallAmount > maxShortfall) maxShortfall = afterRisk.shortfallAmount;
          if (!beforeRisk || beforeRisk.riskLevel !== 'SHORTFALL') {
            afterRisk.newlyRisky = true;
            newlyRiskyWeeks.push(afterRisk);
          }
        } else if (afterRisk.riskLevel === 'WARNING') {
          afterWarning++;
        } else {
          afterSafe++;
        }
      });

      const weeklyIncome = income.defaultAmount;
      const currentWeeklyEmi = beforePeriods[0] ? beforePeriods[0].totalEmiDue : 0;
      const currentWeeklyBuffer = weeklyIncome - currentWeeklyEmi;

      let verdict = "SAFE_TO_TAKE", verdictBadge = "🟢 Safe to take", headline = "No shortfall detected.", recommendation = "✅ Safe to take";
      if (afterRisky > 0) {
        verdict = "SHORTFALL_RISK";
        verdictBadge = "🔴 Shortfall Risk";
        headline = `Adding this EMI creates ${newlyRiskyWeeks.length} newly risky week(s) with up to ₹${maxShortfall.toFixed(0)} shortfall!`;
        recommendation = "❌ NOT RECOMMENDED: Avoid taking this EMI or negotiate a due date outside your peak stacking period.";
      } else if (afterWarning > 0) {
        verdict = "WARNING";
        verdictBadge = "🟠 Tight Buffer";
        headline = "Affordable but leaves a very tight cash margin (<15% buffer).";
        recommendation = "⚠️ CAUTION: Proceed only if you have emergency savings.";
      }

      const result = {
        simulationRequest: body,
        beforeSafeWeeks: beforeSafe,
        beforeRiskyWeeks: beforeRisky,
        beforeWarningWeeks: beforeWarning,
        afterSafeWeeks: afterSafe,
        afterRiskyWeeks: afterRisky,
        afterWarningWeeks: afterWarning,
        currentWeeklyIncome: weeklyIncome,
        currentWeeklyEmi,
        currentWeeklyBuffer,
        newLoanEmi: body.emiAmount,
        afterWeeklyEmi: currentWeeklyEmi + body.emiAmount,
        afterWeeklyBufferOrShortfall: weeklyIncome - (currentWeeklyEmi + body.emiAmount),
        maxProjectedShortfall: maxShortfall,
        beforePeriods,
        afterPeriods,
        newlyRiskyWeeks,
        verdict,
        verdictBadge,
        verdictHeadline: headline,
        recommendation
      };

      LocalEngine.saveAudit({
        id: "AUDIT-" + Date.now(),
        createdAt: new Date().toISOString(),
        loanName: body.loanName,
        emiAmount: body.emiAmount,
        verdict,
        verdictBadge,
        maxProjectedShortfall: maxShortfall
      });

      return result;
    }

    if (endpoint === '/simulator/history') {
      return LocalEngine.getAudits();
    }

    if (endpoint.startsWith('/reports')) {
      const loans = LocalEngine.getLoans();
      const income = LocalEngine.getIncome();
      const periods = LocalEngine.computeWeeklyPeriods(loans, income, 8);
      const totalEmi = periods.reduce((acc, p) => acc + p.totalEmiDue, 0);
      const totalIncome = periods.reduce((acc, p) => acc + p.expectedIncome, 0);
      const totalShortfall = periods.reduce((acc, p) => acc + p.shortfallAmount, 0);

      const payments = [];
      periods.forEach(p => p.contributingPayments.forEach(cp => payments.push(cp)));

      return {
        totalActiveLoans: loans.filter(l => l.active).length,
        totalEmiObligationInPeriod: totalEmi,
        totalExpectedIncomeInPeriod: totalIncome,
        netBalanceInPeriod: totalIncome - totalEmi,
        totalProjectedShortfall: totalShortfall,
        totalWeeksAnalyzed: periods.length,
        safeWeeksCount: periods.filter(p => p.riskLevel === 'SAFE').length,
        riskyWeeksCount: periods.filter(p => p.riskLevel === 'SHORTFALL').length,
        warningWeeksCount: periods.filter(p => p.riskLevel === 'WARNING').length,
        periods,
        payments,
        activeLoans: loans.filter(l => l.active)
      };
    }

    if (endpoint === '/system/reset-sample-data') {
      LocalEngine.resetAll();
      return { success: true, message: "Sample loan stack and income schedule successfully reset!" };
    }

    if (endpoint === '/system/clear-all-data') {
      LocalEngine.saveLoans([]);
      return { success: true, message: "All loans cleared." };
    }

    return {};
  },

  // API Methods
  async getDashboard() { return this.request('/dashboard'); },
  async getLoans() { return this.request('/loans'); },
  async getLoanTypes() { return this.request('/loans/types'); },
  async createLoan(loanData) {
    return this.request('/loans', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loanData)
    });
  },
  async updateLoan(id, loanData) {
    return this.request(`/loans/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loanData)
    });
  },
  async deleteLoan(id) {
    return this.request(`/loans/${id}`, { method: 'DELETE' });
  },
  async resetLoans() {
    return this.request('/loans/reset', { method: 'POST' });
  },
  async getIncomePattern() { return this.request('/income'); },
  async updateIncomePattern(patternData) {
    return this.request('/income', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(patternData)
    });
  },
  async addCustomIncome(customData) {
    return this.request('/income/custom-schedule', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customData)
    });
  },
  async resetIncomePattern() {
    return this.request('/income/reset', { method: 'POST' });
  },
  async getRiskCalendar(weeks = 8) {
    return this.request(`/risk/calendar?weeks=${weeks}`);
  },
  async getRiskAnalysis(weeks = 8) {
    return this.request(`/risk/analysis?weeks=${weeks}`);
  },
  async getUpcomingPayments(days = 30) {
    return this.request(`/risk/upcoming-payments?daysAhead=${days}`);
  },
  async simulateLoan(simRequest) {
    return this.request('/simulator/simulate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(simRequest)
    });
  },
  async getSimulationHistory() {
    return this.request('/simulator/history');
  },
  async getReport(startDate, endDate) {
    let url = `/reports`;
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (params.toString()) url += `?${params.toString()}`;
    return this.request(url);
  },
  async resetAllSampleData() {
    return this.request('/system/reset-sample-data', { method: 'POST' });
  },
  async clearAllData() {
    return this.request('/system/clear-all-data', { method: 'POST' });
  }
};
