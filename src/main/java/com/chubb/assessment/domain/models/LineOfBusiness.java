package com.chubb.assessment.domain.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LineOfBusiness {

    PROPERTY("Property"),
    CASUALTY("Casualty"),
    ACCIDENT_AND_HEALTH("A&H"),
    MARINE("Marine");

    private final String displayName;

    LineOfBusiness(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
