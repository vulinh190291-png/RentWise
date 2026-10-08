package com.rentwise.training.domain;

public enum RiskTopic {
    DEPOSIT_RETURN("押金返还"),
    EARLY_TERMINATION("提前退租"),
    REPAIR_RESPONSIBILITY("维修责任"),
    COST_BEARING("费用承担"),
    BREACH_LIABILITY("违约责任");

    private final String displayName;

    RiskTopic(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
}
