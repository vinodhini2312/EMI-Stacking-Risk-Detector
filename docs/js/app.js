/**
 * EMI Stacking Risk Detector - Application Controller
 */

const app = {
  activeView: 'dashboard-view',
  currentDashboard: null,
  currentLoans: [],
  currentIncomePattern: null,
  currentSimResult: null,
  pendingDeleteId: null,
  currencySymbol: '₹',
  theme: 'dark',

  init() {
    this.initTheme();
    this.bindEvents();
    this.initDates();
    this.loadAll();
  },

  initTheme() {
    const savedTheme = localStorage.getItem('emi_theme') || 'dark';
    this.setTheme(savedTheme, false);
  },

  setTheme(theme, save = true) {
    this.theme = theme;
    if (save) {
      localStorage.setItem('emi_theme', theme);
    }
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.style.colorScheme = theme;

    const iconElem = document.getElementById('themeToggleIcon');
    if (iconElem) {
      iconElem.textContent = theme === 'light' ? '🌙' : '☀️';
    }

    const themeSelect = document.getElementById('themeSelector');
    if (themeSelect && themeSelect.value !== theme) {
      themeSelect.value = theme;
    }

    // Refresh active charts with updated theme palette if rendered
    if (this.currentDashboard && this.currentDashboard.weeklyPeriods) {
      chartEngine.renderDashboardChart('dashboardRiskChart', this.currentDashboard.weeklyPeriods);
    }
    if (this.currentSimResult) {
      chartEngine.renderSimulationComparisonChart('simComparisonChart', this.currentSimResult);
    }
    if (this.currentLoans && this.currentLoans.length > 0 && document.getElementById('loanDistributionChart')) {
      chartEngine.renderLoanDistributionChart('loanDistributionChart', this.currentLoans);
    }
  },

  toggleTheme() {
    const nextTheme = this.theme === 'light' ? 'dark' : 'light';
    this.setTheme(nextTheme, true);
    this.showToast(`Switched to ${nextTheme === 'light' ? 'Light' : 'Dark'} mode`, 'info');
  },

  bindEvents() {
    // Navigation items
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', () => {
        const viewId = item.getAttribute('data-view');
        if (viewId) {
          this.navigateTo(viewId);
        }
      });
    });

    // Mobile sidebar toggle
    const mobileBtn = document.getElementById('mobileMenuBtn');
    if (mobileBtn) {
      mobileBtn.addEventListener('click', () => {
        document.getElementById('sidebar').classList.toggle('mobile-open');
      });
    }

    // Currency selector
    const currSelect = document.getElementById('currencySelector');
    if (currSelect) {
      currSelect.addEventListener('change', (e) => {
        this.currencySymbol = e.target.value;
        this.loadDashboard();
        this.loadLoans();
      });
    }
  },

  initDates() {
    const todayStr = new Date().toISOString().split('T')[0];
    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);
    const nextWeekStr = nextWeek.toISOString().split('T')[0];

    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 2);
    const nextMonthStr = nextMonth.toISOString().split('T')[0];

    // Defaults for dates
    if (document.getElementById('simFirstDueDate')) {
      document.getElementById('simFirstDueDate').value = nextWeekStr;
    }
    if (document.getElementById('loanStartDate')) {
      document.getElementById('loanStartDate').value = todayStr;
    }
    if (document.getElementById('incomeNextPayDate')) {
      document.getElementById('incomeNextPayDate').value = todayStr;
    }
    if (document.getElementById('customIncomeDate')) {
      document.getElementById('customIncomeDate').value = todayStr;
    }
    if (document.getElementById('reportStartDate')) {
      document.getElementById('reportStartDate').value = todayStr;
    }
    if (document.getElementById('reportEndDate')) {
      document.getElementById('reportEndDate').value = nextMonthStr;
    }
  },

  navigateTo(viewId) {
    this.activeView = viewId;

    // Update nav active classes
    document.querySelectorAll('.nav-item').forEach(item => {
      if (item.getAttribute('data-view') === viewId) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });

    // Update panels
    document.querySelectorAll('.view-panel').forEach(panel => {
      if (panel.id === viewId) {
        panel.classList.add('active');
      } else {
        panel.classList.remove('active');
      }
    });

    // Update header title
    const titles = {
      'dashboard-view': 'Financial Risk Dashboard',
      'simulator-view': 'New EMI Risk Simulator',
      'calendar-view': 'EMI Risk Calendar & Timeline',
      'analysis-view': 'Deep-Dive Risk Analysis',
      'loans-view': 'Manage Loans & Lenders',
      'income-view': 'Income Pattern Setup',
      'payments-view': 'Upcoming Payment Stack',
      'reports-view': 'Financial Risk Reports',
      'settings-view': 'Settings & Demo Tools'
    };
    const titleElem = document.getElementById('pageHeaderTitle');
    if (titleElem && titles[viewId]) {
      titleElem.textContent = titles[viewId];
    }

    // Close mobile menu if open
    document.getElementById('sidebar').classList.remove('mobile-open');

    // Trigger lazy loading per view
    if (viewId === 'dashboard-view') this.loadDashboard();
    if (viewId === 'calendar-view') this.loadCalendar();
    if (viewId === 'analysis-view') this.loadAnalysis();
    if (viewId === 'loans-view') this.loadLoans();
    if (viewId === 'income-view') this.loadIncomePattern();
    if (viewId === 'payments-view') this.loadPayments();
    if (viewId === 'reports-view') this.loadReport();
  },

  async loadAll() {
    try {
      await Promise.all([
        this.loadDashboard(),
        this.loadLoans(),
        this.loadIncomePattern()
      ]);
    } catch (err) {
      console.error('Error initializing application data:', err);
    }
  },

  // ================= 1. DASHBOARD =================
  async loadDashboard() {
    try {
      const summary = await api.getDashboard();
      this.currentDashboard = summary;

      // Top Status Pill
      const statusPill = document.getElementById('topStatusPill');
      const statusDot = document.getElementById('topStatusDot');
      const statusText = document.getElementById('topStatusText');

      if (summary.riskyWeeksCount > 0) {
        statusDot.className = 'status-dot shortfall';
        statusText.textContent = `🔴 Shortfall Risk (${summary.riskyWeeksCount} wks)`;
      } else if (summary.warningWeeksCount > 0) {
        statusDot.className = 'status-dot warning';
        statusText.textContent = '🟠 Tight Buffer';
      } else {
        statusDot.className = 'status-dot safe';
        statusText.textContent = '🟢 All EMIs Safe';
      }

      // Affordability Banner
      const banner = document.getElementById('dashAffordabilityBanner');
      const bannerIcon = document.getElementById('dashAffordabilityIcon');
      const bannerTitle = document.getElementById('dashAffordabilityTitle');
      const bannerSub = document.getElementById('dashAffordabilitySub');

      if (summary.riskyWeeksCount > 0) {
        banner.className = 'affordability-banner shortfall';
        bannerIcon.className = 'banner-icon-box shortfall';
        bannerIcon.textContent = '🔴';
        bannerTitle.textContent = summary.affordabilityHeadline || '🔴 SHORTFALL RISK DETECTED';
        bannerSub.textContent = summary.affordabilitySubtitle || 'Your upcoming EMI stack exceeds expected income!';
      } else if (summary.warningWeeksCount > 0) {
        banner.className = 'affordability-banner warning';
        bannerIcon.className = 'banner-icon-box warning';
        bannerIcon.textContent = '🟠';
        bannerTitle.textContent = summary.affordabilityHeadline || '🟠 TIGHT CASH BUFFER';
        bannerSub.textContent = summary.affordabilitySubtitle || 'You can cover your dues, but with very little cash left over.';
      } else {
        banner.className = 'affordability-banner safe';
        bannerIcon.className = 'banner-icon-box safe';
        bannerIcon.textContent = '🟢';
        bannerTitle.textContent = summary.affordabilityHeadline || '🟢 ALL UPCOMING EMIS ARE AFFORDABLE';
        bannerSub.textContent = summary.affordabilitySubtitle || 'Your expected income comfortably covers all combined EMI obligations.';
      }

      // Metrics
      document.getElementById('dashActiveLoansCount').textContent = summary.totalActiveLoans;
      document.getElementById('dashMonthlyEmi').textContent = `${this.currencySymbol}${Math.round(summary.totalMonthlyEmiObligation).toLocaleString()}`;
      document.getElementById('dashMonthlyIncome').textContent = `${this.currencySymbol}${Math.round(summary.expectedMonthlyIncome).toLocaleString()}`;
      document.getElementById('dashCurrentWeekEmi').textContent = `${this.currencySymbol}${Math.round(summary.currentWeekEmi).toLocaleString()}`;

      const currAvail = Math.round(summary.currentWeekAvailable);
      const currAvailElem = document.getElementById('dashCurrentWeekBalance');
      if (currAvail >= 0) {
        currAvailElem.className = 'metric-sub positive';
        currAvailElem.textContent = `Available buffer: ${this.currencySymbol}${currAvail.toLocaleString()}`;
      } else {
        currAvailElem.className = 'metric-sub negative';
        currAvailElem.textContent = `Shortfall deficit: -${this.currencySymbol}${Math.abs(currAvail).toLocaleString()}`;
      }

      // Risk counts
      document.getElementById('dashSafeWeeks').textContent = summary.safeWeeksCount;
      document.getElementById('dashWarningWeeks').textContent = summary.warningWeeksCount;
      document.getElementById('dashRiskyWeeks').textContent = summary.riskyWeeksCount;
      document.getElementById('dashProjectedShortfall').textContent = `${this.currencySymbol}${Math.round(summary.totalProjectedShortfall).toLocaleString()} max shortfall`;

      // Render Chart
      if (summary.weeklyPeriods && summary.weeklyPeriods.length > 0) {
        chartEngine.renderDashboardChart('dashboardRiskChart', summary.weeklyPeriods);
      }

      // Render Stacking Alert Zone
      const alertZone = document.getElementById('dashStackingAlertZone');
      const riskyPeriods = (summary.weeklyPeriods || []).filter(p => p.riskLevel === 'SHORTFALL');
      
      if (riskyPeriods.length === 0) {
        alertZone.innerHTML = `
          <div style="padding: 1.5rem; text-align: center; background: rgba(16, 185, 129, 0.08); border-radius: var(--radius-md); border: 1px solid var(--border-emerald);">
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">🎉</div>
            <div style="font-weight: 700; color: var(--accent-emerald-light);">No Stacking Collisions!</div>
            <div style="font-size: 0.82rem; color: var(--text-secondary); margin-top: 0.25rem;">Your loans are spaced out well across your pay periods.</div>
          </div>
        `;
      } else {
        const topRisk = riskyPeriods[0];
        alertZone.innerHTML = `
          <div style="padding: 1.25rem; background: rgba(239, 68, 68, 0.1); border-radius: var(--radius-md); border: 1px solid var(--border-crimson);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
              <span class="risk-badge shortfall">Peak Shortfall Week</span>
              <span style="font-family: var(--font-display); font-weight: 700; color: var(--accent-crimson-light);">${topRisk.label}</span>
            </div>
            <div style="font-size: 0.88rem; color: var(--text-primary); margin-bottom: 0.75rem;">
              <strong>${this.currencySymbol}${Math.round(topRisk.totalEmiDue).toLocaleString()}</strong> due across ${topRisk.contributingPayments ? topRisk.contributingPayments.length : 'multiple'} loans vs expected income of <strong>${this.currencySymbol}${Math.round(topRisk.expectedIncome).toLocaleString()}</strong>.
            </div>
            <div style="font-size: 0.85rem; font-weight: 700; color: var(--accent-crimson-light);">
              ⚠️ Exact Shortfall: ${this.currencySymbol}${Math.round(topRisk.shortfallAmount).toLocaleString()}
            </div>
          </div>
        `;
      }

      // Render 14-day upcoming payments
      const upcomingBody = document.getElementById('dashUpcomingTableBody');
      const payments = (summary.upcomingPayments || []).slice(0, 5);
      if (payments.length === 0) {
        upcomingBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No upcoming payments scheduled.</td></tr>`;
      } else {
        upcomingBody.innerHTML = payments.map(p => `
          <tr>
            <td><strong>${p.dueDate}</strong></td>
            <td>${p.lenderName}</td>
            <td><span class="loan-type-pill">${p.loanType}</span></td>
            <td><strong style="font-family: var(--font-display);">${this.currencySymbol}${Math.round(p.amount).toLocaleString()}</strong></td>
            <td><span style="color: var(--text-muted); font-size: 0.8rem;">${p.periodLabel}</span></td>
          </tr>
        `).join('');
      }

    } catch (err) {
      console.error('Failed to load dashboard:', err);
    }
  },

  // ================= 2. EMI SIMULATOR (WHAT-IF) =================
  async runSimulation(e) {
    if (e) e.preventDefault();

    const request = {
      loanName: document.getElementById('simLoanName').value,
      loanType: document.getElementById('simLoanType').value,
      emiAmount: parseFloat(document.getElementById('simEmiAmount').value) || 0,
      firstDueDate: document.getElementById('simFirstDueDate').value,
      durationMonths: parseInt(document.getElementById('simDuration').value) || 6,
      frequency: document.getElementById('simFrequency').value
    };

    if (!request.emiAmount || request.emiAmount <= 0) {
      this.showToast('Please enter a valid EMI amount', 'error');
      return;
    }

    try {
      const result = await api.simulateLoan(request);
      this.currentSimResult = result;

      document.getElementById('simEmptyState').style.display = 'none';
      document.getElementById('simResultsContainer').style.display = 'block';

      // Verdict Banner
      const verdictBanner = document.getElementById('simVerdictBanner');
      const verdictIcon = document.getElementById('simVerdictIcon');
      const verdictTitle = document.getElementById('simVerdictTitle');
      const verdictSub = document.getElementById('simVerdictSub');
      const verdictPill = document.getElementById('simVerdictPill');

      if (result.verdict === 'SHORTFALL_RISK') {
        verdictBanner.className = 'affordability-banner shortfall';
        verdictIcon.className = 'banner-icon-box shortfall';
        verdictIcon.textContent = '🔴';
        verdictTitle.textContent = 'Shortfall Risk Detected';
        verdictSub.textContent = result.verdictHeadline || 'Taking this EMI will push your obligations beyond your expected income!';
        verdictPill.className = 'risk-badge shortfall';
        verdictPill.textContent = '🔴 Shortfall Risk';
      } else if (result.verdict === 'EXISTING_RISK_PERSISTS') {
        verdictBanner.className = 'affordability-banner warning';
        verdictIcon.className = 'banner-icon-box warning';
        verdictIcon.textContent = '🟠';
        verdictTitle.textContent = 'Caution - Financial Strain';
        verdictSub.textContent = result.verdictHeadline;
        verdictPill.className = 'risk-badge warning';
        verdictPill.textContent = '🟠 Caution';
      } else {
        verdictBanner.className = 'affordability-banner safe';
        verdictIcon.className = 'banner-icon-box safe';
        verdictIcon.textContent = '🟢';
        verdictTitle.textContent = 'Safe to Take!';
        verdictSub.textContent = result.verdictHeadline || 'Your expected income comfortably covers this new EMI.';
        verdictPill.className = 'risk-badge safe';
        verdictPill.textContent = '🟢 Safe to take';
      }

      // Comparison Side-by-Side Cards
      document.getElementById('simBeforeIncome').textContent = `${this.currencySymbol}${Math.round(result.currentWeeklyIncome).toLocaleString()}`;
      document.getElementById('simBeforeEmi').textContent = `${this.currencySymbol}${Math.round(result.currentWeeklyEmi).toLocaleString()}`;
      document.getElementById('simBeforeBuffer').textContent = `${this.currencySymbol}${Math.round(result.currentWeeklyBuffer).toLocaleString()}`;
      document.getElementById('simBeforeWeekCounts').textContent = `🟢 ${result.beforeSafeWeeks} Safe, 🔴 ${result.beforeRiskyWeeks} Risky`;

      document.getElementById('simAfterIncome').textContent = `${this.currencySymbol}${Math.round(result.currentWeeklyIncome).toLocaleString()}`;
      document.getElementById('simAfterEmi').textContent = `${this.currencySymbol}${Math.round(result.afterWeeklyEmi).toLocaleString()}`;

      const afterBuffer = Math.round(result.afterWeeklyBufferOrShortfall);
      const afterBufferElem = document.getElementById('simAfterBuffer');
      if (afterBuffer >= 0) {
        afterBufferElem.className = 'val safe-val';
        afterBufferElem.textContent = `${this.currencySymbol}${afterBuffer.toLocaleString()}`;
      } else {
        afterBufferElem.className = 'val shortfall-val';
        afterBufferElem.textContent = `-${this.currencySymbol}${Math.abs(afterBuffer).toLocaleString()}`;
      }
      document.getElementById('simAfterWeekCounts').textContent = `🟢 ${result.afterSafeWeeks} Safe, 🔴 ${result.afterRiskyWeeks} Risky`;

      // Recommendation Text
      document.getElementById('simRecommendationText').innerHTML = `
        <strong>Analysis:</strong> ${result.detailedAnalysis}<br><br>
        <strong>Recommendation:</strong> ${result.recommendation}
      `;

      // Render Before vs After Chart
      chartEngine.renderSimulationComparisonChart('simulationChart', result);

      // Render Newly Risky Weeks
      const newlyRiskyContainer = document.getElementById('simNewlyRiskyList');
      const newRisks = result.newlyRiskyWeeks || [];
      if (newRisks.length === 0) {
        newlyRiskyContainer.innerHTML = `
          <div style="padding: 1rem; color: var(--accent-emerald-light); background: rgba(16, 185, 129, 0.08); border-radius: var(--radius-md);">
            ✅ No new shortfall weeks created! All weeks remain within your income limits.
          </div>
        `;
      } else {
        newlyRiskyContainer.innerHTML = `
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Affected Week</th>
                  <th>Expected Income</th>
                  <th>New Total EMI</th>
                  <th>Exact Shortfall</th>
                </tr>
              </thead>
              <tbody>
                ${newRisks.map(r => `
                  <tr class="highlight-danger">
                    <td><strong>${r.label}</strong></td>
                    <td>${this.currencySymbol}${Math.round(r.expectedIncome).toLocaleString()}</td>
                    <td><strong style="color: var(--accent-crimson-light);">${this.currencySymbol}${Math.round(r.totalEmiDue).toLocaleString()}</strong></td>
                    <td><strong style="color: var(--accent-crimson-light);">-₹${Math.round(r.shortfallAmount).toLocaleString()}</strong></td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        `;
      }

      this.showToast('Simulation completed!', 'success');

    } catch (err) {
      console.error('Simulation failed:', err);
      this.showToast('Simulation failed: ' + err.message, 'error');
    }
  },

  resetSimForm() {
    document.getElementById('simulatorForm').reset();
    this.initDates();
    document.getElementById('simEmptyState').style.display = 'block';
    document.getElementById('simResultsContainer').style.display = 'none';
  },

  async commitSimulatedLoan() {
    if (!this.currentSimResult || !this.currentSimResult.simulationRequest) return;
    const req = this.currentSimResult.simulationRequest;

    const newLoan = {
      lenderName: req.loanName,
      loanType: req.loanType,
      emiAmount: req.emiAmount,
      dueDayOfMonth: new Date(req.firstDueDate).getDate(),
      startDate: req.firstDueDate,
      durationMonths: req.durationMonths,
      frequency: req.frequency,
      notes: 'Added from EMI Simulator'
    };

    try {
      await api.createLoan(newLoan);
      this.showToast('Simulated loan added to active loans!', 'success');
      this.navigateTo('loans-view');
    } catch (err) {
      this.showToast('Failed to add loan: ' + err.message, 'error');
    }
  },

  // ================= 3. RISK CALENDAR =================
  async loadCalendar(weeks = 8) {
    try {
      const calendar = await api.getRiskCalendar(weeks);
      
      // Render Cards
      const cardsGrid = document.getElementById('calendarCardsGrid');
      cardsGrid.innerHTML = calendar.map(w => {
        let statusClass = 'status-safe';
        let badgeClass = 'safe';
        let badgeText = '🟢 Safe';

        if (w.riskLevel === 'SHORTFALL') {
          statusClass = 'status-shortfall';
          badgeClass = 'shortfall';
          badgeText = '🔴 Shortfall';
        } else if (w.riskLevel === 'WARNING') {
          statusClass = 'status-warning';
          badgeClass = 'warning';
          badgeText = '🟠 Warning';
        }

        return `
          <div class="timeline-week-card ${statusClass}">
            <div class="timeline-card-header">
              <span class="timeline-week-label">${w.label}</span>
              <span class="risk-badge ${badgeClass}">${badgeText}</span>
            </div>
            <div class="timeline-row">
              <span class="timeline-row-label">Expected Income</span>
              <span class="timeline-row-val">${this.currencySymbol}${Math.round(w.expectedIncome).toLocaleString()}</span>
            </div>
            <div class="timeline-row">
              <span class="timeline-row-label">Total EMI Due</span>
              <span class="timeline-row-val">${this.currencySymbol}${Math.round(w.totalEmiDue).toLocaleString()}</span>
            </div>
            <div class="timeline-row">
              <span class="timeline-row-label">Net Balance</span>
              <span class="timeline-row-val ${w.availableAmount < 0 ? 'shortfall-val' : 'safe-val'}">
                ${w.availableAmount < 0 ? '-' : ''}${this.currencySymbol}${Math.abs(Math.round(w.availableAmount)).toLocaleString()}
              </span>
            </div>
            ${w.shortfallAmount > 0 ? `
              <div style="margin-top: 0.5rem; padding: 0.4rem; background: rgba(239,68,68,0.15); border-radius: var(--radius-sm); font-size: 0.75rem; color: var(--accent-crimson-light); font-weight: 700; text-align: center;">
                ⚠️ Shortfall: ${this.currencySymbol}${Math.round(w.shortfallAmount).toLocaleString()}
              </div>
            ` : ''}
          </div>
        `;
      }).join('');

      // Render Table
      const tableBody = document.getElementById('calendarTableBody');
      tableBody.innerHTML = calendar.map(w => `
        <tr class="${w.riskLevel === 'SHORTFALL' ? 'highlight-danger' : ''}">
          <td><strong>${w.label}</strong></td>
          <td>${this.currencySymbol}${Math.round(w.expectedIncome).toLocaleString()}</td>
          <td><strong>${this.currencySymbol}${Math.round(w.totalEmiDue).toLocaleString()}</strong></td>
          <td><strong style="color: ${w.availableAmount < 0 ? 'var(--accent-crimson-light)' : 'var(--accent-emerald-light)'};">
            ${w.availableAmount < 0 ? '-' : ''}${this.currencySymbol}${Math.abs(Math.round(w.availableAmount)).toLocaleString()}
          </strong></td>
          <td><span class="risk-badge ${w.riskLevel.toLowerCase()}">${w.riskLevel === 'SHORTFALL' ? '🔴 Shortfall' : (w.riskLevel === 'WARNING' ? '🟠 Warning' : '🟢 Safe')}</span></td>
          <td style="font-size: 0.8rem; color: var(--text-secondary);">
            ${(w.contributingPayments || []).map(p => `${p.lenderName} (${this.currencySymbol}${Math.round(p.amount).toLocaleString()})`).join(', ') || 'No EMIs due'}
          </td>
        </tr>
      `).join('');

    } catch (err) {
      console.error('Failed to load calendar:', err);
    }
  },

  // ================= 4. RISK ANALYSIS =================
  async loadAnalysis() {
    try {
      const loans = await api.getLoans();
      chartEngine.renderLoanDistributionChart('loanDistributionChart', loans);
    } catch (err) {
      console.error('Failed to load analysis:', err);
    }
  },

  // ================= 5. LOANS MANAGEMENT =================
  async loadLoans() {
    try {
      const loans = await api.getLoans();
      this.currentLoans = loans;

      const container = document.getElementById('loansCardsContainer');
      if (loans.length === 0) {
        container.innerHTML = `
          <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 3rem 2rem;">
            <div style="font-size: 2.5rem; margin-bottom: 0.75rem;">💳</div>
            <h3>No Active Loans Found</h3>
            <p style="margin-top: 0.5rem; color: var(--text-secondary);">Add your existing loans or reset the demo dataset.</p>
            <div style="margin-top: 1.25rem; display: flex; gap: 0.75rem; justify-content: center;">
              <button class="btn btn-primary btn-sm" onclick="app.openAddLoanModal()">➕ Add Loan</button>
              <button class="btn btn-secondary btn-sm" onclick="app.resetSampleData()">🔄 Load Sample Data</button>
            </div>
          </div>
        `;
        return;
      }

      container.innerHTML = loans.map(loan => `
        <div class="loan-item-card">
          <div>
            <div class="loan-header">
              <div>
                <h4 style="font-size: 1.1rem; margin-bottom: 0.2rem;">${loan.lenderName}</h4>
                <span class="loan-type-pill">${loan.loanType}</span>
              </div>
              <span class="risk-badge safe">Active</span>
            </div>

            <div class="loan-amount-box">
              <span class="loan-amount-label">Monthly EMI Due</span>
              <span class="loan-amount-val">${this.currencySymbol}${Math.round(loan.emiAmount).toLocaleString()}</span>
            </div>

            <div class="loan-meta-grid">
              <div class="loan-meta-item">
                <span class="loan-meta-label">Due Date</span>
                <span class="loan-meta-val">${loan.dueDayOfMonth}th of month</span>
              </div>
              <div class="loan-meta-item">
                <span class="loan-meta-label">Tenure</span>
                <span class="loan-meta-val">${loan.durationMonths ? loan.durationMonths + ' mos' : 'Ongoing'}</span>
              </div>
              <div class="loan-meta-item">
                <span class="loan-meta-label">Start Date</span>
                <span class="loan-meta-val">${loan.startDate || 'N/A'}</span>
              </div>
              <div class="loan-meta-item">
                <span class="loan-meta-label">Total Principal</span>
                <span class="loan-meta-val">${loan.totalPrincipal ? this.currencySymbol + Math.round(loan.totalPrincipal).toLocaleString() : 'N/A'}</span>
              </div>
            </div>

            ${loan.notes ? `
              <div style="font-size: 0.78rem; color: var(--text-muted); margin-bottom: 1rem; font-style: italic;">
                "${loan.notes}"
              </div>
            ` : ''}
          </div>

          <div class="loan-actions">
            <button class="btn btn-sm btn-secondary" onclick="app.openEditLoanModal('${loan.id}')">✏️ Edit</button>
            <button class="btn btn-sm btn-danger" onclick="app.promptDeleteLoan('${loan.id}', '${loan.lenderName}', ${loan.emiAmount})">🗑️ Delete</button>
          </div>
        </div>
      `).join('');

    } catch (err) {
      console.error('Failed to load loans:', err);
    }
  },

  openAddLoanModal() {
    document.getElementById('loanModalTitle').textContent = '➕ Add New Loan';
    document.getElementById('loanEditId').value = '';
    document.getElementById('loanForm').reset();
    document.getElementById('loanStartDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('loanModal').classList.add('active');
  },

  openEditLoanModal(id) {
    const loan = this.currentLoans.find(l => l.id === id);
    if (!loan) return;

    document.getElementById('loanModalTitle').textContent = '✏️ Edit Loan';
    document.getElementById('loanEditId').value = loan.id;
    document.getElementById('loanLenderName').value = loan.lenderName || '';
    document.getElementById('loanTypeSelect').value = loan.loanType || 'PERSONAL_LOAN';
    document.getElementById('loanEmiAmount').value = loan.emiAmount || '';
    document.getElementById('loanDueDay').value = loan.dueDayOfMonth || 5;
    document.getElementById('loanStartDate').value = loan.startDate || '';
    document.getElementById('loanDurationMonths').value = loan.durationMonths || '';
    document.getElementById('loanPrincipal').value = loan.totalPrincipal || '';
    document.getElementById('loanNotes').value = loan.notes || '';

    document.getElementById('loanModal').classList.add('active');
  },

  async saveLoan(e) {
    if (e) e.preventDefault();

    const editId = document.getElementById('loanEditId').value;
    const loanData = {
      lenderName: document.getElementById('loanLenderName').value,
      loanType: document.getElementById('loanTypeSelect').value,
      emiAmount: parseFloat(document.getElementById('loanEmiAmount').value) || 0,
      dueDayOfMonth: parseInt(document.getElementById('loanDueDay').value) || 5,
      startDate: document.getElementById('loanStartDate').value,
      durationMonths: parseInt(document.getElementById('loanDurationMonths').value) || null,
      totalPrincipal: parseFloat(document.getElementById('loanPrincipal').value) || null,
      notes: document.getElementById('loanNotes').value,
      frequency: 'MONTHLY'
    };

    try {
      if (editId) {
        await api.updateLoan(editId, loanData);
        this.showToast('Loan updated successfully!', 'success');
      } else {
        await api.createLoan(loanData);
        this.showToast('New loan added successfully!', 'success');
      }
      this.closeModal('loanModal');
      this.loadDashboard();
      this.loadLoans();
    } catch (err) {
      this.showToast('Failed to save loan: ' + err.message, 'error');
    }
  },

  promptDeleteLoan(id, lenderName, emiAmount) {
    this.pendingDeleteId = id;
    document.getElementById('deleteModalPrompt').innerHTML = `
      Are you sure you want to delete <strong>${lenderName}</strong> (EMI: ${this.currencySymbol}${Math.round(emiAmount).toLocaleString()})?<br><br>
      This will remove it from all future risk stacking calculations.
    `;
    document.getElementById('deleteModal').classList.add('active');
  },

  async executeDeleteLoan() {
    if (!this.pendingDeleteId) return;
    try {
      await api.deleteLoan(this.pendingDeleteId);
      this.showToast('Loan deleted successfully', 'info');
      this.closeModal('deleteModal');
      this.pendingDeleteId = null;
      this.loadDashboard();
      this.loadLoans();
    } catch (err) {
      this.showToast('Delete failed: ' + err.message, 'error');
    }
  },

  // ================= 6. INCOME PATTERN =================
  async loadIncomePattern() {
    try {
      const pattern = await api.getIncomePattern();
      this.currentIncomePattern = pattern;

      document.getElementById('incomePatternType').value = pattern.patternType || 'WEEKLY';
      document.getElementById('incomeDefaultAmount').value = pattern.defaultAmount || 9000;
      document.getElementById('incomePayDayOfWeek').value = pattern.payDayOfWeek || 5;
      document.getElementById('incomePayDayOfMonth').value = pattern.payDayOfMonth || 1;
      if (pattern.nextPayDate) {
        document.getElementById('incomeNextPayDate').value = pattern.nextPayDate;
      }

      this.handleIncomeTypeChange();

      // Sidebar widget update
      const sidebarVal = document.getElementById('sidebarIncomeVal');
      if (sidebarVal) {
        sidebarVal.textContent = `${this.currencySymbol}${Math.round(pattern.defaultAmount).toLocaleString()} / ${pattern.patternType.toLowerCase()}`;
      }

      // Render custom entries table
      const customBody = document.getElementById('customIncomeTableBody');
      const customList = pattern.customEntries || [];
      if (customList.length === 0) {
        customBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-muted);">No custom income streams configured.</td></tr>`;
      } else {
        customBody.innerHTML = customList.map(s => `
          <tr>
            <td><strong>${s.sourceName}</strong></td>
            <td>${s.expectedDate}</td>
            <td><strong>${this.currencySymbol}${Math.round(s.amount).toLocaleString()}</strong></td>
          </tr>
        `).join('');
      }

    } catch (err) {
      console.error('Failed to load income pattern:', err);
    }
  },

  handleIncomeTypeChange() {
    const type = document.getElementById('incomePatternType').value;
    const weeklyRow = document.getElementById('incomeWeeklyRow');
    const monthlyRow = document.getElementById('incomeMonthlyRow');

    if (type === 'MONTHLY') {
      weeklyRow.style.display = 'none';
      monthlyRow.style.display = 'block';
    } else {
      weeklyRow.style.display = 'grid';
      monthlyRow.style.display = 'none';
    }
  },

  async saveIncomePattern(e) {
    if (e) e.preventDefault();

    const patternData = {
      patternType: document.getElementById('incomePatternType').value,
      defaultAmount: parseFloat(document.getElementById('incomeDefaultAmount').value) || 0,
      payDayOfWeek: parseInt(document.getElementById('incomePayDayOfWeek').value) || 5,
      payDayOfMonth: parseInt(document.getElementById('incomePayDayOfMonth').value) || 1,
      nextPayDate: document.getElementById('incomeNextPayDate').value,
      customEntries: this.currentIncomePattern ? this.currentIncomePattern.customEntries : []
    };

    try {
      await api.updateIncomePattern(patternData);
      this.showToast('Income configuration saved!', 'success');
      this.loadDashboard();
      this.loadIncomePattern();
    } catch (err) {
      this.showToast('Failed to update income: ' + err.message, 'error');
    }
  },

  openCustomIncomeModal() {
    document.getElementById('customIncomeForm').reset();
    document.getElementById('customIncomeDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('customIncomeModal').classList.add('active');
  },

  async saveCustomIncome(e) {
    if (e) e.preventDefault();

    const entry = {
      sourceName: document.getElementById('customIncomeSource').value,
      amount: parseFloat(document.getElementById('customIncomeAmount').value) || 0,
      expectedDate: document.getElementById('customIncomeDate').value,
      recurring: true
    };

    try {
      await api.addCustomIncome(entry);
      this.showToast('Custom income stream added!', 'success');
      this.closeModal('customIncomeModal');
      this.loadIncomePattern();
      this.loadDashboard();
    } catch (err) {
      this.showToast('Failed to add custom income: ' + err.message, 'error');
    }
  },

  // ================= 7. UPCOMING PAYMENTS =================
  async loadPayments() {
    try {
      const payments = await api.getUpcomingPayments(60);
      const tbody = document.getElementById('allUpcomingPaymentsTableBody');

      if (payments.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No upcoming payments found.</td></tr>`;
        return;
      }

      tbody.innerHTML = payments.map(p => `
        <tr>
          <td><strong style="font-family: var(--font-display);">${p.dueDate}</strong></td>
          <td>${p.lenderName}</td>
          <td><span class="loan-type-pill">${p.loanType}</span></td>
          <td><strong style="font-family: var(--font-display);">${this.currencySymbol}${Math.round(p.amount).toLocaleString()}</strong></td>
          <td><span style="color: var(--text-muted);">${p.periodLabel}</span></td>
          <td><span class="risk-badge safe">Scheduled</span></td>
        </tr>
      `).join('');
    } catch (err) {
      console.error('Failed to load upcoming payments:', err);
    }
  },

  // ================= 8. REPORTS =================
  async loadReport() {
    const startDate = document.getElementById('reportStartDate').value;
    const endDate = document.getElementById('reportEndDate').value;

    try {
      const report = await api.getReport(startDate, endDate);
      const container = document.getElementById('reportOutputArea');

      container.innerHTML = `
        <div class="metrics-grid" style="margin-bottom: 1.5rem;">
          <div class="metric-card">
            <div class="metric-label">Total Expected Income</div>
            <div class="metric-value" style="color: var(--accent-emerald-light);">${this.currencySymbol}${Math.round(report.totalExpectedIncomeInPeriod).toLocaleString()}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">Total EMI Obligations</div>
            <div class="metric-value" style="color: var(--accent-crimson-light);">${this.currencySymbol}${Math.round(report.totalEmiObligationInPeriod).toLocaleString()}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">Net Projected Cashflow</div>
            <div class="metric-value">${report.netBalanceInPeriod >= 0 ? '' : '-'}${this.currencySymbol}${Math.abs(Math.round(report.netBalanceInPeriod)).toLocaleString()}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">Total Risk Weeks</div>
            <div class="metric-value" style="color: var(--accent-crimson-light);">${report.riskyWeeksCount} of ${report.totalWeeksAnalyzed}</div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Audit Log for Period (${report.reportStartDate} to ${report.reportEndDate})</h3>
          </div>
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Period</th>
                  <th>Expected Income</th>
                  <th>Total EMI Due</th>
                  <th>Net Balance</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                ${(report.periods || []).map(p => `
                  <tr class="${p.riskLevel === 'SHORTFALL' ? 'highlight-danger' : ''}">
                    <td><strong>${p.label}</strong></td>
                    <td>${this.currencySymbol}${Math.round(p.expectedIncome).toLocaleString()}</td>
                    <td>${this.currencySymbol}${Math.round(p.totalEmiDue).toLocaleString()}</td>
                    <td>${p.availableAmount < 0 ? '-' : ''}${this.currencySymbol}${Math.abs(Math.round(p.availableAmount)).toLocaleString()}</td>
                    <td><span class="risk-badge ${p.riskLevel.toLowerCase()}">${p.riskLevel}</span></td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `;
    } catch (err) {
      console.error('Failed to load report:', err);
    }
  },

  async exportReportJson() {
    const startDate = document.getElementById('reportStartDate').value;
    const endDate = document.getElementById('reportEndDate').value;
    try {
      const report = await api.getReport(startDate, endDate);
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(report, null, 2));
      const downloadAnchor = document.createElement('a');
      downloadAnchor.setAttribute("href", dataStr);
      downloadAnchor.setAttribute("download", `EMI_Risk_Report_${startDate}_${endDate}.json`);
      document.body.appendChild(downloadAnchor);
      downloadAnchor.click();
      downloadAnchor.remove();
      this.showToast('Report downloaded as JSON', 'success');
    } catch (err) {
      this.showToast('Export failed: ' + err.message, 'error');
    }
  },

  // ================= 9. SETTINGS & SAMPLE DATA =================
  async resetSampleData() {
    try {
      await api.resetAllSampleData();
      this.showToast('Sample dataset restored successfully!', 'success');
      this.loadAll();
      this.navigateTo('dashboard-view');
    } catch (err) {
      this.showToast('Failed to reset data: ' + err.message, 'error');
    }
  },

  async clearAllData() {
    if (!confirm('Are you sure you want to clear all active loans?')) return;
    try {
      await api.clearAllData();
      this.showToast('All loans cleared', 'info');
      this.loadAll();
      this.navigateTo('loans-view');
    } catch (err) {
      this.showToast('Clear failed: ' + err.message, 'error');
    }
  },

  // ================= MODAL & TOAST UTILITIES =================
  closeModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('active');
  },

  showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✅' : (type === 'error' ? '❌' : 'ℹ️');
    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }
};

// Auto bootstrap when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  app.init();
});
