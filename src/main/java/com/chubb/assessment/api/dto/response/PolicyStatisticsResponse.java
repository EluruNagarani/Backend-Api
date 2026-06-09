package com.chubb.assessment.api.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record PolicyStatisticsResponse(
        Map<String, Long> countsByStatus,
        Map<String, BigDecimal> totalPremiumByLineOfBusiness,
        long expiringSoonCount) {
}
