/**
 * EMI Stacking Risk Detector - REST API Client
 */

const API_BASE = '/api';

const api = {
  // Dashboard
  async getDashboard() {
    const res = await fetch(`${API_BASE}/dashboard`);
    if (!res.ok) throw new Error('Failed to fetch dashboard data');
    return res.json();
  },

  // Loans
  async getLoans() {
    const res = await fetch(`${API_BASE}/loans`);
    if (!res.ok) throw new Error('Failed to fetch loans');
    return res.json();
  },

  async getLoanTypes() {
    const res = await fetch(`${API_BASE}/loans/types`);
    if (!res.ok) throw new Error('Failed to fetch loan types');
    return res.json();
  },

  async createLoan(loanData) {
    const res = await fetch(`${API_BASE}/loans`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loanData)
    });
    if (!res.ok) throw new Error('Failed to create loan');
    return res.json();
  },

  async updateLoan(id, loanData) {
    const res = await fetch(`${API_BASE}/loans/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loanData)
    });
    if (!res.ok) throw new Error('Failed to update loan');
    return res.json();
  },

  async deleteLoan(id) {
    const res = await fetch(`${API_BASE}/loans/${id}`, {
      method: 'DELETE'
    });
    if (!res.ok) throw new Error('Failed to delete loan');
    return res.json();
  },

  async resetLoans() {
    const res = await fetch(`${API_BASE}/loans/reset`, {
      method: 'POST'
    });
    if (!res.ok) throw new Error('Failed to reset loans');
    return res.json();
  },

  // Income
  async getIncomePattern() {
    const res = await fetch(`${API_BASE}/income`);
    if (!res.ok) throw new Error('Failed to fetch income pattern');
    return res.json();
  },

  async updateIncomePattern(patternData) {
    const res = await fetch(`${API_BASE}/income`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(patternData)
    });
    if (!res.ok) throw new Error('Failed to update income pattern');
    return res.json();
  },

  async addCustomIncome(customData) {
    const res = await fetch(`${API_BASE}/income/custom-schedule`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customData)
    });
    if (!res.ok) throw new Error('Failed to add custom income');
    return res.json();
  },

  // Risk Calendar & Analysis
  async getRiskCalendar(weeks = 8) {
    const res = await fetch(`${API_BASE}/risk/calendar?weeks=${weeks}`);
    if (!res.ok) throw new Error('Failed to fetch risk calendar');
    return res.json();
  },

  async getRiskAnalysis(weeks = 8) {
    const res = await fetch(`${API_BASE}/risk/analysis?weeks=${weeks}`);
    if (!res.ok) throw new Error('Failed to fetch risk analysis');
    return res.json();
  },

  async getUpcomingPayments(days = 30) {
    const res = await fetch(`${API_BASE}/risk/upcoming-payments?daysAhead=${days}`);
    if (!res.ok) throw new Error('Failed to fetch upcoming payments');
    return res.json();
  },

  // Simulator
  async simulateLoan(simRequest) {
    const res = await fetch(`${API_BASE}/simulator/simulate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(simRequest)
    });
    if (!res.ok) throw new Error('Failed to run simulation');
    return res.json();
  },

  // Reports
  async getReport(startDate, endDate) {
    let url = `${API_BASE}/reports`;
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (params.toString()) url += `?${params.toString()}`;

    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to generate report');
    return res.json();
  },

  // System
  async resetAllSampleData() {
    const res = await fetch(`${API_BASE}/system/reset-sample-data`, {
      method: 'POST'
    });
    if (!res.ok) throw new Error('Failed to reset sample data');
    return res.json();
  },

  async clearAllData() {
    const res = await fetch(`${API_BASE}/system/clear-all-data`, {
      method: 'POST'
    });
    if (!res.ok) throw new Error('Failed to clear all data');
    return res.json();
  }
};
