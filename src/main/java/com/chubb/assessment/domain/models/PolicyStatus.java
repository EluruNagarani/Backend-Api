package com.chubb.assessment.domain.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyStatus {

    ACTIVE("Active"),
    EXPIRED("Expired"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String displayName;

    PolicyStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
