package com.emiguard.model;

public enum LoanType {
    PERSONAL_LOAN("Personal Loan", "🏦"),
    VEHICLE_LOAN("Vehicle Loan", "🚗"),
    PHONE_DEVICE_LOAN("Phone/Device Loan", "📱"),
    BNPL("BNPL (Buy Now Pay Later)", "⚡"),
    EDUCATION_LOAN("Education Loan", "🎓"),
    OTHER("Other Loan", "📋");

    private final String displayName;
    private final String icon;

    LoanType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
