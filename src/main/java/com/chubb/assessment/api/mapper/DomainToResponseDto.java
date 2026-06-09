package com.chubb.assessment.api.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.chubb.assessment.api.dto.response.FlagPolicyResponse;
import com.chubb.assessment.api.dto.response.MoneyResponse;
import com.chubb.assessment.api.dto.response.PagedPolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyStatisticsResponse;
import com.chubb.assessment.api.dto.response.PolicyStatusResponse;
import com.chubb.assessment.api.dto.response.PolicySummaryResponse;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.LineOfBusiness;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.domain.models.PolicyStatus;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class DomainToResponseDto {

    private static final int EXPIRY_WINDOW_DAYS = 30;

    public PagedPolicyResponse toPagedResponse(Page<Policy> policies) {
        List<PolicySummaryResponse> content = policies.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();
        return new PagedPolicyResponse(
                content,
                policies.getNumber(),
                policies.getSize(),
                policies.getTotalElements(),
                policies.getTotalPages());
    }

    public PolicySummaryResponse toSummaryResponse(Policy policy) {
        return new PolicySummaryResponse(
                policy.getPolicyNumber(),
                policy.getPolicyholderName(),
                policy.getRegion(),
                toStatusResponse(policy.getStatus()),
                new MoneyResponse(policy.getPremiumAmount(), policy.getCurrency()),
                policy.getEffectiveDate(),
                policy.getExpiryDate(),
                isExpiringSoon(policy.getExpiryDate()));
    }

    public FlagPolicyResponse toFlagResponse(FlagResult result) {
        return new FlagPolicyResponse(result.requested(), result.flagged(), result.missingIds());
    }

    public PolicyStatisticsResponse toStatisticsResponse(PolicyStatistics statistics) {
        return new PolicyStatisticsResponse(
                toDisplayKeyedCounts(statistics.countsByStatus()),
                toDisplayKeyedPremiums(statistics.totalPremiumByLineOfBusiness()),
                statistics.expiringSoonCount());
    }

    private PolicyStatusResponse toStatusResponse(PolicyStatus status) {
        return PolicyStatusResponse.valueOf(status.name());
    }

    private boolean isExpiringSoon(LocalDate expiryDate) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(EXPIRY_WINDOW_DAYS);
        return !expiryDate.isBefore(today) && !expiryDate.isAfter(threshold);
    }

    private Map<String, Long> toDisplayKeyedCounts(Map<PolicyStatus, Long> countsByStatus) {
        return countsByStatus.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getDisplayName(), Map.Entry::getValue));
    }

    private Map<String, BigDecimal> toDisplayKeyedPremiums(Map<LineOfBusiness, BigDecimal> premiums) {
        return premiums.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getDisplayName(), Map.Entry::getValue));
    }
}
