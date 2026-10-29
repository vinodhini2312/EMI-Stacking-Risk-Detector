/**
 * EMI Stacking Risk Detector - Chart Engine (Chart.js wrapper)
 */

let dashboardChartInstance = null;
let simulationChartInstance = null;
let loanDistributionChartInstance = null;

const chartEngine = {
  getThemeColors() {
    const isLight = document.documentElement.getAttribute('data-theme') === 'light';
    return {
      textColor: isLight ? '#475569' : '#94a3b8',
      gridColor: isLight ? 'rgba(0, 0, 0, 0.06)' : 'rgba(255, 255, 255, 0.05)',
      tooltipBg: isLight ? '#ffffff' : '#1e293b',
      tooltipTitle: isLight ? '#0f172a' : '#f8fafc',
      tooltipBody: isLight ? '#334155' : '#cbd5e1',
      tooltipBorder: isLight ? 'rgba(0, 0, 0, 0.12)' : 'rgba(255, 255, 255, 0.15)',
      doughnutBorder: isLight ? '#ffffff' : '#111827'
    };
  },

  /**
   * Render or update the Dashboard Risk Overview Chart
   */
  renderDashboardChart(canvasId, periods) {
    const ctx = document.getElementById(canvasId);
    if (!ctx || !window.Chart) return;

    const theme = this.getThemeColors();
    const labels = periods.map(p => p.label);
    const incomeData = periods.map(p => p.expectedIncome);
    const emiData = periods.map(p => p.totalEmiDue);
    const backgroundColors = periods.map(p => {
      if (p.riskLevel === 'SHORTFALL') return 'rgba(239, 68, 68, 0.85)';
      if (p.riskLevel === 'WARNING') return 'rgba(245, 158, 11, 0.85)';
      return 'rgba(16, 185, 129, 0.75)';
    });

    if (dashboardChartInstance) {
      dashboardChartInstance.destroy();
    }

    dashboardChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            type: 'line',
            label: 'Expected Income (₹)',
            data: incomeData,
            borderColor: '#38bdf8',
            backgroundColor: 'rgba(56, 189, 248, 0.1)',
            borderWidth: 3,
            borderDash: [5, 5],
            pointBackgroundColor: '#38bdf8',
            pointRadius: 5,
            fill: false,
            tension: 0.2,
            order: 1
          },
          {
            type: 'bar',
            label: 'Total EMI Due (₹)',
            data: emiData,
            backgroundColor: backgroundColors,
            borderRadius: 8,
            borderWidth: 1,
            borderColor: 'rgba(255, 255, 255, 0.1)',
            order: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          legend: {
            position: 'top',
            labels: {
              color: theme.textColor,
              font: { family: 'Inter', size: 12 }
            }
          },
          tooltip: {
            backgroundColor: theme.tooltipBg,
            titleColor: theme.tooltipTitle,
            bodyColor: theme.tooltipBody,
            borderColor: theme.tooltipBorder,
            borderWidth: 1,
            padding: 12,
            callbacks: {
              afterBody: function(tooltipItems) {
                const index = tooltipItems[0].dataIndex;
                const period = periods[index];
                if (period.riskLevel === 'SHORTFALL') {
                  return `\n🔴 Shortfall: ₹${period.shortfallAmount.toLocaleString()}`;
                } else {
                  return `\n🟢 Buffer: ₹${period.availableAmount.toLocaleString()}`;
                }
              }
            }
          }
        },
        scales: {
          x: {
            grid: { color: theme.gridColor },
            ticks: { color: theme.textColor, font: { family: 'Inter', size: 11 } }
          },
          y: {
            grid: { color: theme.gridColor },
            ticks: {
              color: theme.textColor,
              font: { family: 'Inter', size: 11 },
              callback: val => '₹' + val.toLocaleString()
            }
          }
        }
      }
    });
  },

  /**
   * Render Before vs After EMI Stacking Comparison Chart in Simulator
   */
  renderSimulationComparisonChart(canvasId, simResult) {
    const ctx = document.getElementById(canvasId);
    if (!ctx || !window.Chart || !simResult) return;

    const theme = this.getThemeColors();
    const beforePeriods = simResult.beforePeriods || [];
    const afterPeriods = simResult.afterPeriods || [];
    const labels = beforePeriods.map(p => p.label);

    const beforeEmi = beforePeriods.map(p => p.totalEmiDue);
    const afterEmi = afterPeriods.map(p => p.totalEmiDue);
    const incomeLine = beforePeriods.map(p => p.expectedIncome);

    if (simulationChartInstance) {
      simulationChartInstance.destroy();
    }

    simulationChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            type: 'line',
            label: 'Expected Income (₹)',
            data: incomeLine,
            borderColor: '#38bdf8',
            borderWidth: 2,
            borderDash: [6, 4],
            pointRadius: 4,
            fill: false,
            order: 1
          },
          {
            type: 'bar',
            label: 'Before (Existing EMI)',
            data: beforeEmi,
            backgroundColor: 'rgba(100, 116, 139, 0.65)',
            borderRadius: 6,
            order: 2
          },
          {
            type: 'bar',
            label: 'After (With New Loan)',
            data: afterEmi,
            backgroundColor: afterPeriods.map(p => p.newlyRisky ? '#ef4444' : (p.riskLevel === 'SHORTFALL' ? '#f87171' : '#6366f1')),
            borderRadius: 6,
            order: 3
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: { color: theme.textColor, font: { family: 'Inter', size: 12 } }
          },
          tooltip: {
            backgroundColor: theme.tooltipBg,
            titleColor: theme.tooltipTitle,
            bodyColor: theme.tooltipBody,
            borderColor: theme.tooltipBorder,
            borderWidth: 1,
            padding: 12
          }
        },
        scales: {
          x: {
            grid: { color: theme.gridColor },
            ticks: { color: theme.textColor, font: { family: 'Inter', size: 11 } }
          },
          y: {
            grid: { color: theme.gridColor },
            ticks: {
              color: theme.textColor,
              font: { family: 'Inter', size: 11 },
              callback: val => '₹' + val.toLocaleString()
            }
          }
        }
      }
    });
  },

  /**
   * Render Loan Type Distribution in Risk Analysis
   */
  renderLoanDistributionChart(canvasId, loans) {
    const ctx = document.getElementById(canvasId);
    if (!ctx || !window.Chart || !loans || loans.length === 0) return;

    const theme = this.getThemeColors();
    const typeTotals = {};
    loans.forEach(loan => {
      const typeName = loan.loanType || 'OTHER';
      typeTotals[typeName] = (typeTotals[typeName] || 0) + (loan.emiAmount || 0);
    });

    const labels = Object.keys(typeTotals);
    const data = Object.values(typeTotals);
    const colors = [
      '#6366f1', '#10b981', '#f59e0b', '#ec4899', '#06b6d4', '#8b5cf6'
    ];

    if (loanDistributionChartInstance) {
      loanDistributionChartInstance.destroy();
    }

    loanDistributionChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: colors.slice(0, labels.length),
          borderWidth: 2,
          borderColor: theme.doughnutBorder
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: { color: theme.textColor, font: { family: 'Inter', size: 11 } }
          }
        },
        cutout: '65%'
      }
    });
  }
};
