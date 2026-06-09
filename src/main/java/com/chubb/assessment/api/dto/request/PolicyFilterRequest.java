package com.chubb.assessment.api.dto.request;

import java.time.LocalDate;

import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.PolicyStatus;

public record PolicyFilterRequest(
        PolicyStatus status,
        LineOfBusiness lineOfBusiness,
        String region,
        LocalDate effectiveDateFrom,
        LocalDate effectiveDateTo,
        String search) {
}
