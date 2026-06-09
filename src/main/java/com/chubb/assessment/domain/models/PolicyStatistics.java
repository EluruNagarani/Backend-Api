package com.chubb.assessment.domain.models;

import java.math.BigDecimal;
import java.util.Map;

public record PolicyStatistics(
        Map<PolicyStatus, Long> countsByStatus,
        Map<LineOfBusiness, BigDecimal> totalPremiumByLineOfBusiness,
        long expiringSoonCount) {
}
