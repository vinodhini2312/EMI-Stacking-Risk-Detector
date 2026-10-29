package com.emiguard.model;

public enum RiskLevel {
    SAFE("Safe", "🟢", "Income is sufficient to comfortably cover EMI obligations."),
    WARNING("Warning", "🟠", "Tight margin: EMI obligations consume most of expected income."),
    SHORTFALL("Shortfall Risk", "🔴", "Critical: Total EMI due exceeds expected income!");

    private final String label;
    private final String indicator;
    private final String description;

    RiskLevel(String label, String indicator, String description) {
        this.label = label;
        this.indicator = indicator;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getIndicator() {
        return indicator;
    }

    public String getDescription() {
        return description;
    }
}
