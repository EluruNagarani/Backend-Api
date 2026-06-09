package com.chubb.assessment.api.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyStatusResponse {

    ACTIVE("Active"),
    EXPIRED("Expired"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String displayName;

    PolicyStatusResponse(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
