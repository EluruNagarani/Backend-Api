package com.chubb.assessment.api.dto.response;

import java.time.LocalDate;

public record PolicySummaryResponse(
        String policyNumber,
        String holderName,
        String region,
        PolicyStatusResponse status,
        MoneyResponse premium,
        LocalDate startDate,
        LocalDate endDate,
        boolean isExpiringSoon) {
}
