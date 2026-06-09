package com.chubb.assessment.domain.models;

import java.time.LocalDate;

public record PolicyFilter(
        PolicyStatus status,
        LineOfBusiness lineOfBusiness,
        String region,
        LocalDate effectiveDateFrom,
        LocalDate effectiveDateTo,
        String search) {
}
